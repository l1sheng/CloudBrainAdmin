package com.neuCloudBrainMedical.admin.repository.doctor;

import com.neuCloudBrainMedical.admin.entity.doctor.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

	List<Doctor> findByStatus(Integer status);

	List<Doctor> findByDeptIdAndStatusOrderByDoctorIdAsc(Long deptId, Integer status);

	long countByDeptId(Long deptId);

	boolean existsByDoctorNo(String doctorNo);

	@Query(value = """
			SELECT d FROM Doctor d
			LEFT JOIN SysUser u ON u.userId = d.userId
			LEFT JOIN Department dep ON dep.deptId = d.deptId
			WHERE (:departmentId IS NULL OR d.deptId = :departmentId)
			  AND (:status IS NULL OR d.status = :status)
			  AND (:title IS NULL OR d.title = :title)
			  AND (:keyword IS NULL OR :keyword = ''
			       OR LOWER(d.doctorNo) LIKE LOWER(CONCAT('%', :keyword, '%'))
			       OR LOWER(u.realName) LIKE LOWER(CONCAT('%', :keyword, '%'))
			       OR LOWER(d.specialty) LIKE LOWER(CONCAT('%', :keyword, '%')))
			""",
			countQuery = """
			SELECT COUNT(d) FROM Doctor d
			LEFT JOIN SysUser u ON u.userId = d.userId
			WHERE (:departmentId IS NULL OR d.deptId = :departmentId)
			  AND (:status IS NULL OR d.status = :status)
			  AND (:title IS NULL OR d.title = :title)
			  AND (:keyword IS NULL OR :keyword = ''
			       OR LOWER(d.doctorNo) LIKE LOWER(CONCAT('%', :keyword, '%'))
			       OR LOWER(u.realName) LIKE LOWER(CONCAT('%', :keyword, '%'))
			       OR LOWER(d.specialty) LIKE LOWER(CONCAT('%', :keyword, '%')))
			""")
	Page<Doctor> findForAdmin(@Param("departmentId") Long departmentId,
	                          @Param("keyword") String keyword,
	                          @Param("status") Integer status,
	                          @Param("title") String title,
	                          Pageable pageable);

	@Query("""
			SELECT d FROM Doctor d
			WHERE (:departmentId IS NULL OR d.deptId = :departmentId)
			ORDER BY d.doctorId ASC
			""")
	List<Doctor> findAllForExport(@Param("departmentId") Long departmentId);
}




