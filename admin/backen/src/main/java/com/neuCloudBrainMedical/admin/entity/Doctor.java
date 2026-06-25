package com.neuCloudBrainMedical.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "doctor")
public class Doctor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "doctor_id")
	private Long doctorId;

	@Column(name = "user_id", nullable = false, unique = true)
	private Long userId;

	@Column(name = "dept_id", nullable = false)
	private Long deptId;

	@Column(name = "doctor_no", nullable = false, unique = true, length = 50)
	private String doctorNo;

	@Column(name = "doctor_type", nullable = false, length = 50)
	private String doctorType;

	@Column(name = "title", length = 50)
	private String title;

	@Column(name = "specialty")
	private String specialty;

	@Lob
	@Column(name = "introduction")
	private String introduction;

	@Column(name = "status", nullable = false)
	private Integer status;

	/** 医生状态：启用 */
	public static final int STATUS_ENABLED = 1;
	/** 医生状态：停用 */
	public static final int STATUS_DISABLED = 0;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public Long getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(Long doctorId) {
		this.doctorId = doctorId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getDeptId() {
		return deptId;
	}

	public void setDeptId(Long deptId) {
		this.deptId = deptId;
	}

	public String getDoctorNo() {
		return doctorNo;
	}

	public void setDoctorNo(String doctorNo) {
		this.doctorNo = doctorNo;
	}

	public String getDoctorType() {
		return doctorType;
	}

	public void setDoctorType(String doctorType) {
		this.doctorType = doctorType;
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

	public String getIntroduction() {
		return introduction;
	}

	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
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