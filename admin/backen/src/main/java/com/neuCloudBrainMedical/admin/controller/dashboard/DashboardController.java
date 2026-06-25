package com.neuCloudBrainMedical.admin.controller.dashboard;

import com.neuCloudBrainMedical.admin.dto.dashboard.DashboardStatisticsResponse;
import com.neuCloudBrainMedical.admin.service.dashboard.IDashboardStatisticsService;
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





