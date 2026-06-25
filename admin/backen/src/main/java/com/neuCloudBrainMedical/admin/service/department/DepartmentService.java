package com.neuCloudBrainMedical.admin.service.department;

import com.neuCloudBrainMedical.admin.dto.department.DepartmentDTO;

import java.util.List;

public interface DepartmentService {

	List<DepartmentDTO> listEnabledDepartments();

	DepartmentDTO getDepartmentById(Long deptId);
}





