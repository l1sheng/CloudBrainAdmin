package com.neuCloudBrainMedical.admin.dto;

public class DoctorInfo {

	private Long doctorId;
	private String doctorName;
	private String title;
	private String specialty;
	private Long historicalWorkDays;

	public Long getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(Long doctorId) {
		this.doctorId = doctorId;
	}

	public String getDoctorName() {
		return doctorName;
	}

	public void setDoctorName(String doctorName) {
		this.doctorName = doctorName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSpecialty() {
		return specialty;
	}

	public void setSpecialty(String specialty) {
		this.specialty = specialty;
	}

	public Long getHistoricalWorkDays() {
		return historicalWorkDays;
	}

	public void setHistoricalWorkDays(Long historicalWorkDays) {
		this.historicalWorkDays = historicalWorkDays;
	}
}
