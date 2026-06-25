package com.neuCloudBrainMedical.admin.util;

import com.neuCloudBrainMedical.admin.exception.BusinessException;

import java.util.List;

public final class ScheduleTimeSlotUtils {

	private ScheduleTimeSlotUtils() {
	}

	public static String normalize(String timeSlot) {
		if (timeSlot == null || timeSlot.isBlank()) {
			throw new BusinessException(400, "请选择排班时段");
		}
		String value = timeSlot.trim();
		return switch (value.toUpperCase()) {
			case "MORNING" -> "MORNING";
			case "AFTERNOON" -> "AFTERNOON";
			case "EVENING" -> "EVENING";
			default -> switch (value) {
				case "上午" -> "MORNING";
				case "下午" -> "AFTERNOON";
				case "晚间", "晚上" -> "EVENING";
				default -> throw new BusinessException(400, "排班时段只能是上午、下午或晚间");
			};
		};
	}

	public static List<String> conflictValues(String timeSlot) {
		return switch (normalize(timeSlot)) {
			case "MORNING" -> List.of("MORNING", "上午");
			case "AFTERNOON" -> List.of("AFTERNOON", "下午");
			case "EVENING" -> List.of("EVENING", "晚间", "晚上");
			default -> List.of(timeSlot);
		};
	}

	public static String displayName(String timeSlot) {
		return switch (normalize(timeSlot)) {
			case "MORNING" -> "上午";
			case "AFTERNOON" -> "下午";
			case "EVENING" -> "晚间";
			default -> timeSlot;
		};
	}
}
