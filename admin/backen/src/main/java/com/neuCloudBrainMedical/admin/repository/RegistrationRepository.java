package com.neuCloudBrainMedical.admin.repository;

import com.neuCloudBrainMedical.admin.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

	Long countByRegisteredAtGreaterThanEqualAndRegisteredAtLessThan(LocalDateTime startTime, LocalDateTime endTime);

	@Query("SELECT r.scheduleId, COUNT(r) FROM Registration r WHERE r.scheduleId IN :scheduleIds GROUP BY r.scheduleId")
	List<Object[]> countByScheduleIdIn(@Param("scheduleIds") Set<Long> scheduleIds);
}