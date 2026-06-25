package com.neuCloudBrainMedical.admin.service.schedule;

import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleRegistrationResponse;

import java.util.List;

/**
 * 排班下挂号记录的查询服务。
 * 单一职责：只负责"某个排班 → 挂号列表"的查询与组装。
 */
public interface IScheduleRegistrationQueryService {

	List<ScheduleRegistrationResponse> listByScheduleId(Long scheduleId);
}