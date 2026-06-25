package com.neuCloudBrainMedical.admin.service.schedule;

import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleResponse;

import java.time.LocalDate;
import java.util.List;

public interface IScheduleQueryService {

	List<ScheduleResponse> listSchedules(Long departmentId, LocalDate startDate, LocalDate endDate);

	ScheduleResponse getScheduleDetail(Long id);
}





