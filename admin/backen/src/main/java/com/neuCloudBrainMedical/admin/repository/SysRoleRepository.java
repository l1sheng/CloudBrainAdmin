package com.neuCloudBrainMedical.admin.repository;

import com.neuCloudBrainMedical.admin.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SysRoleRepository extends JpaRepository<SysRole, Long> {

	/**
	 * 角色 code 包含给定关键字的所有角色（例如包含 DOCTOR 表示医生类角色）。
	 */
	List<SysRole> findByRoleCodeContainingOrderByRoleId(String keyword);

	Optional<SysRole> findByRoleCode(String roleCode);
}