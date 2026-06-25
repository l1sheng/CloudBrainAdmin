package com.neuCloudBrainMedical.admin.service;

import com.neuCloudBrainMedical.admin.dto.DepartmentDTO;

import java.util.List;

public interface DepartmentService {

	List<DepartmentDTO> listEnabledDepartments();

	DepartmentDTO getDepartmentById(Long deptId);
}
