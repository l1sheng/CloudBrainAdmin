package com.neuCloudBrainMedical.admin.repository.schedule;

import com.neuCloudBrainMedical.admin.entity.schedule.AiScheduleSuggestionDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiScheduleSuggestionDetailRepository extends JpaRepository<AiScheduleSuggestionDetail, Long> {

	List<AiScheduleSuggestionDetail> findBySuggestionId(Long suggestionId);

	List<AiScheduleSuggestionDetail> findBySuggestionIdAndStatus(Long suggestionId, String status);
}





