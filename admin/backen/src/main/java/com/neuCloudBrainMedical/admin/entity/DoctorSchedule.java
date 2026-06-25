package com.neuCloudBrainMedical.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "doctor_schedule")
public class DoctorSchedule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "schedule_id")
	private Long scheduleId;

	@Column(name = "doctor_id", nullable = false)
	private Long doctorId;

	@Column(name = "dept_id", nullable = false)
	private Long deptId;

	@Column(name = "work_date", nullable = false)
	private LocalDate workDate;

	@Column(name = "time_period", nullable = false, length = 20)
	private String timePeriod;

	@Column(name = "start_time")
	private LocalTime startTime;

	@Column(name = "end_time")
	private LocalTime endTime;

	@Column(name = "total_quota", nullable = false)
	private Integer totalQuota;

	@Column(name = "remain_quota", nullable = false)
	private Integer remainQuota;

	@Column(name = "registration_fee", nullable = false)
	private BigDecimal registrationFee;

	@Column(name = "status", nullable = false, length = 20)
	private String status;

	@Column(name = "source", nullable = false, length = 30)
	private String source;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public Long getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(Long scheduleId) {
		this.scheduleId = scheduleId;
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

	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	public Integer getTotalQuota() {
		return totalQuota;
	}

	public void setTotalQuota(Integer totalQuota) {
		this.totalQuota = totalQuota;
	}

	public Integer getRemainQuota() {
		return remainQuota;
	}

	public void setRemainQuota(Integer remainQuota) {
		this.remainQuota = remainQuota;
	}

	public BigDecimal getRegistrationFee() {
		return registrationFee;
	}

	public void setRegistrationFee(BigDecimal registrationFee) {
		this.registrationFee = registrationFee;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}