package com.neuCloudBrainMedical.admin.service.role;

import com.neuCloudBrainMedical.admin.dto.role.SysRoleRequest;
import com.neuCloudBrainMedical.admin.dto.role.SysRoleResponse;

import java.util.List;

public interface ISysRoleService {
	List<SysRoleResponse> listRoles();
	SysRoleResponse getRoleDetail(Long id);
	SysRoleResponse createRole(SysRoleRequest request);
	SysRoleResponse updateRole(Long id, SysRoleRequest request);
	void deleteRole(Long id);
	void toggleStatus(Long id);
}