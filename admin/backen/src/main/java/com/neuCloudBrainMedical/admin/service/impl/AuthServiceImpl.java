package com.neuCloudBrainMedical.admin.service.impl;

import com.neuCloudBrainMedical.admin.dto.AdminInfoResponse;
import com.neuCloudBrainMedical.admin.dto.LoginRequest;
import com.neuCloudBrainMedical.admin.dto.LoginResponse;
import com.neuCloudBrainMedical.admin.entity.SysRole;
import com.neuCloudBrainMedical.admin.entity.SysUser;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.repository.SysRoleRepository;
import com.neuCloudBrainMedical.admin.repository.SysUserRepository;
import com.neuCloudBrainMedical.admin.security.JwtTokenProvider;
import com.neuCloudBrainMedical.admin.service.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {

	private static final int ENABLED_STATUS = 1;
	private static final String BEARER_PREFIX = "Bearer ";

	private final SysUserRepository sysUserRepository;
	private final SysRoleRepository sysRoleRepository;
	private final JwtTokenProvider jwtTokenProvider;

	public AuthServiceImpl(SysUserRepository sysUserRepository,
			SysRoleRepository sysRoleRepository,
			JwtTokenProvider jwtTokenProvider) {
		this.sysUserRepository = sysUserRepository;
		this.sysRoleRepository = sysRoleRepository;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));
        if (!isEnabled(user) || !passwordMatches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        SysRole role = findRole(user.getRoleId());
		String token = jwtTokenProvider.generateToken(user.getUserId(), user.getUsername(), role.getRoleCode());
		return toLoginResponse(user, role, token);
	}

	private boolean isEnabled(SysUser user) {
		return user.getStatus() != null && user.getStatus() == ENABLED_STATUS;
	}

	@Override
	public AdminInfoResponse getAdminInfo(String authorizationHeader) {
		String token = extractToken(authorizationHeader);
		if (!jwtTokenProvider.validateToken(token)) {
			throw new BusinessException(401, "登录已过期，请重新登录");
		}
		String username = jwtTokenProvider.getUsernameFromToken(token);
		SysUser user = sysUserRepository.findByUsername(username)
				.orElseThrow(() -> new BusinessException(401, "用户不存在"));
		if (!isEnabled(user)) {
			throw new BusinessException(401, "用户已被禁用");
		}
		SysRole role = findRole(user.getRoleId());
		return toAdminInfoResponse(user, role);
	}

	private String extractToken(String authorizationHeader) {
		if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
			throw new BusinessException(401, "请先登录");
		}
		return authorizationHeader.substring(BEARER_PREFIX.length());
	}

	private SysRole findRole(Long roleId) {
		return sysRoleRepository.findById(roleId)
				.orElseThrow(() -> new BusinessException(500, "用户角色不存在"));
	}

	private boolean passwordMatches(String rawPassword, String storedPassword) {
		return StringUtils.hasText(rawPassword) && rawPassword.equals(storedPassword);
	}

	private LoginResponse toLoginResponse(SysUser user, SysRole role, String token) {
		LoginResponse response = new LoginResponse();
		response.setToken(token);
		response.setUserId(user.getUserId());
		response.setUsername(user.getUsername());
		response.setRealName(user.getRealName());
		response.setRoleCode(role.getRoleCode());
		response.setRoleName(role.getRoleName());
		return response;
	}

	private AdminInfoResponse toAdminInfoResponse(SysUser user, SysRole role) {
		AdminInfoResponse response = new AdminInfoResponse();
		response.setUserId(user.getUserId());
		response.setUsername(user.getUsername());
		response.setRealName(user.getRealName());
		response.setRoleCode(role.getRoleCode());
		response.setRoleName(role.getRoleName());
		return response;
	}
}
