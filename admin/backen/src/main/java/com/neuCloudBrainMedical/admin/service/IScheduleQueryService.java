package com.neuCloudBrainMedical.admin.service;

import com.neuCloudBrainMedical.admin.dto.ScheduleResponse;

import java.time.LocalDate;
import java.util.List;

public interface IScheduleQueryService {

	List<ScheduleResponse> listSchedules(Long departmentId, LocalDate startDate, LocalDate endDate);

	ScheduleResponse getScheduleDetail(Long id);
}
