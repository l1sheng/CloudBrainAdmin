package com.neuCloudBrainMedical.admin.service.impl;

import com.neuCloudBrainMedical.admin.dto.DashboardStatisticsResponse;
import com.neuCloudBrainMedical.admin.dto.DepartmentOverviewBrief;
import com.neuCloudBrainMedical.admin.entity.Department;
import com.neuCloudBrainMedical.admin.entity.Doctor;
import com.neuCloudBrainMedical.admin.repository.AiConsultationRepository;
import com.neuCloudBrainMedical.admin.repository.AiScheduleSuggestionRepository;
import com.neuCloudBrainMedical.admin.repository.DepartmentRepository;
import com.neuCloudBrainMedical.admin.repository.DoctorRepository;
import com.neuCloudBrainMedical.admin.repository.RegistrationRepository;
import com.neuCloudBrainMedical.admin.repository.ScheduleRepository;
import com.neuCloudBrainMedical.admin.service.IDashboardStatisticsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardStatisticsServiceImpl implements IDashboardStatisticsService {

	private static final List<String> AVAILABLE_SCHEDULE_STATUSES = List.of("1", "AVAILABLE");
	private static final Integer ENABLED_STATUS = 1;

	private final RegistrationRepository registrationRepository;
	private final ScheduleRepository scheduleRepository;
	private final DepartmentRepository departmentRepository;
	private final DoctorRepository doctorRepository;
	private final AiConsultationRepository aiConsultationRepository;
	private final AiScheduleSuggestionRepository aiScheduleSuggestionRepository;

	public DashboardStatisticsServiceImpl(RegistrationRepository registrationRepository,
			ScheduleRepository scheduleRepository,
			DepartmentRepository departmentRepository,
			DoctorRepository doctorRepository,
			AiConsultationRepository aiConsultationRepository,
			AiScheduleSuggestionRepository aiScheduleSuggestionRepository) {
		this.registrationRepository = registrationRepository;
		this.scheduleRepository = scheduleRepository;
		this.departmentRepository = departmentRepository;
		this.doctorRepository = doctorRepository;
		this.aiConsultationRepository = aiConsultationRepository;
		this.aiScheduleSuggestionRepository = aiScheduleSuggestionRepository;
	}

	@Override
	public DashboardStatisticsResponse getStatistics() {
		LocalDate today = LocalDate.now();
		LocalDateTime startTime = today.atStartOfDay();
		LocalDateTime endTime = today.plusDays(1).atStartOfDay();

		Long todayRegistrationCount = registrationRepository
				.countByRegisteredAtGreaterThanEqualAndRegisteredAtLessThan(startTime, endTime);
		Long todayScheduledDoctorCount = scheduleRepository
				.countDistinctDoctorsByWorkDateAndStatuses(today, AVAILABLE_SCHEDULE_STATUSES);
		List<Department> enabledDepartments = departmentRepository.findByStatus(ENABLED_STATUS);
		List<Doctor> enabledDoctors = doctorRepository.findByStatus(ENABLED_STATUS);
		Map<Long, Long> doctorCountByDepartment = enabledDoctors.stream()
				.collect(Collectors.groupingBy(Doctor::getDeptId, Collectors.counting()));
		Long aiUsageCount = aiConsultationRepository
				.countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndAiResultIsNotNull(startTime, endTime)
				+ aiScheduleSuggestionRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(startTime, endTime);
		List<DepartmentOverviewBrief> departmentOverviews = enabledDepartments.stream()
				.map(department -> new DepartmentOverviewBrief(
						department.getDeptName(),
						doctorCountByDepartment.getOrDefault(department.getDeptId(), 0L)))
				.sorted(Comparator.comparing(DepartmentOverviewBrief::getDoctorCount).reversed()
						.thenComparing(DepartmentOverviewBrief::getDepartmentName))
				.toList();

		DashboardStatisticsResponse response = new DashboardStatisticsResponse();
		response.setTodayRegistrationCount(todayRegistrationCount);
		response.setActiveDepartmentCount((long) enabledDepartments.size());
		response.setTodayScheduledDoctorCount(todayScheduledDoctorCount);
		response.setAiUsageCount(aiUsageCount);
		response.setDepartmentOverviews(departmentOverviews);
		return response;
	}
}