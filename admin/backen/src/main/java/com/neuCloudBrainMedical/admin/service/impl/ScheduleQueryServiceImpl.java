package com.neuCloudBrainMedical.admin.service.impl;

import com.neuCloudBrainMedical.admin.dto.ScheduleResponse;
import com.neuCloudBrainMedical.admin.entity.Department;
import com.neuCloudBrainMedical.admin.entity.Doctor;
import com.neuCloudBrainMedical.admin.entity.DoctorSchedule;
import com.neuCloudBrainMedical.admin.entity.SysUser;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.repository.DepartmentRepository;
import com.neuCloudBrainMedical.admin.repository.DoctorRepository;
import com.neuCloudBrainMedical.admin.repository.RegistrationRepository;
import com.neuCloudBrainMedical.admin.repository.ScheduleRepository;
import com.neuCloudBrainMedical.admin.repository.SysUserRepository;
import com.neuCloudBrainMedical.admin.service.IScheduleQueryService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ScheduleQueryServiceImpl implements IScheduleQueryService {

	private final ScheduleRepository scheduleRepository;
	private final DoctorRepository doctorRepository;
	private final DepartmentRepository departmentRepository;
	private final SysUserRepository sysUserRepository;
	private final RegistrationRepository registrationRepository;
	private final ScheduleMapper scheduleMapper;

	public ScheduleQueryServiceImpl(ScheduleRepository scheduleRepository,
			DoctorRepository doctorRepository,
			DepartmentRepository departmentRepository,
			SysUserRepository sysUserRepository,
			RegistrationRepository registrationRepository,
			ScheduleMapper scheduleMapper) {
		this.scheduleRepository = scheduleRepository;
		this.doctorRepository = doctorRepository;
		this.departmentRepository = departmentRepository;
		this.sysUserRepository = sysUserRepository;
		this.registrationRepository = registrationRepository;
		this.scheduleMapper = scheduleMapper;
	}

	@Override
	public List<ScheduleResponse> listSchedules(Long departmentId, LocalDate startDate, LocalDate endDate) {
		List<DoctorSchedule> schedules = scheduleRepository.findSchedulesForAdmin(departmentId, startDate, endDate);
		return toResponses(schedules);
	}

	@Override
	public ScheduleResponse getScheduleDetail(Long id) {
		DoctorSchedule schedule = scheduleRepository.findById(id)
				.orElseThrow(() -> new BusinessException(404, "排班不存在"));
		return toResponses(List.of(schedule)).get(0);
	}

	private List<ScheduleResponse> toResponses(List<DoctorSchedule> schedules) {
		if (schedules.isEmpty()) {
			return List.of();
		}

		Set<Long> scheduleIds = schedules.stream()
				.map(DoctorSchedule::getScheduleId)
				.collect(Collectors.toSet());

		// 1) 查询关联医生
		List<Doctor> doctors = doctorRepository.findAllById(
				schedules.stream().map(DoctorSchedule::getDoctorId).collect(Collectors.toSet()));
		Map<Long, Doctor> doctorsById = doctors.stream()
				.collect(Collectors.toMap(Doctor::getDoctorId, Function.identity()));

		// 2) 查询关联科室
		Map<Long, Department> departments = departmentRepository.findAllById(
						schedules.stream().map(DoctorSchedule::getDeptId).collect(Collectors.toSet()))
				.stream()
				.collect(Collectors.toMap(Department::getDeptId, Function.identity()));

		// 3) 通过医生 user_id 查询 sys_user，拿到 real_name 作为医生姓名（避免冗余字段）
		Set<Long> userIds = doctors.stream()
				.map(Doctor::getUserId)
				.collect(Collectors.toSet());
		Map<Long, SysUser> usersByUserId = sysUserRepository.findAllById(userIds)
				.stream()
				.collect(Collectors.toMap(SysUser::getUserId, Function.identity()));

		// 4) 查询每个排班的真实挂号数（从 registration 表统计）
		Map<Long, Integer> registrationCounts = new HashMap<>();
		List<Object[]> counts = registrationRepository.countByScheduleIdIn(scheduleIds);
		if (counts != null) {
			for (Object[] row : counts) {
				Long sid = (Long) row[0];
				Long cnt = (Long) row[1];
				registrationCounts.put(sid, cnt != null ? cnt.intValue() : 0);
			}
		}

		return schedules.stream()
				.map(schedule -> scheduleMapper.toResponse(schedule, doctorsById, departments, usersByUserId, registrationCounts))
				.collect(Collectors.toList());
	}
}