package com.neuCloudBrainMedical.admin.repository;

import com.neuCloudBrainMedical.admin.entity.AiScheduleSuggestionDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiScheduleSuggestionDetailRepository extends JpaRepository<AiScheduleSuggestionDetail, Long> {

	List<AiScheduleSuggestionDetail> findBySuggestionId(Long suggestionId);

	List<AiScheduleSuggestionDetail> findBySuggestionIdAndStatus(Long suggestionId, String status);
}
