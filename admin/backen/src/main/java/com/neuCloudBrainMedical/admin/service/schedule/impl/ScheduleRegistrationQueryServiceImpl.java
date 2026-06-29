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
		// 数据库已存中文值，直接返回
		return feeStatus != null ? feeStatus : "-";
	}

	private String mapRegistrationStatus(String status) {
		// 数据库已存中文值，直接返回
		return status != null ? status : "-";
	}

	private String mapSource(String source) {
		// 数据库已存中文值，直接返回（兼容旧英文值）
		if (source == null) return "-";
		return switch (source) {
			case "ONLINE", "线上" -> "线上";
			case "OFFLINE", "线下" -> "线下";
			default -> source;
		};
	}
}