package com.neuCloudBrainMedical.admin.repository;

import com.neuCloudBrainMedical.admin.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUser, Long> {

	Optional<SysUser> findByUsername(String username);

	Optional<SysUser> findByPhone(String phone);

	Optional<SysUser> findByEmail(String email);
}

