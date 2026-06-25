package com.neuCloudBrainMedical.admin.service.impl;

import com.neuCloudBrainMedical.admin.dto.ScheduleResponse;
import com.neuCloudBrainMedical.admin.entity.Department;
import com.neuCloudBrainMedical.admin.entity.Doctor;
import com.neuCloudBrainMedical.admin.entity.DoctorSchedule;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ScheduleMapper {

	public ScheduleResponse toResponse(DoctorSchedule schedule,
	                                    Map<Long, Doctor> doctors,
	                                    Map<Long, Department> departments) {
		return toResponse(schedule, doctors, departments, null);
	}

	/**
	 * 将 DoctorSchedule 转换为 ScheduleResponse。
	 *
	 * @param schedule           排班实体
	 * @param doctors            医生 id -> 医生实体 的映射
	 * @param departments        科室 id -> 科室实体 的映射
	 * @param registrationCounts 排班 id -> 真实挂号数 的映射（可为 null）
	 */
	public ScheduleResponse toResponse(DoctorSchedule schedule,
	                                    Map<Long, Doctor> doctors,
	                                    Map<Long, Department> departments,
	                                    Map<Long, Integer> registrationCounts) {
		Doctor doctor = doctors.get(schedule.getDoctorId());
		Department department = departments.get(schedule.getDeptId());

		// 优先使用从 registration 表统计的真实挂号数，
		// 若没有统计数据，则回退为 total_quota - remain_quota
		int realCount;
		if (registrationCounts != null && registrationCounts.containsKey(schedule.getScheduleId())) {
			realCount = registrationCounts.get(schedule.getScheduleId());
		} else {
			realCount = schedule.getTotalQuota() - schedule.getRemainQuota();
		}

		ScheduleResponse response = new ScheduleResponse();
		response.setId(schedule.getScheduleId());
		response.setDoctorId(schedule.getDoctorId());
		response.setDoctorName(doctor != null ? doctor.getDoctorName() : "");
		response.setDoctorNo(doctor != null ? doctor.getDoctorNo() : "");
		response.setDoctorType(doctor != null ? doctor.getDoctorType() : "");
		response.setTitle(doctor != null ? doctor.getTitle() : "");
		response.setSpecialty(doctor != null ? doctor.getSpecialty() : "");
		response.setDepartmentId(schedule.getDeptId());
		response.setDepartmentName(department != null ? department.getDeptName() : "");
		response.setScheduleDate(schedule.getWorkDate());
		response.setTimeSlot(schedule.getTimePeriod());
		response.setMaxAppointments(schedule.getTotalQuota());
		response.setCurrentAppointments(realCount);
		response.setSource(schedule.getSource());
		response.setStatus(schedule.getStatus());
		return response;
	}
}