package com.neuCloudBrainMedical.admin.repository;

import com.neuCloudBrainMedical.admin.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

	List<Doctor> findByStatus(Integer status);

	List<Doctor> findByDeptIdAndStatus(Long deptId, Integer status);

	List<Doctor> findByDeptIdAndStatusOrderByDoctorIdAsc(Long deptId, Integer status);

	/** 统计科室下关联医生数（删除科室前校验使用）。 */
	long countByDeptId(Long deptId);
}