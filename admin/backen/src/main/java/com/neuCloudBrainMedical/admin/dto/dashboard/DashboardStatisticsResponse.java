package com.neuCloudBrainMedical.admin.dto.dashboard;

import com.neuCloudBrainMedical.admin.dto.department.DepartmentOverviewBrief;
import java.util.List;

public class DashboardStatisticsResponse {

	private Long todayRegistrationCount;
	private Long activeDepartmentCount;
	private Long todayScheduledDoctorCount;
	private Long aiUsageCount;
	private List<DepartmentOverviewBrief> departmentOverviews;

	public Long getTodayRegistrationCount() {
		return todayRegistrationCount;
	}

	public void setTodayRegistrationCount(Long todayRegistrationCount) {
		this.todayRegistrationCount = todayRegistrationCount;
	}

	public Long getActiveDepartmentCount() {
		return activeDepartmentCount;
	}

	public void setActiveDepartmentCount(Long activeDepartmentCount) {
		this.activeDepartmentCount = activeDepartmentCount;
	}

	public Long getTodayScheduledDoctorCount() {
		return todayScheduledDoctorCount;
	}

	public void setTodayScheduledDoctorCount(Long todayScheduledDoctorCount) {
		this.todayScheduledDoctorCount = todayScheduledDoctorCount;
	}

	public Long getAiUsageCount() {
		return aiUsageCount;
	}

	public void setAiUsageCount(Long aiUsageCount) {
		this.aiUsageCount = aiUsageCount;
	}

	public List<DepartmentOverviewBrief> getDepartmentOverviews() {
		return departmentOverviews;
	}

	public void setDepartmentOverviews(List<DepartmentOverviewBrief> departmentOverviews) {
		this.departmentOverviews = departmentOverviews;
	}
}