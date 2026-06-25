package com.neuCloudBrainMedical.admin.service.impl;

import com.neuCloudBrainMedical.admin.dto.DepartmentResponse;
import com.neuCloudBrainMedical.admin.dto.DepartmentTreeNode;
import com.neuCloudBrainMedical.admin.entity.Department;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 科室实体 / DTO 转换组件。
 * 单一职责：只负责 Department <-> DepartmentResponse / DepartmentTreeNode 转换。
 */
@Component
public class DepartmentMapper {

	/** 平级 list → DepartmentResponse（不填充 children）。 */
	public List<DepartmentResponse> toResponseList(List<Department> departments) {
		if (departments == null) return Collections.emptyList();
		return departments.stream().map(this::toResponse).toList();
	}

	public DepartmentResponse toResponse(Department d) {
		if (d == null) return null;
		DepartmentResponse resp = new DepartmentResponse();
		resp.setId(d.getDeptId());
		resp.setParentId(d.getParentId());
		resp.setName(d.getDeptName());
		resp.setCode(d.getDeptCode());
		resp.setDepartmentType(d.getDeptType());
		resp.setFloor(d.getFloor());
		resp.setPhone(d.getPhone());
		resp.setDescription(d.getDescription());
		resp.setStatus(d.getStatus());
		resp.setSortOrder(d.getSortOrder());
		return resp;
	}

	/** 将平级列表组装为树形结构（children 为 null 或 List）。 */
	public List<DepartmentResponse> toTreeResponse(List<Department> departments) {
		if (departments == null || departments.isEmpty()) return Collections.emptyList();
		Map<Long, DepartmentResponse> map = new HashMap<>();
		for (Department d : departments) {
			map.put(d.getDeptId(), toResponse(d));
		}
		List<DepartmentResponse> roots = new ArrayList<>();
		for (Department d : departments) {
			DepartmentResponse self = map.get(d.getDeptId());
			if (d.getParentId() != null && map.containsKey(d.getParentId())) {
				DepartmentResponse parent = map.get(d.getParentId());
				if (parent.getChildren() == null) parent.setChildren(new ArrayList<>());
				parent.getChildren().add(self);
			} else {
				roots.add(self);
			}
		}
		return roots;
	}

	/** 极简树（只包含 id / name / children），供 el-tree 使用。 */
	public List<DepartmentTreeNode> toTreeNodes(List<Department> departments) {
		if (departments == null || departments.isEmpty()) return Collections.emptyList();
		Map<Long, DepartmentTreeNode> map = new HashMap<>();
		for (Department d : departments) {
			map.put(d.getDeptId(), new DepartmentTreeNode(d.getDeptId(), d.getDeptName()));
		}
		List<DepartmentTreeNode> roots = new ArrayList<>();
		for (Department d : departments) {
			DepartmentTreeNode self = map.get(d.getDeptId());
			if (d.getParentId() != null && map.containsKey(d.getParentId())) {
				DepartmentTreeNode parent = map.get(d.getParentId());
				if (parent.getChildren() == null) parent.setChildren(new ArrayList<>());
				parent.getChildren().add(self);
			} else {
				roots.add(self);
			}
		}
		return roots;
	}

	/** 沿 parentId 指针自底向上构建祖先链（根在前，当前节点最后）。 */
	public List<DepartmentResponse> buildAncestorChain(Long id, List<Department> departments) {
		if (id == null || departments == null || departments.isEmpty()) return Collections.emptyList();
		Map<Long, Department> byId = new HashMap<>();
		for (Department d : departments) byId.put(d.getDeptId(), d);
		List<DepartmentResponse> chain = new ArrayList<>();
		Department current = byId.get(id);
		while (current != null) {
			chain.add(0, toResponse(current));
			current = current.getParentId() != null ? byId.get(current.getParentId()) : null;
		}
		return chain;
	}
}