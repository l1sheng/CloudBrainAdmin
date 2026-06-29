package com.neuCloudBrainMedical.admin.service.schedule.impl;

import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleBatchCreateRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleCreateRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleResponse;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleUpdateRequest;
import com.neuCloudBrainMedical.admin.entity.doctor.Doctor;
import com.neuCloudBrainMedical.admin.entity.schedule.DoctorSchedule;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.repository.doctor.DoctorRepository;
import com.neuCloudBrainMedical.admin.repository.schedule.ScheduleRepository;
import com.neuCloudBrainMedical.admin.service.schedule.IScheduleCommandService;
import com.neuCloudBrainMedical.admin.service.schedule.IScheduleQueryService;
import com.neuCloudBrainMedical.admin.util.ScheduleTimeSlotUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ScheduleCommandServiceImpl implements IScheduleCommandService {

	private static final String STATUS_ACTIVE = "可预约";
	private static final String SOURCE_MANUAL = "MANUAL";

	private final ScheduleRepository scheduleRepository;
	private final DoctorRepository doctorRepository;
	private final IScheduleQueryService queryService;

	public ScheduleCommandServiceImpl(ScheduleRepository scheduleRepository,
			DoctorRepository doctorRepository,
			IScheduleQueryService queryService) {
		this.scheduleRepository = scheduleRepository;
		this.doctorRepository = doctorRepository;
		this.queryService = queryService;
	}

	@Override
	@Transactional
	public ScheduleResponse createSchedule(ScheduleCreateRequest request) {
		String timeSlot = ScheduleTimeSlotUtils.normalize(request.getTimeSlot());
		request.setTimeSlot(timeSlot);
		validateConflict(request.getDoctorId(), request.getScheduleDate(), timeSlot);
		return createScheduleWithSource(request, SOURCE_MANUAL);
	}

	@Override
	@Transactional
	public List<ScheduleResponse> batchCreateSchedule(ScheduleBatchCreateRequest request) {
		normalizeAndValidateBatch(request.getSchedules());
		return request.getSchedules().stream()
				.map(schedule -> createScheduleWithSource(schedule, SOURCE_MANUAL))
				.toList();
	}

	@Override
	@Transactional
	public ScheduleResponse updateSchedule(Long id, ScheduleUpdateRequest request) {
		DoctorSchedule schedule = scheduleRepository.findById(id)
				.orElseThrow(() -> new BusinessException(404, "排班不存在"));
		if (request.getTimeSlot() != null) {
			String timeSlot = ScheduleTimeSlotUtils.normalize(request.getTimeSlot());
			if (!timeSlot.equals(schedule.getTimePeriod())) {
				validateConflict(schedule.getDoctorId(), schedule.getWorkDate(), timeSlot);
				schedule.setTimePeriod(timeSlot);
				schedule.setStartTime(ScheduleTimeSlotUtils.defaultStartTime(timeSlot));
				schedule.setEndTime(ScheduleTimeSlotUtils.defaultEndTime(timeSlot));
			}
		}

		// 同时处理 maxAppointments 和 currentAppointments，避免校验顺序导致的问题
		int newMax = schedule.getTotalQuota();
		int newCurrent = getCurrentAppointments(schedule);

		if (request.getMaxAppointments() != null) {
			newMax = request.getMaxAppointments();
		}
		if (request.getCurrentAppointments() != null) {
			newCurrent = request.getCurrentAppointments();
		}

		// 1) max 不能为负
		if (newMax < 0) {
			throw new BusinessException(400, "最大接诊量不能为负数");
		}
		// 2) current 不能为负且不能超过 max
		if (newCurrent < 0 || newCurrent > newMax) {
			throw new BusinessException(400, "已预约数量不能为负数且不能超过最大接诊量");
		}

		// 应用更新
		if (request.getMaxAppointments() != null) {
			schedule.setTotalQuota(newMax);
		}
		schedule.setRemainQuota(newMax - newCurrent);

		if (request.getRegistrationFee() != null) {
			schedule.setRegistrationFee(request.getRegistrationFee());
		}
		if (request.getStatus() != null) {
			schedule.setStatus(request.getStatus());
		}
		schedule.setUpdatedAt(LocalDateTime.now());
		return toResponse(scheduleRepository.save(schedule));
	}

	@Override
	@Transactional
	public void cancelSchedule(Long id) {
		DoctorSchedule schedule = scheduleRepository.findById(id)
				.orElseThrow(() -> new BusinessException(404, "排班不存在"));
		if (getCurrentAppointments(schedule) > 0) {
			throw new BusinessException(409, "该排班已有预约，不能删除，请改为停诊或调整接诊量");
		}
		scheduleRepository.delete(schedule);
	}

	DoctorSchedule saveNewSchedule(ScheduleCreateRequest request, String source) {
		LocalDateTime now = LocalDateTime.now();
		DoctorSchedule schedule = new DoctorSchedule();
		schedule.setDoctorId(request.getDoctorId());
		schedule.setDeptId(request.getDepartmentId());
		schedule.setWorkDate(request.getScheduleDate());
		schedule.setTimePeriod(request.getTimeSlot());
		schedule.setTotalQuota(request.getMaxAppointments());
		schedule.setRemainQuota(request.getMaxAppointments());
		schedule.setStartTime(ScheduleTimeSlotUtils.defaultStartTime(request.getTimeSlot()));
		schedule.setEndTime(ScheduleTimeSlotUtils.defaultEndTime(request.getTimeSlot()));
		// 根据医生职称设置默认挂号费
		Doctor doctor = doctorRepository.findById(request.getDoctorId()).orElse(null);
		schedule.setRegistrationFee(ScheduleTimeSlotUtils.defaultFeeByTitle(doctor != null ? doctor.getTitle() : null));
		schedule.setStatus(STATUS_ACTIVE);
		schedule.setSource(source);
		schedule.setCreatedAt(now);
		schedule.setUpdatedAt(now);
		return scheduleRepository.save(schedule);
	}

	void validateConflict(Long doctorId, LocalDate scheduleDate, String timeSlot) {
		if (scheduleRepository.existsByDoctorIdAndWorkDateAndTimePeriodIn(
				doctorId,
				scheduleDate,
				ScheduleTimeSlotUtils.conflictValues(timeSlot))) {
			throw new BusinessException(409, buildConflictMessage(doctorId, scheduleDate, timeSlot));
		}
	}

	void normalizeAndValidateBatch(List<ScheduleCreateRequest> schedules) {
		if (schedules == null || schedules.isEmpty()) {
			throw new BusinessException(400, "请至少添加一条排班");
		}
		Set<String> requestKeys = new HashSet<>();
		for (ScheduleCreateRequest schedule : schedules) {
			String timeSlot = ScheduleTimeSlotUtils.normalize(schedule.getTimeSlot());
			schedule.setTimeSlot(timeSlot);
			String requestKey = buildScheduleKey(schedule.getDoctorId(), schedule.getScheduleDate(), timeSlot);
			if (!requestKeys.add(requestKey)) {
				throw new BusinessException(409, buildConflictMessage(
						schedule.getDoctorId(),
						schedule.getScheduleDate(),
						timeSlot));
			}
			validateConflict(schedule.getDoctorId(), schedule.getScheduleDate(), timeSlot);
		}
	}

	private int getCurrentAppointments(DoctorSchedule schedule) {
		return schedule.getTotalQuota() - schedule.getRemainQuota();
	}

	private String buildScheduleKey(Long doctorId, LocalDate scheduleDate, String timeSlot) {
		return doctorId + "|" + scheduleDate + "|" + ScheduleTimeSlotUtils.normalize(timeSlot);
	}

	private String buildConflictMessage(Long doctorId, LocalDate scheduleDate, String timeSlot) {
		String doctorLabel = doctorRepository.findById(doctorId)
				.map(Doctor::getDoctorNo)
				.orElse("该医生");
		return "排班冲突：" + doctorLabel + "在" + scheduleDate + " " + ScheduleTimeSlotUtils.displayName(timeSlot) + "已安排，不能重复安排";
	}

	ScheduleResponse createScheduleWithSource(ScheduleCreateRequest request, String source) {
		DoctorSchedule schedule = saveNewSchedule(request, source);
		return toResponse(schedule);
	}

	private ScheduleResponse toResponse(DoctorSchedule schedule) {
		return queryService.toResponse(schedule);
	}
}




