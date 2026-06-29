package com.neuCloudBrainMedical.admin.repository;

import com.neuCloudBrainMedical.admin.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SysRoleRepository extends JpaRepository<SysRole, Long> {

	/**
	 * 角色 code 以给定前缀开头的所有角色（例如 DOCTOR_ 前缀表示医生类角色）。
	 */
	List<SysRole> findByRoleCodeStartingWithOrderByRoleId(String prefix);

	Optional<SysRole> findByRoleCode(String roleCode);
}