package com.neuCloudBrainMedical.admin.service;

import com.neuCloudBrainMedical.admin.dto.LoginRequest;
import com.neuCloudBrainMedical.admin.dto.LoginResponse;
import com.neuCloudBrainMedical.admin.entity.SysRole;
import com.neuCloudBrainMedical.admin.entity.SysUser;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.repository.SysRoleRepository;
import com.neuCloudBrainMedical.admin.repository.SysUserRepository;
import com.neuCloudBrainMedical.admin.security.JwtTokenProvider;
import com.neuCloudBrainMedical.admin.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

	@Test
	void loginReturnsTokenAndUserInfoForActiveAdmin() {
		SysUserRepository userRepository = mock(SysUserRepository.class);
		SysRoleRepository roleRepository = mock(SysRoleRepository.class);
		JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
		SysUser user = new SysUser();
		user.setUserId(1L);
		user.setRoleId(1L);
		user.setUsername("admin");
		user.setPassword("123456");
		user.setRealName("系统管理员");
		user.setStatus(1);
		SysRole role = new SysRole();
		role.setRoleId(1L);
		role.setRoleCode("ADMIN");
		role.setRoleName("管理员");
		when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
		when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
		when(jwtTokenProvider.generateToken(1L, "admin", "ADMIN")).thenReturn("token-value");
		AuthService authService = new AuthServiceImpl(userRepository, roleRepository, jwtTokenProvider);

		LoginResponse response = authService.login(new LoginRequest("admin", "123456"));

		assertThat(response.getToken()).isEqualTo("token-value");
		assertThat(response.getUserId()).isEqualTo(1L);
		assertThat(response.getUsername()).isEqualTo("admin");
		assertThat(response.getRoleCode()).isEqualTo("ADMIN");
	}

	@Test
	void loginThrowsBusinessExceptionWhenPasswordIsWrong() {
		SysUserRepository userRepository = mock(SysUserRepository.class);
		SysRoleRepository roleRepository = mock(SysRoleRepository.class);
		JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
		SysUser user = new SysUser();
		user.setUsername("admin");
		user.setPassword("123456");
		user.setStatus(1);
		when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
		AuthService authService = new AuthServiceImpl(userRepository, roleRepository, jwtTokenProvider);

		assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "wrong")))
				.isInstanceOf(BusinessException.class)
				.hasMessage("用户名或密码错误");
	}
}