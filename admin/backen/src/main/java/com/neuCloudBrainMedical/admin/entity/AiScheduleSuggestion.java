package com.neuCloudBrainMedical.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_schedule_suggestion")
public class AiScheduleSuggestion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "suggestion_id")
	private Long suggestionId;

	@Column(name = "doctor_id")
	private Long doctorId;

	@Column(name = "dept_id")
	private Long deptId;

	@Column(name = "work_date")
	private LocalDate workDate;

	@Column(name = "time_period", length = 20)
	private String timePeriod;

	@Column(name = "suggested_quota")
	private Integer suggestedQuota;

	@Lob
	@Column(name = "suggestion_reason")
	private String suggestionReason;

	@Column(name = "status", nullable = false, length = 20)
	private String status;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "confirmed_at")
	private LocalDateTime confirmedAt;

	public Long getSuggestionId() {
		return suggestionId;
	}

	public void setSuggestionId(Long suggestionId) {
		this.suggestionId = suggestionId;
	}

	public Long getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(Long doctorId) {
		this.doctorId = doctorId;
	}

	public Long getDeptId() {
		return deptId;
	}

	public void setDeptId(Long deptId) {
		this.deptId = deptId;
	}

	public LocalDate getWorkDate() {
		return workDate;
	}

	public void setWorkDate(LocalDate workDate) {
		this.workDate = workDate;
	}

	public String getTimePeriod() {
		return timePeriod;
	}

	public void setTimePeriod(String timePeriod) {
		this.timePeriod = timePeriod;
	}

	public Integer getSuggestedQuota() {
		return suggestedQuota;
	}

	public void setSuggestedQuota(Integer suggestedQuota) {
		this.suggestedQuota = suggestedQuota;
	}

	public String getSuggestionReason() {
		return suggestionReason;
	}

	public void setSuggestionReason(String suggestionReason) {
		this.suggestionReason = suggestionReason;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getConfirmedAt() {
		return confirmedAt;
	}

	public void setConfirmedAt(LocalDateTime confirmedAt) {
		this.confirmedAt = confirmedAt;
	}
}
