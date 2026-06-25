package com.neuCloudBrainMedical.admin.repository;

import com.neuCloudBrainMedical.admin.entity.AiConsultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AiConsultationRepository extends JpaRepository<AiConsultation, Long> {

	Long countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndAiResultIsNotNull(
			LocalDateTime startTime,
			LocalDateTime endTime);
}
