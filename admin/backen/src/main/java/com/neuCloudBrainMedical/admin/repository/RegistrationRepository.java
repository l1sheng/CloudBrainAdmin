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

	/** 查询某个排班下的挂号记录，按挂号时间倒序排列（最新在前）。 */
	List<Registration> findByScheduleIdOrderByRegisteredAtDesc(Long scheduleId);

	/**
	 * 批量查询挂号记录对应的患者姓名。
	 * registration.patient_id 关联 patient.patient_id，
	 * 再通过 patient.user_id 关联 sys_user.user_id 拿到 real_name。
	 * 若 patient.user_id 为空，则退而使用 patient.patient_name。
	 */
	@Query(value = "SELECT r.registration_id, COALESCE(u.real_name, p.patient_name) AS patient_name "
			+ "FROM registration r "
			+ "LEFT JOIN patient p ON p.patient_id = r.patient_id "
			+ "LEFT JOIN sys_user u ON u.user_id = p.user_id "
			+ "WHERE r.registration_id IN :registrationIds", nativeQuery = true)
	List<Object[]> findPatientNamesByRegistrationIds(@Param("registrationIds") Set<Long> registrationIds);

	/** 统计医生名下未完成（非终态）的挂号数（禁用前校验）。 */
	@Query("""
			SELECT COUNT(r) FROM Registration r
			WHERE r.doctorId = :doctorId
			  AND r.status NOT IN ('已取消', '已退号', '已完成', '已结束')
			""")
	long countPendingRegistrations(@Param("doctorId") Long doctorId);
}