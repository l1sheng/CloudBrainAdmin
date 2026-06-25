package com.neuCloudBrainMedical.admin.service.impl;

import com.neuCloudBrainMedical.admin.dto.DepartmentResponse;
import com.neuCloudBrainMedical.admin.dto.DepartmentTreeNode;
import com.neuCloudBrainMedical.admin.entity.Department;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.repository.DepartmentRepository;
import com.neuCloudBrainMedical.admin.service.IDepartmentQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 科室查询实现。
 * 只读不写，所有排序统一委托给 Repository 的 OrderBy 查询。
 */
@Service
@Transactional(readOnly = true)
public class DepartmentQueryServiceImpl implements IDepartmentQueryService {

	private final DepartmentRepository departmentRepository;
	private final DepartmentMapper departmentMapper;

	public DepartmentQueryServiceImpl(DepartmentRepository departmentRepository, DepartmentMapper departmentMapper) {
		this.departmentRepository = departmentRepository;
		this.departmentMapper = departmentMapper;
	}

	@Override
	public List<DepartmentResponse> listDepartments(String keyword, Integer status) {
		List<Department> entities;
		boolean hasKeyword = keyword != null && !keyword.isBlank();
		if (hasKeyword && status != null) {
			entities = departmentRepository.findByDeptNameContainingAndStatusOrderBySortOrderAscDeptIdAsc(
					keyword.trim(), status);
		} else if (hasKeyword) {
			entities = departmentRepository.findByDeptNameContaining(keyword.trim());
		} else if (status != null) {
			entities = departmentRepository.findByStatusOrderBySortOrderAscDeptIdAsc(status);
		} else {
			entities = departmentRepository.findAllByOrderBySortOrderAscDeptIdAsc();
		}
		return departmentMapper.toResponseList(entities);
	}

	@Override
	public List<DepartmentTreeNode> getDepartmentTree() {
		return departmentMapper.toTreeNodes(
				departmentRepository.findAllByOrderBySortOrderAscDeptIdAsc());
	}

	@Override
	public DepartmentResponse getDepartmentDetail(Long id) {
		return departmentRepository.findById(id)
				.map(departmentMapper::toResponse)
				.orElseThrow(() -> new BusinessException(404, "科室不存在"));
	}

	@Override
	public List<DepartmentResponse> getAncestors(Long id) {
		return departmentMapper.buildAncestorChain(id,
				departmentRepository.findAllByOrderBySortOrderAscDeptIdAsc());
	}
}