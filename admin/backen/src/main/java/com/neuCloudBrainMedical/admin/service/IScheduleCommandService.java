package com.neuCloudBrainMedical.admin.service;

import com.neuCloudBrainMedical.admin.dto.ScheduleBatchCreateRequest;
import com.neuCloudBrainMedical.admin.dto.ScheduleCreateRequest;
import com.neuCloudBrainMedical.admin.dto.ScheduleResponse;
import com.neuCloudBrainMedical.admin.dto.ScheduleUpdateRequest;

import java.util.List;

public interface IScheduleCommandService {

	ScheduleResponse createSchedule(ScheduleCreateRequest request);

	List<ScheduleResponse> batchCreateSchedule(ScheduleBatchCreateRequest request);

	ScheduleResponse updateSchedule(Long id, ScheduleUpdateRequest request);

	void cancelSchedule(Long id);
}
