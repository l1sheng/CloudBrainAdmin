package com.neuCloudBrainMedical.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "registration")
public class Registration {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "registration_id")
	private Long registrationId;

	@Column(name = "patient_id", nullable = false)
	private Long patientId;

	@Column(name = "consultation_id")
	private Long consultationId;

	@Column(name = "dept_id", nullable = false)
	private Long deptId;

	@Column(name = "doctor_id", nullable = false)
	private Long doctorId;

	@Column(name = "schedule_id")
	private Long scheduleId;

	@Column(name = "operator_user_id")
	private Long operatorUserId;

	@Column(name = "source", nullable = false, length = 20)
	private String source;

	@Column(name = "registration_no", nullable = false, unique = true, length = 50)
	private String registrationNo;

	@Column(name = "queue_no")
	private Integer queueNo;

	@Column(name = "registration_fee", nullable = false)
	private BigDecimal registrationFee;

	@Column(name = "fee_status", nullable = false, length = 20)
	private String feeStatus;

	@Column(name = "status", nullable = false, length = 30)
	private String status;

	@Column(name = "registered_at", nullable = false)
	private LocalDateTime registeredAt;

	@Column(name = "called_at")
	private LocalDateTime calledAt;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public Long getRegistrationId() {
		return registrationId;
	}

	public void setRegistrationId(Long registrationId) {
		this.registrationId = registrationId;
	}

	public Long getPatientId() {
		return patientId;
	}

	public void setPatientId(Long patientId) {
		this.patientId = patientId;
	}

	public Long getConsultationId() {
		return consultationId;
	}

	public void setConsultationId(Long consultationId) {
		this.consultationId = consultationId;
	}

	public Long getDeptId() {
		return deptId;
	}

	public void setDeptId(Long deptId) {
		this.deptId = deptId;
	}

	public Long getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(Long doctorId) {
		this.doctorId = doctorId;
	}

	public Long getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(Long scheduleId) {
		this.scheduleId = scheduleId;
	}

	public Long getOperatorUserId() {
		return operatorUserId;
	}

	public void setOperatorUserId(Long operatorUserId) {
		this.operatorUserId = operatorUserId;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getRegistrationNo() {
		return registrationNo;
	}

	public void setRegistrationNo(String registrationNo) {
		this.registrationNo = registrationNo;
	}

	public Integer getQueueNo() {
		return queueNo;
	}

	public void setQueueNo(Integer queueNo) {
		this.queueNo = queueNo;
	}

	public BigDecimal getRegistrationFee() {
		return registrationFee;
	}

	public void setRegistrationFee(BigDecimal registrationFee) {
		this.registrationFee = registrationFee;
	}

	public String getFeeStatus() {
		return feeStatus;
	}

	public void setFeeStatus(String feeStatus) {
		this.feeStatus = feeStatus;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getRegisteredAt() {
		return registeredAt;
	}

	public void setRegisteredAt(LocalDateTime registeredAt) {
		this.registeredAt = registeredAt;
	}

	public LocalDateTime getCalledAt() {
		return calledAt;
	}

	public void setCalledAt(LocalDateTime calledAt) {
		this.calledAt = calledAt;
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
