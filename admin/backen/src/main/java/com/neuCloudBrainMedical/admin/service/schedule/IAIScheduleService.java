package com.neuCloudBrainMedical.admin.service.schedule;

import com.neuCloudBrainMedical.admin.dto.schedule.AIScheduleSuggestRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.AIScheduleSuggestionResponse;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleResponse;

import java.util.List;

public interface IAIScheduleService {

	AIScheduleSuggestionResponse generateSuggestion(AIScheduleSuggestRequest request);

	List<ScheduleResponse> acceptSuggestion(Long suggestionId);

	void rejectSuggestion(Long suggestionId);

	ScheduleResponse acceptSuggestionDetail(Long suggestionId, Long detailId);

	void rejectSuggestionDetail(Long suggestionId, Long detailId);
}





