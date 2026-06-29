package com.neuCloudBrainMedical.admin.util;

import com.neuCloudBrainMedical.admin.exception.BusinessException;

import java.math.BigDecimal;
import java.util.List;

public final class ScheduleTimeSlotUtils {

	private ScheduleTimeSlotUtils() {
	}

	/**
	 * 将时段值归一化为数据库标准中文值（上午/下午/夜间）。
	 * 同时兼容前端传来的旧英文值和中文值。
	 */
	public static String normalize(String timeSlot) {
		if (timeSlot == null || timeSlot.isBlank()) {
			throw new BusinessException(400, "请选择排班时段");
		}
		String value = timeSlot.trim();
		// 兼容旧英文值
		return switch (value.toUpperCase()) {
			case "MORNING" -> "上午";
			case "AFTERNOON" -> "下午";
			case "EVENING" -> "夜间";
			default -> switch (value) {
				case "上午" -> "上午";
				case "下午" -> "下午";
				case "夜间", "晚间", "晚上" -> "夜间";
				default -> throw new BusinessException(400, "排班时段只能是上午、下午或夜间");
			};
		};
	}

	public static List<String> conflictValues(String timeSlot) {
		return switch (normalize(timeSlot)) {
			case "上午" -> List.of("上午", "MORNING");
			case "下午" -> List.of("下午", "AFTERNOON");
			case "夜间" -> List.of("夜间", "EVENING", "晚间", "晚上");
			default -> List.of(timeSlot);
		};
	}

	public static String displayName(String timeSlot) {
		return normalize(timeSlot);
	}

	/**
	 * 根据时段返回默认开始时间。
	 */
	public static java.time.LocalTime defaultStartTime(String timeSlot) {
		return switch (normalize(timeSlot)) {
			case "上午" -> java.time.LocalTime.of(8, 0);
			case "下午" -> java.time.LocalTime.of(14, 0);
			case "夜间" -> java.time.LocalTime.of(18, 0);
			default -> java.time.LocalTime.of(8, 0);
		};
	}

	/**
	 * 根据时段返回默认结束时间。
	 */
	/** 根据医生职称返回默认挂号费 */
	public static BigDecimal defaultFeeByTitle(String title) {
		if (title == null) return new BigDecimal("15.00");
		return switch (title) {
			case "主任医师" -> new BigDecimal("50.00");
			case "副主任医师" -> new BigDecimal("25.00");
			case "主治医师" -> new BigDecimal("15.00");
			case "住院医师" -> new BigDecimal("10.00");
			default -> new BigDecimal("15.00");
		};
	}

	public static java.time.LocalTime defaultEndTime(String timeSlot) {
		return switch (normalize(timeSlot)) {
			case "上午" -> java.time.LocalTime.of(12, 0);
			case "下午" -> java.time.LocalTime.of(17, 0);
			case "夜间" -> java.time.LocalTime.of(21, 0);
			default -> java.time.LocalTime.of(12, 0);
		};
	}
}
