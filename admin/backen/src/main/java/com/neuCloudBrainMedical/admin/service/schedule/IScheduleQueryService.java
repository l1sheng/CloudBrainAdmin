package com.neuCloudBrainMedical.admin.service.schedule;

import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleResponse;

import java.time.LocalDate;
import java.util.List;

public interface IScheduleQueryService {

	List<ScheduleResponse> listSchedules(Long departmentId, LocalDate startDate, LocalDate endDate);

	ScheduleResponse getScheduleDetail(Long id);

	/** 将单个 DoctorSchedule 实体转换为 DTO（含医生、科室、用户信息查询）。 */
	ScheduleResponse toResponse(com.neuCloudBrainMedical.admin.entity.schedule.DoctorSchedule schedule);
}





