package com.neuCloudBrainMedical.admin.service.schedule;

import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleBatchCreateRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleCreateRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleResponse;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleUpdateRequest;

import java.util.List;

public interface IScheduleCommandService {

	ScheduleResponse createSchedule(ScheduleCreateRequest request);

	List<ScheduleResponse> batchCreateSchedule(ScheduleBatchCreateRequest request);

	ScheduleResponse updateSchedule(Long id, ScheduleUpdateRequest request);

	void cancelSchedule(Long id);
}





