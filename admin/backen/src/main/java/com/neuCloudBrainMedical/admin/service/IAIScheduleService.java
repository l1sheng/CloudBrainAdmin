package com.neuCloudBrainMedical.admin.service;

import com.neuCloudBrainMedical.admin.dto.AIScheduleSuggestRequest;
import com.neuCloudBrainMedical.admin.dto.AIScheduleSuggestionResponse;
import com.neuCloudBrainMedical.admin.dto.ScheduleResponse;

import java.util.List;

public interface IAIScheduleService {

	AIScheduleSuggestionResponse generateSuggestion(AIScheduleSuggestRequest request);

	List<ScheduleResponse> acceptSuggestion(Long suggestionId);

	void rejectSuggestion(Long suggestionId);

	ScheduleResponse acceptSuggestionDetail(Long suggestionId, Long detailId);

	void rejectSuggestionDetail(Long suggestionId, Long detailId);
}
