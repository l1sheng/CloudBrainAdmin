package com.neuCloudBrainMedical.admin.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuCloudBrainMedical.admin.dto.AIScheduleSuggestRequest;
import com.neuCloudBrainMedical.admin.dto.AIScheduleSuggestionResponse;
import com.neuCloudBrainMedical.admin.dto.DoctorInfo;
import com.neuCloudBrainMedical.admin.dto.ScheduleResponse;
import com.neuCloudBrainMedical.admin.dto.SuggestionDetailResponse;
import com.neuCloudBrainMedical.admin.entity.AiScheduleSuggestion;
import com.neuCloudBrainMedical.admin.entity.AiScheduleSuggestionDetail;
import com.neuCloudBrainMedical.admin.entity.Department;
import com.neuCloudBrainMedical.admin.entity.Doctor;
import com.neuCloudBrainMedical.admin.entity.DoctorSchedule;
import com.neuCloudBrainMedical.admin.exception.AIServiceException;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.repository.AiScheduleSuggestionDetailRepository;
import com.neuCloudBrainMedical.admin.repository.AiScheduleSuggestionRepository;
import com.neuCloudBrainMedical.admin.repository.DepartmentRepository;
import com.neuCloudBrainMedical.admin.repository.DoctorRepository;
import com.neuCloudBrainMedical.admin.repository.ScheduleRepository;
import com.neuCloudBrainMedical.admin.service.IAIScheduleService;
import com.neuCloudBrainMedical.admin.service.IAISchedulingClient;
import com.neuCloudBrainMedical.admin.util.ScheduleTimeSlotUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AIScheduleServiceImpl implements IAIScheduleService {

	private static final String STATUS_PENDING = "PENDING";
	private static final String STATUS_ACCEPTED = "ACCEPTED";
	private static final String STATUS_REJECTED = "REJECTED";
	private static final String STATUS_AVAILABLE = "AVAILABLE";
	private static final String SOURCE_AI = "AI_SUGGESTED";
	private static final int ENABLED_DOCTOR_STATUS = 1;

	private final IAISchedulingClient aiSchedulingClient;
	private final DoctorRepository doctorRepository;
	private final DepartmentRepository departmentRepository;
	private final ScheduleRepository scheduleRepository;
	private final AiScheduleSuggestionRepository suggestionRepository;
	private final AiScheduleSuggestionDetailRepository detailRepository;
	private final ScheduleMapper scheduleMapper;
	private final ObjectMapper objectMapper;

	public AIScheduleServiceImpl(IAISchedulingClient aiSchedulingClient,
			DoctorRepository doctorRepository,
			DepartmentRepository departmentRepository,
			ScheduleRepository scheduleRepository,
			AiScheduleSuggestionRepository suggestionRepository,
			AiScheduleSuggestionDetailRepository detailRepository,
			ScheduleMapper scheduleMapper,
			ObjectMapper objectMapper) {
		this.aiSchedulingClient = aiSchedulingClient;
		this.doctorRepository = doctorRepository;
		this.departmentRepository = departmentRepository;
		this.scheduleRepository = scheduleRepository;
		this.suggestionRepository = suggestionRepository;
		this.detailRepository = detailRepository;
		this.scheduleMapper = scheduleMapper;
		this.objectMapper = objectMapper;
	}

	@Override
	@Transactional
	public AIScheduleSuggestionResponse generateSuggestion(AIScheduleSuggestRequest request) {
		List<Doctor> doctors = doctorRepository.findByDeptIdAndStatusOrderByDoctorIdAsc(
				request.getDepartmentId(), ENABLED_DOCTOR_STATUS);
		List<DoctorInfo> doctorInfos = doctors.stream().map(this::toDoctorInfo).toList();
		String rawJson = aiSchedulingClient.requestSchedulingSuggestion(
				request.getDepartmentId(),
				request.getStartDate(),
				request.getEndDate(),
				doctorInfos,
				"近4周门诊量统计暂未接入，按医生基础信息和排班规则生成建议。");
		List<AiScheduleSuggestionDetail> details = parseDetails(rawJson, null);

		AiScheduleSuggestion suggestion = createSuggestion(request, details);
		Long suggestionId = suggestion.getSuggestionId();
		details.forEach(detail -> {
			detail.setSuggestionId(suggestionId);
			detail.setStatus(STATUS_PENDING);
		});
		detailRepository.saveAll(details);
		return toSuggestionResponse(suggestion, details, request.getStartDate(), request.getEndDate());
	}

	@Override
	@Transactional
	public List<ScheduleResponse> acceptSuggestion(Long suggestionId) {
		AiScheduleSuggestion suggestion = findPendingSuggestion(suggestionId);
		List<AiScheduleSuggestionDetail> details = detailRepository.findBySuggestionIdAndStatus(suggestionId, STATUS_PENDING);
		List<ScheduleResponse> schedules = details.stream()
				.map(detail -> acceptDetail(suggestion, detail))
				.toList();
		updateSuggestionStatusIfCompleted(suggestion);
		return schedules;
	}

	@Override
	@Transactional
	public void rejectSuggestion(Long suggestionId) {
		AiScheduleSuggestion suggestion = findPendingSuggestion(suggestionId);
		List<AiScheduleSuggestionDetail> details = detailRepository.findBySuggestionIdAndStatus(suggestionId, STATUS_PENDING);
		details.forEach(detail -> detail.setStatus(STATUS_REJECTED));
		detailRepository.saveAll(details);
		updateSuggestionStatusIfCompleted(suggestion);
	}

	@Override
	@Transactional
	public ScheduleResponse acceptSuggestionDetail(Long suggestionId, Long detailId) {
		AiScheduleSuggestion suggestion = findPendingSuggestion(suggestionId);
		AiScheduleSuggestionDetail detail = findPendingDetail(suggestionId, detailId);
		ScheduleResponse response = acceptDetail(suggestion, detail);
		updateSuggestionStatusIfCompleted(suggestion);
		return response;
	}

	@Override
	@Transactional
	public void rejectSuggestionDetail(Long suggestionId, Long detailId) {
		AiScheduleSuggestion suggestion = findPendingSuggestion(suggestionId);
		AiScheduleSuggestionDetail detail = findPendingDetail(suggestionId, detailId);
		detail.setStatus(STATUS_REJECTED);
		detailRepository.save(detail);
		updateSuggestionStatusIfCompleted(suggestion);
	}

	private AiScheduleSuggestion findPendingSuggestion(Long suggestionId) {
		return suggestionRepository.findBySuggestionIdAndStatus(suggestionId, STATUS_PENDING)
				.orElseThrow(() -> new BusinessException(404, "AI 排班建议不存在或已处理"));
	}

	private AiScheduleSuggestion createSuggestion(AIScheduleSuggestRequest request, List<AiScheduleSuggestionDetail> details) {
		AiScheduleSuggestion suggestion = new AiScheduleSuggestion();
		suggestion.setDeptId(request.getDepartmentId());
		suggestion.setWorkDate(request.getStartDate());
		suggestion.setTimePeriod("AI_RANGE");
		suggestion.setSuggestionReason(buildSuggestionSummary(details));
		suggestion.setStatus(STATUS_PENDING);
		suggestion.setCreatedAt(LocalDateTime.now());
		return suggestionRepository.save(suggestion);
	}

	private DoctorInfo toDoctorInfo(Doctor doctor) {
		DoctorInfo info = new DoctorInfo();
		info.setDoctorId(doctor.getDoctorId());
		info.setDoctorName(doctor.getDoctorNo());
		info.setTitle(doctor.getTitle());
		info.setSpecialty(doctor.getSpecialty());
		info.setHistoricalWorkDays(0L);
		return info;
	}

	private void validateConflict(Long doctorId, LocalDate scheduleDate, String timeSlot) {
		if (scheduleRepository.existsByDoctorIdAndWorkDateAndTimePeriodIn(
				doctorId,
				scheduleDate,
				ScheduleTimeSlotUtils.conflictValues(timeSlot))) {
			String doctorLabel = doctorRepository.findById(doctorId)
					.map(Doctor::getDoctorNo)
					.orElse("该医生");
			throw new BusinessException(409, "排班冲突：" + doctorLabel + "在" + scheduleDate + " "
					+ ScheduleTimeSlotUtils.displayName(timeSlot) + "已安排，不能重复安排");
		}
	}

	private DoctorSchedule createScheduleFromDetail(AiScheduleSuggestion suggestion, AiScheduleSuggestionDetail detail) {
		LocalDateTime now = LocalDateTime.now();
		DoctorSchedule schedule = new DoctorSchedule();
		schedule.setDoctorId(detail.getDoctorId());
		schedule.setDeptId(suggestion.getDeptId());
		schedule.setWorkDate(detail.getScheduleDate());
		schedule.setTimePeriod(detail.getTimeSlot());
		schedule.setTotalQuota(detail.getMaxAppointments());
		schedule.setRemainQuota(detail.getMaxAppointments());
		schedule.setRegistrationFee(BigDecimal.ZERO);
		schedule.setStatus(STATUS_AVAILABLE);
		schedule.setSource(SOURCE_AI);
		schedule.setCreatedAt(now);
		schedule.setUpdatedAt(now);
		return scheduleRepository.save(schedule);
	}

	private ScheduleResponse acceptDetail(AiScheduleSuggestion suggestion, AiScheduleSuggestionDetail detail) {
		detail.setTimeSlot(ScheduleTimeSlotUtils.normalize(detail.getTimeSlot()));
		validateConflict(detail.getDoctorId(), detail.getScheduleDate(), detail.getTimeSlot());
		DoctorSchedule schedule = createScheduleFromDetail(suggestion, detail);
		detail.setStatus(STATUS_ACCEPTED);
		detailRepository.save(detail);
		return toScheduleResponse(schedule);
	}

	private AiScheduleSuggestionDetail findPendingDetail(Long suggestionId, Long detailId) {
		AiScheduleSuggestionDetail detail = detailRepository.findById(detailId)
				.orElseThrow(() -> new BusinessException(404, "AI 排班建议明细不存在"));
		if (!suggestionId.equals(detail.getSuggestionId())) {
			throw new BusinessException(404, "AI 排班建议明细不存在");
		}
		if (!STATUS_PENDING.equals(detail.getStatus())) {
			throw new BusinessException(409, "该 AI 排班建议明细已处理");
		}
		return detail;
	}

	private void updateSuggestionStatusIfCompleted(AiScheduleSuggestion suggestion) {
		List<AiScheduleSuggestionDetail> pendingDetails = detailRepository.findBySuggestionIdAndStatus(
				suggestion.getSuggestionId(), STATUS_PENDING);
		if (!pendingDetails.isEmpty()) {
			return;
		}
		List<AiScheduleSuggestionDetail> acceptedDetails = detailRepository.findBySuggestionIdAndStatus(
				suggestion.getSuggestionId(), STATUS_ACCEPTED);
		suggestion.setStatus(acceptedDetails.isEmpty() ? STATUS_REJECTED : STATUS_ACCEPTED);
		suggestion.setConfirmedAt(LocalDateTime.now());
		suggestionRepository.save(suggestion);
	}

	private ScheduleResponse toScheduleResponse(DoctorSchedule schedule) {
		Doctor doctor = doctorRepository.findById(schedule.getDoctorId()).orElse(null);
		Department department = departmentRepository.findById(schedule.getDeptId()).orElse(null);
		return scheduleMapper.toResponse(schedule,
				doctor == null ? Map.of() : Map.of(doctor.getDoctorId(), doctor),
				department == null ? Map.of() : Map.of(department.getDeptId(), department));
	}

	private List<AiScheduleSuggestionDetail> parseDetails(String rawJson, Long suggestionId) {
		try {
			JsonNode root = objectMapper.readTree(rawJson);
			if (!root.isArray()) {
				throw new AIServiceException("AI 返回格式无法解析，请使用手动排班功能");
			}
			List<JsonNode> nodes = objectMapper.convertValue(
					root,
					objectMapper.getTypeFactory().constructCollectionType(List.class, JsonNode.class));
			return nodes.stream()
					.map(node -> toDetail(node, suggestionId))
					.toList();
		} catch (AIServiceException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new AIServiceException("AI 返回格式无法解析，请使用手动排班功能");
		}
	}

	private AiScheduleSuggestionDetail toDetail(JsonNode node, Long suggestionId) {
		AiScheduleSuggestionDetail detail = new AiScheduleSuggestionDetail();
		detail.setSuggestionId(suggestionId);
		detail.setDoctorId(node.path("doctorId").asLong());
		detail.setDoctorName(node.path("doctorName").asText());
		detail.setScheduleDate(LocalDate.parse(node.path("date").asText()));
		detail.setTimeSlot(ScheduleTimeSlotUtils.normalize(node.path("timeSlot").asText()));
		detail.setMaxAppointments(node.path("maxAppointments").asInt());
		detail.setReason(node.path("reason").asText());
		detail.setStatus(STATUS_PENDING);
		return detail;
	}

	private String buildSuggestionSummary(List<AiScheduleSuggestionDetail> details) {
		if (details == null || details.isEmpty()) {
			return "AI 已生成排班建议";
		}
		return "AI 已生成" + details.size() + "条排班建议";
	}

	private AIScheduleSuggestionResponse toSuggestionResponse(AiScheduleSuggestion suggestion,
			List<AiScheduleSuggestionDetail> details,
			LocalDate startDate,
			LocalDate endDate) {
		AIScheduleSuggestionResponse response = new AIScheduleSuggestionResponse();
		response.setSuggestionId(suggestion.getSuggestionId());
		response.setDepartmentId(suggestion.getDeptId());
		response.setStartDate(startDate);
		response.setEndDate(endDate);
		response.setStatus(suggestion.getStatus());
		response.setDetails(details.stream().map(this::toDetailResponse).toList());
		return response;
	}

	private SuggestionDetailResponse toDetailResponse(AiScheduleSuggestionDetail detail) {
		SuggestionDetailResponse response = new SuggestionDetailResponse();
		response.setDetailId(detail.getDetailId());
		response.setDoctorId(detail.getDoctorId());
		response.setDoctorName(detail.getDoctorName());
		response.setScheduleDate(detail.getScheduleDate());
		response.setTimeSlot(detail.getTimeSlot());
		response.setMaxAppointments(detail.getMaxAppointments());
		response.setReason(detail.getReason());
		response.setStatus(detail.getStatus());
		return response;
	}
}