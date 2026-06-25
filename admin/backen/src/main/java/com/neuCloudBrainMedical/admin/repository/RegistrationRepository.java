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

	/** 统计医生名下未完成（非终态）的挂号数（禁用前校验）。 */
	@Query("""
			SELECT COUNT(r) FROM Registration r
			WHERE r.doctorId = :doctorId
			  AND r.status NOT IN ('已取消', '已退号', '已完成', '已结束')
			""")
	long countPendingRegistrations(@Param("doctorId") Long doctorId);
}

