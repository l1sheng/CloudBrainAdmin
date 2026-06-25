package com.neuCloudBrainMedical.admin.controller.schedule;

import com.neuCloudBrainMedical.admin.dto.schedule.AIScheduleSuggestRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.AIScheduleSuggestionResponse;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleResponse;
import com.neuCloudBrainMedical.admin.service.schedule.IAIScheduleService;
import com.neuCloudBrainMedical.admin.util.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 排班建议接口。
 * 资源路径：/api/admin/schedules/ai-suggestions
 */
@RestController
@RequestMapping("/api/admin/schedules/ai-suggestions")
public class AIScheduleSuggestionController {

	private final IAIScheduleService aiScheduleService;

	public AIScheduleSuggestionController(IAIScheduleService aiScheduleService) {
		this.aiScheduleService = aiScheduleService;
	}

	@PostMapping
	public Result<AIScheduleSuggestionResponse> generateSuggestion(@Valid @RequestBody AIScheduleSuggestRequest request) {
		return Result.success(aiScheduleService.generateSuggestion(request));
	}

	@PostMapping("/{suggestionId}/accept")
	public Result<List<ScheduleResponse>> acceptSuggestion(@PathVariable Long suggestionId) {
		return Result.success(aiScheduleService.acceptSuggestion(suggestionId));
	}

	@PostMapping("/{suggestionId}/reject")
	public Result<Void> rejectSuggestion(@PathVariable Long suggestionId) {
		aiScheduleService.rejectSuggestion(suggestionId);
		return Result.success();
	}

	@PostMapping("/{suggestionId}/details/{detailId}/accept")
	public Result<ScheduleResponse> acceptSuggestionDetail(@PathVariable Long suggestionId, @PathVariable Long detailId) {
		return Result.success(aiScheduleService.acceptSuggestionDetail(suggestionId, detailId));
	}

	@PostMapping("/{suggestionId}/details/{detailId}/reject")
	public Result<Void> rejectSuggestionDetail(@PathVariable Long suggestionId, @PathVariable Long detailId) {
		aiScheduleService.rejectSuggestionDetail(suggestionId, detailId);
		return Result.success();
	}
}




