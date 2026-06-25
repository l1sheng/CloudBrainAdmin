package com.neuCloudBrainMedical.admin.controller;

import com.neuCloudBrainMedical.admin.dto.DashboardStatisticsResponse;
import com.neuCloudBrainMedical.admin.service.IDashboardStatisticsService;
import com.neuCloudBrainMedical.admin.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

	private final IDashboardStatisticsService dashboardStatisticsService;

	public DashboardController(IDashboardStatisticsService dashboardStatisticsService) {
		this.dashboardStatisticsService = dashboardStatisticsService;
	}

	@GetMapping("/statistics")
	public Result<DashboardStatisticsResponse> getStatistics() {
		return Result.success(dashboardStatisticsService.getStatistics());
	}
}
