package com.neuCloudBrainMedical.admin.dto;

import java.util.List;

/** 科室树形节点（极简：仅 id / name / children，用于 el-tree）。 */
public class DepartmentTreeNode {

	private Long id;
	private String name;
	private List<DepartmentTreeNode> children;

	public DepartmentTreeNode() {}

	public DepartmentTreeNode(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<DepartmentTreeNode> getChildren() {
		return children;
	}

	public void setChildren(List<DepartmentTreeNode> children) {
		this.children = children;
	}
}