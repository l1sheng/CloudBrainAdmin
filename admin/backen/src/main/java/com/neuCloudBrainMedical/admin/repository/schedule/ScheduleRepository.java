package com.neuCloudBrainMedical.admin.repository.schedule;

import com.neuCloudBrainMedical.admin.entity.schedule.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

	@Query("""
			select count(distinct schedule.doctorId)
			from DoctorSchedule schedule
			where schedule.workDate = :workDate
			and schedule.status in :statuses
			""")
	Long countDistinctDoctorsByWorkDateAndStatuses(@Param("workDate") LocalDate workDate,
			@Param("statuses") Collection<String> statuses);

	List<DoctorSchedule> findByDeptIdAndWorkDateBetween(Long departmentId, LocalDate startDate, LocalDate endDate);

	List<DoctorSchedule> findByWorkDateBetween(LocalDate startDate, LocalDate endDate);

	@Query("""
			select schedule
			from DoctorSchedule schedule
			where (:departmentId is null or schedule.deptId = :departmentId)
			and schedule.workDate between :startDate and :endDate
			order by schedule.workDate asc, schedule.doctorId asc, schedule.timePeriod asc
			""")
	List<DoctorSchedule> findSchedulesForAdmin(@Param("departmentId") Long departmentId,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);

	boolean existsByDoctorIdAndWorkDateAndTimePeriodIn(Long doctorId, LocalDate scheduleDate,
			Collection<String> timeSlots);

	Long countByWorkDate(LocalDate scheduleDate);

	/** 统计科室下关联的排班记录数（删除科室前校验使用）。 */
	long countByDeptId(Long departmentId);

	/** 统计医生在指定日期（含）之后仍有效的排班数（禁用医生前校验）。 */
	@Query("""
			SELECT COUNT(s) FROM DoctorSchedule s
			WHERE s.doctorId = :doctorId
			  AND s.workDate >= :minDate
			  AND s.status NOT IN ('已取消', '已过期')
			""")
	long countPendingSchedules(@Param("doctorId") Long doctorId,
	                           @Param("minDate") LocalDate minDate);
}




