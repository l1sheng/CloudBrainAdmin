package com.neuCloudBrainMedical.admin.service.schedule.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuCloudBrainMedical.admin.dto.schedule.AIScheduleSuggestRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.AIScheduleSuggestionResponse;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorInfo;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleResponse;
import com.neuCloudBrainMedical.admin.dto.schedule.SuggestionDetailResponse;
import com.neuCloudBrainMedical.admin.entity.schedule.AiScheduleSuggestion;
import com.neuCloudBrainMedical.admin.entity.schedule.AiScheduleSuggestionDetail;
import com.neuCloudBrainMedical.admin.entity.doctor.Doctor;
import com.neuCloudBrainMedical.admin.entity.schedule.DoctorSchedule;
import com.neuCloudBrainMedical.admin.entity.SysUser;
import com.neuCloudBrainMedical.admin.exception.AIServiceException;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.repository.schedule.AiScheduleSuggestionDetailRepository;
import com.neuCloudBrainMedical.admin.repository.schedule.AiScheduleSuggestionRepository;
import com.neuCloudBrainMedical.admin.repository.doctor.DoctorRepository;
import com.neuCloudBrainMedical.admin.repository.schedule.ScheduleRepository;
import com.neuCloudBrainMedical.admin.repository.SysUserRepository;
import com.neuCloudBrainMedical.admin.service.schedule.IAIScheduleService;
import com.neuCloudBrainMedical.admin.service.schedule.IAISchedulingClient;
import com.neuCloudBrainMedical.admin.service.schedule.IScheduleQueryService;
import com.neuCloudBrainMedical.admin.util.ScheduleTimeSlotUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AIScheduleServiceImpl implements IAIScheduleService {

	private static final String STATUS_PENDING = "PENDING";
	private static final String STATUS_ACCEPTED = "ACCEPTED";
	private static final String STATUS_REJECTED = "REJECTED";
	private static final String SUGGESTION_PENDING = "待确认";
	private static final String SUGGESTION_ACCEPTED = "已确认";
	private static final String SUGGESTION_REJECTED = "已忽略";
	private static final String STATUS_AVAILABLE = "可预约";
	private static final String SOURCE_AI = "AI_SUGGESTED";
	private static final int ENABLED_DOCTOR_STATUS = 1;

	private final IAISchedulingClient aiSchedulingClient;
	private final DoctorRepository doctorRepository;
	private final SysUserRepository sysUserRepository;
	private final ScheduleRepository scheduleRepository;
	private final AiScheduleSuggestionRepository suggestionRepository;
	private final AiScheduleSuggestionDetailRepository detailRepository;
	private final IScheduleQueryService queryService;
	private final ObjectMapper objectMapper;

	public AIScheduleServiceImpl(IAISchedulingClient aiSchedulingClient,
			DoctorRepository doctorRepository,
			SysUserRepository sysUserRepository,
			ScheduleRepository scheduleRepository,
			AiScheduleSuggestionRepository suggestionRepository,
			AiScheduleSuggestionDetailRepository detailRepository,
			IScheduleQueryService queryService,
			ObjectMapper objectMapper) {
		this.aiSchedulingClient = aiSchedulingClient;
		this.doctorRepository = doctorRepository;
		this.sysUserRepository = sysUserRepository;
		this.scheduleRepository = scheduleRepository;
		this.suggestionRepository = suggestionRepository;
		this.detailRepository = detailRepository;
		this.queryService = queryService;
		this.objectMapper = objectMapper;
	}

	@Override
	@Transactional
	public AIScheduleSuggestionResponse generateSuggestion(AIScheduleSuggestRequest request) {
		List<Doctor> doctors = doctorRepository.findByDeptIdAndStatusOrderByDoctorIdAsc(
				request.getDepartmentId(), ENABLED_DOCTOR_STATUS);

		// 通过 user_id 关联 sys_user.real_name 作为医生姓名（去掉 doctor 表冗余字段 doctor_name）
		Map<Long, SysUser> usersByUserId = Map.of();
		if (!doctors.isEmpty()) {
			Set<Long> userIds = doctors.stream()
					.map(Doctor::getUserId)
					.collect(Collectors.toSet());
			usersByUserId = sysUserRepository.findAllById(userIds)
					.stream()
					.collect(Collectors.toMap(SysUser::getUserId, Function.identity()));
		}

		Map<Long, SysUser> usersFinal = usersByUserId;
		List<DoctorInfo> doctorInfos = doctors.stream()
				.map(doctor -> toDoctorInfo(doctor, usersFinal.get(doctor.getUserId())))
				.toList();

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
		return suggestionRepository.findBySuggestionIdAndStatus(suggestionId, SUGGESTION_PENDING)
				.orElseThrow(() -> new BusinessException(404, "AI 排班建议不存在或已处理"));
	}

	private AiScheduleSuggestion createSuggestion(AIScheduleSuggestRequest request, List<AiScheduleSuggestionDetail> details) {
		AiScheduleSuggestion suggestion = new AiScheduleSuggestion();
		suggestion.setDeptId(request.getDepartmentId());
		suggestion.setWorkDate(request.getStartDate());
		suggestion.setTimePeriod("AI_RANGE");
		suggestion.setSuggestionReason(buildSuggestionSummary(details));
		suggestion.setStatus(SUGGESTION_PENDING);
		suggestion.setCreatedAt(LocalDateTime.now());
		return suggestionRepository.save(suggestion);
	}

	private DoctorInfo toDoctorInfo(Doctor doctor, SysUser user) {
		DoctorInfo info = new DoctorInfo();
		info.setDoctorId(doctor.getDoctorId());
		// 医生姓名从关联用户 real_name 取；若 user 缺失，回退为空字符串
		info.setDoctorName(user != null ? user.getRealName() : "");
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
		schedule.setStartTime(ScheduleTimeSlotUtils.defaultStartTime(detail.getTimeSlot()));
		schedule.setEndTime(ScheduleTimeSlotUtils.defaultEndTime(detail.getTimeSlot()));
		// 根据医生职称设置默认挂号费
		Doctor scheduleDoctor = doctorRepository.findById(detail.getDoctorId()).orElse(null);
		schedule.setRegistrationFee(ScheduleTimeSlotUtils.defaultFeeByTitle(scheduleDoctor != null ? scheduleDoctor.getTitle() : null));
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
		suggestion.setStatus(acceptedDetails.isEmpty() ? SUGGESTION_REJECTED : SUGGESTION_ACCEPTED);
		suggestion.setConfirmedAt(LocalDateTime.now());
		suggestionRepository.save(suggestion);
	}

	private ScheduleResponse toScheduleResponse(DoctorSchedule schedule) {
		return queryService.toResponse(schedule);
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




