package com.neuCloudBrainMedical.admin.service.role.impl;

import com.neuCloudBrainMedical.admin.dto.role.SysRoleRequest;
import com.neuCloudBrainMedical.admin.dto.role.SysRoleResponse;
import com.neuCloudBrainMedical.admin.entity.SysRole;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.repository.SysRoleRepository;
import com.neuCloudBrainMedical.admin.service.role.ISysRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysRoleServiceImpl implements ISysRoleService {

	private final SysRoleRepository repository;

	public SysRoleServiceImpl(SysRoleRepository repository) {
		this.repository = repository;
	}

	@Override
	public List<SysRoleResponse> listRoles() {
		return repository.findAll().stream().map(this::toResponse).toList();
	}

	@Override
	public SysRoleResponse getRoleDetail(Long id) {
		SysRole role = repository.findById(id)
				.orElseThrow(() -> new BusinessException(404, "角色不存在"));
		return toResponse(role);
	}

	@Override
	@Transactional
	public SysRoleResponse createRole(SysRoleRequest request) {
		if (repository.findByRoleCode(request.getRoleCode()).isPresent()) {
			throw new BusinessException(400, "角色编码【" + request.getRoleCode() + "】已存在");
		}
		SysRole role = new SysRole();
		role.setRoleCode(request.getRoleCode());
		role.setRoleName(request.getRoleName());
		role.setDescription(request.getDescription());
		role.setStatus(request.getStatus() != null ? request.getStatus() : 1);
		role.setCreatedAt(LocalDateTime.now());
		role.setUpdatedAt(LocalDateTime.now());
		return toResponse(repository.save(role));
	}

	@Override
	@Transactional
	public SysRoleResponse updateRole(Long id, SysRoleRequest request) {
		SysRole role = repository.findById(id)
				.orElseThrow(() -> new BusinessException(404, "角色不存在"));
		if (!role.getRoleCode().equals(request.getRoleCode())) {
			repository.findByRoleCode(request.getRoleCode()).ifPresent(existing -> {
				if (!existing.getRoleId().equals(id)) {
					throw new BusinessException(400, "角色编码【" + request.getRoleCode() + "】已存在");
				}
			});
			role.setRoleCode(request.getRoleCode());
		}
		role.setRoleName(request.getRoleName());
		role.setDescription(request.getDescription());
		if (request.getStatus() != null) role.setStatus(request.getStatus());
		role.setUpdatedAt(LocalDateTime.now());
		return toResponse(repository.save(role));
	}

	@Override
	@Transactional
	public void deleteRole(Long id) {
		SysRole role = repository.findById(id)
				.orElseThrow(() -> new BusinessException(404, "角色不存在"));
		repository.delete(role);
	}

	@Override
	@Transactional
	public void toggleStatus(Long id) {
		SysRole role = repository.findById(id)
				.orElseThrow(() -> new BusinessException(404, "角色不存在"));
		role.setStatus(role.getStatus() == 1 ? 0 : 1);
		role.setUpdatedAt(LocalDateTime.now());
		repository.save(role);
	}

	private SysRoleResponse toResponse(SysRole role) {
		SysRoleResponse resp = new SysRoleResponse();
		resp.setRoleId(role.getRoleId());
		resp.setRoleCode(role.getRoleCode());
		resp.setRoleName(role.getRoleName());
		resp.setDescription(role.getDescription());
		resp.setStatus(role.getStatus());
		return resp;
	}
}