package com.neuCloudBrainMedical.admin.service.schedule.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorInfo;
import com.neuCloudBrainMedical.admin.exception.AIServiceException;
import com.neuCloudBrainMedical.admin.service.schedule.IAISchedulingClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DefaultAISchedulingClient implements IAISchedulingClient {

	private final RestTemplate aiRestTemplate;
	private final ObjectMapper objectMapper;

	@Value("${ai.deepseek.base-url:https://api.deepseek.com}")
	private String baseUrl;

	@Value("${ai.deepseek.api-key:}")
	private String apiKey;

	@Value("${ai.deepseek.model:deepseek-v4-flash}")
	private String model;

	public DefaultAISchedulingClient(RestTemplate aiRestTemplate, ObjectMapper objectMapper) {
		this.aiRestTemplate = aiRestTemplate;
		this.objectMapper = objectMapper;
	}

	@Override
	public String requestSchedulingSuggestion(Long departmentId,
			LocalDate startDate,
			LocalDate endDate,
			List<DoctorInfo> doctorList,
			String contextInfo) {
		if (apiKey == null || apiKey.isBlank()) {
			throw new AIServiceException("AI 排班服务未配置密钥，请先配置 DEEPSEEK_API_KEY");
		}
		String prompt = buildPrompt(departmentId, startDate, endDate, doctorList, contextInfo);
		try {
			Map<String, Object> requestBody = Map.of(
					"model", model,
					"temperature", 0.2,
					"messages", List.of(
							Map.of("role", "system", "content", buildSystemPrompt()),
							Map.of("role", "user", "content", prompt)));
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBearerAuth(apiKey);
			String response = aiRestTemplate.postForObject(
					baseUrl.replaceAll("/+$", "") + "/chat/completions",
					new HttpEntity<>(requestBody, headers),
					String.class);
			return extractJsonFromChatResponse(response);
		} catch (HttpStatusCodeException exception) {
			throw new AIServiceException(buildDeepSeekErrorMessage(exception));
		} catch (ResourceAccessException exception) {
			throw new AIServiceException("AI 排班服务连接超时或网络不可达，请检查网络和 DeepSeek 地址配置");
		} catch (RestClientException exception) {
			throw new AIServiceException("AI 排班服务请求失败：" + exception.getMessage());
		}
	}

	private String buildDeepSeekErrorMessage(HttpStatusCodeException exception) {
		String responseBody = exception.getResponseBodyAsString();
		String detail = responseBody == null || responseBody.isBlank()
				? exception.getStatusText()
				: responseBody;
		return "AI 排班服务调用失败：HTTP " + exception.getStatusCode().value() + "，" + detail;
	}

	private String buildSystemPrompt() {
		return """
				你是医院管理员端的 AI 排班助手。
				你只能返回 JSON 数组，不要返回 Markdown、解释文字或代码块。
				字段必须为：doctorId, doctorName, date, timeSlot, maxAppointments, reason。
				timeSlot 只能使用 中文值：上午、下午、夜间。
				date 必须使用 yyyy-MM-dd。
				maxAppointments 必须是正整数。
				""";
	}

	private String buildPrompt(Long departmentId,
			LocalDate startDate,
			LocalDate endDate,
			List<DoctorInfo> doctorList,
			String contextInfo) {
		String doctors = doctorList.stream()
				.map(doctor -> """
						- doctorId=%s, doctorName=%s, title=%s, specialty=%s, historicalWorkDays=%s
						""".formatted(doctor.getDoctorId(), doctor.getDoctorName(), doctor.getTitle(),
						doctor.getSpecialty(), doctor.getHistoricalWorkDays()))
				.collect(Collectors.joining());
		return """
				请为医院科室生成医生排班建议。
				科室ID：%s
				日期范围：%s 至 %s

				医生列表：
				%s

				上下文信息：
				%s

				排班规则：
				1. 同一医生同一天最多安排 1 个时段。
				2. 同一医生每周排班不超过 5 天。
				3. 优先让专长匹配的医生覆盖需求高的时段。
				4. 不要安排医生列表之外的医生。
				5. 返回 JSON 数组，例如：
				[
				  {
				    "doctorId": 1,
				    "doctorName": "D20260001",
				    "date": "2026-06-23",
				    "timeSlot": "上午",
				    "maxAppointments": 30,
				    "reason": "上午需求较高，医生专长匹配"
				  }
				]
				""".formatted(departmentId, startDate, endDate, doctors, contextInfo);
	}

	private String extractJsonFromChatResponse(String response) {
		if (response == null || response.isBlank()) {
			throw new AIServiceException("AI 排班建议服务暂时不可用，请使用手动排班功能");
		}
		try {
			JsonNode root = objectMapper.readTree(response);
			JsonNode content = root.path("choices").path(0).path("message").path("content");
			if (!content.isTextual()) {
				throw new AIServiceException("AI 返回格式无法解析，请使用手动排班功能");
			}
			return extractJsonArray(content.asText());
		} catch (AIServiceException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new AIServiceException("AI 返回格式无法解析，请使用手动排班功能");
		}
	}

	private String extractJsonArray(String content) {
		int start = content.indexOf('[');
		int end = content.lastIndexOf(']');
		if (start < 0 || end < start) {
			throw new AIServiceException("AI 返回格式无法解析，请使用手动排班功能");
		}
		return content.substring(start, end + 1);
	}
}




