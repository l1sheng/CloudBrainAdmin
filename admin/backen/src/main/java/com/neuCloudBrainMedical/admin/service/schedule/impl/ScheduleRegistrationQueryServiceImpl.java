package com.neuCloudBrainMedical.admin.service.schedule.impl;

import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleRegistrationResponse;
import com.neuCloudBrainMedical.admin.entity.Registration;
import com.neuCloudBrainMedical.admin.entity.schedule.DoctorSchedule;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.repository.RegistrationRepository;
import com.neuCloudBrainMedical.admin.repository.schedule.ScheduleRepository;
import com.neuCloudBrainMedical.admin.service.schedule.IScheduleRegistrationQueryService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScheduleRegistrationQueryServiceImpl implements IScheduleRegistrationQueryService {

	private final ScheduleRepository scheduleRepository;
	private final RegistrationRepository registrationRepository;

	public ScheduleRegistrationQueryServiceImpl(ScheduleRepository scheduleRepository,
			RegistrationRepository registrationRepository) {
		this.scheduleRepository = scheduleRepository;
		this.registrationRepository = registrationRepository;
	}

	@Override
	public List<ScheduleRegistrationResponse> listByScheduleId(Long scheduleId) {
		DoctorSchedule schedule = scheduleRepository.findById(scheduleId)
				.orElseThrow(() -> new BusinessException(404, "排班不存在"));

		List<Registration> registrations = registrationRepository
				.findByScheduleIdOrderByRegisteredAtDesc(schedule.getScheduleId());

		if (registrations.isEmpty()) {
			return List.of();
		}

		Set<Long> registrationIds = registrations.stream()
				.map(Registration::getRegistrationId)
				.collect(Collectors.toSet());

		// registration_id -> patient_name
		Map<Long, String> patientNamesByRegistrationId = new HashMap<>();
		List<Object[]> rows = registrationRepository.findPatientNamesByRegistrationIds(registrationIds);
		for (Object[] row : rows) {
			if (row.length >= 2 && row[0] != null) {
				Number regId = (Number) row[0];
				String name = row[1] != null ? row[1].toString() : "";
				patientNamesByRegistrationId.put(regId.longValue(), name);
			}
		}

		return registrations.stream()
				.map(r -> toResponse(r, patientNamesByRegistrationId))
				.collect(Collectors.toList());
	}

	private ScheduleRegistrationResponse toResponse(Registration registration,
			Map<Long, String> patientNamesByRegistrationId) {
		ScheduleRegistrationResponse response = new ScheduleRegistrationResponse();
		response.setRegistrationId(registration.getRegistrationId());
		response.setRegistrationNo(registration.getRegistrationNo());
		response.setPatientId(registration.getPatientId());
		response.setPatientName(patientNamesByRegistrationId.getOrDefault(registration.getRegistrationId(), ""));
		response.setQueueNo(registration.getQueueNo());
		response.setRegistrationFee(registration.getRegistrationFee());
		response.setFeeStatus(mapFeeStatus(registration.getFeeStatus()));
		response.setStatus(mapRegistrationStatus(registration.getStatus()));
		response.setSource(mapSource(registration.getSource()));
		response.setRegisteredAt(registration.getRegisteredAt());
		return response;
	}

	private String mapFeeStatus(String feeStatus) {
		if (feeStatus == null) return "-";
		switch (feeStatus) {
			case "待支付": return "待支付";
			case "已支付": return "已支付";
			case "部分退费": return "部分退费";
			case "已退费": return "已退费";
			default: return feeStatus;
		}
	}

	private String mapRegistrationStatus(String status) {
		if (status == null) return "-";
		switch (status) {
			case "待支付": return "待支付";
			case "待接诊": return "待接诊";
			case "接诊中": return "接诊中";
			case "已完成": return "已完成";
			case "已取消": return "已取消";
			case "爽约": return "爽约";
			default: return status;
		}
	}

	private String mapSource(String source) {
		if (source == null) return "-";
		switch (source) {
			case "ONLINE":
			case "线上": return "线上";
			case "OFFLINE":
			case "线下": return "线下";
			case "WALKIN":
			case "现场": return "现场";
			case "APP":
			case "APP预约": return "APP预约";
			default: return source;
		}
	}
}