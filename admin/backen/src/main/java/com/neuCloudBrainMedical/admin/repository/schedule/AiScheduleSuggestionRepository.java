package com.neuCloudBrainMedical.admin.repository.schedule;

import com.neuCloudBrainMedical.admin.entity.schedule.AiScheduleSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AiScheduleSuggestionRepository extends JpaRepository<AiScheduleSuggestion, Long> {

	Long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime startTime, LocalDateTime endTime);

	Optional<AiScheduleSuggestion> findBySuggestionIdAndStatus(Long suggestionId, String status);
}





