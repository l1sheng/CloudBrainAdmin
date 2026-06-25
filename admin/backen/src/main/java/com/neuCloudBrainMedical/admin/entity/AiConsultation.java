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
@Table(name = "ai_consultation")
public class AiConsultation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "consultation_id")
	private Long consultationId;

	@Column(name = "patient_id", nullable = false)
	private Long patientId;

	@Lob
	@Column(name = "chief_complaint")
	private String chiefComplaint;

	@Lob
	@Column(name = "symptom_detail")
	private String symptomDetail;

	@Lob
	@Column(name = "ai_summary")
	private String aiSummary;

	@Column(name = "recommended_dept_id")
	private Long recommendedDeptId;

	@Column(name = "risk_level", nullable = false, length = 20)
	private String riskLevel;

	@Lob
	@Column(name = "ai_result")
	private String aiResult;

	@Column(name = "status", nullable = false, length = 20)
	private String status;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	public Long getConsultationId() {
		return consultationId;
	}

	public void setConsultationId(Long consultationId) {
		this.consultationId = consultationId;
	}

	public Long getPatientId() {
		return patientId;
	}

	public void setPatientId(Long patientId) {
		this.patientId = patientId;
	}

	public String getChiefComplaint() {
		return chiefComplaint;
	}

	public void setChiefComplaint(String chiefComplaint) {
		this.chiefComplaint = chiefComplaint;
	}

	public String getSymptomDetail() {
		return symptomDetail;
	}

	public void setSymptomDetail(String symptomDetail) {
		this.symptomDetail = symptomDetail;
	}

	public String getAiSummary() {
		return aiSummary;
	}

	public void setAiSummary(String aiSummary) {
		this.aiSummary = aiSummary;
	}

	public Long getRecommendedDeptId() {
		return recommendedDeptId;
	}

	public void setRecommendedDeptId(Long recommendedDeptId) {
		this.recommendedDeptId = recommendedDeptId;
	}

	public String getRiskLevel() {
		return riskLevel;
	}

	public void setRiskLevel(String riskLevel) {
		this.riskLevel = riskLevel;
	}

	public String getAiResult() {
		return aiResult;
	}

	public void setAiResult(String aiResult) {
		this.aiResult = aiResult;
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
}
