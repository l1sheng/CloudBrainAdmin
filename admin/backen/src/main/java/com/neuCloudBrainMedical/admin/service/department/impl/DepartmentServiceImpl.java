package com.neuCloudBrainMedical.admin.service.department.impl;

import com.neuCloudBrainMedical.admin.dto.department.DepartmentDTO;
import com.neuCloudBrainMedical.admin.entity.department.Department;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.repository.department.DepartmentRepository;
import com.neuCloudBrainMedical.admin.service.department.DepartmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

	private static final int ENABLED_STATUS = 1;

	private final DepartmentRepository departmentRepository;

	public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
		this.departmentRepository = departmentRepository;
	}

	@Override
	public List<DepartmentDTO> listEnabledDepartments() {
		return departmentRepository.findByStatus(ENABLED_STATUS)
				.stream()
				.map(this::toDTO)
				.toList();
	}

	@Override
	public DepartmentDTO getDepartmentById(Long deptId) {
		return departmentRepository.findById(deptId)
				.map(this::toDTO)
				.orElseThrow(() -> new BusinessException(404, "科室不存在"));
	}

	private DepartmentDTO toDTO(Department department) {
		DepartmentDTO dto = new DepartmentDTO();
		dto.setDeptId(department.getDeptId());
		dto.setDeptCode(department.getDeptCode());
		dto.setDeptName(department.getDeptName());
		dto.setDeptType(department.getDeptType());
		dto.setLocation(department.getLocation());
		dto.setDescription(department.getDescription());
		return dto;
	}
}





