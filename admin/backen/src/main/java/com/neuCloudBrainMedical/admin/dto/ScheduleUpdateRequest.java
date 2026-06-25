package com.neuCloudBrainMedical.admin.dto;

public class ScheduleUpdateRequest {

	private String timeSlot;
	private Integer maxAppointments;
	private Integer currentAppointments;
	private String status;

	public String getTimeSlot() {
		return timeSlot;
	}

	public void setTimeSlot(String timeSlot) {
		this.timeSlot = timeSlot;
	}

	public Integer getMaxAppointments() {
		return maxAppointments;
	}

	public void setMaxAppointments(Integer maxAppointments) {
		this.maxAppointments = maxAppointments;
	}

	public Integer getCurrentAppointments() {
		return currentAppointments;
	}

	public void setCurrentAppointments(Integer currentAppointments) {
		this.currentAppointments = currentAppointments;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}