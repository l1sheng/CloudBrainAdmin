package com.neuCloudBrainMedical.admin.service.department;

import com.neuCloudBrainMedical.admin.dto.department.DepartmentResponse;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentTreeNode;

import java.util.List;

/**
 * 科室查询接口（只读）。
 * 与 {@link IDepartmentCommandService} 分离，遵循接口隔离原则（ISP）。
 */
public interface IDepartmentQueryService {

	/**
	 * 按关键字和状态过滤查询科室（按 sort_order + id 排序）。
	 * @param keyword  名称关键字（可为 null / 空，表示不过滤）
	 * @param status   状态（0 = 停用，1 = 启用），可为 null，表示不过滤
	 */
	List<DepartmentResponse> listDepartments(String keyword, Integer status);

	/** 返回整棵科室树（所有启用 + 停用 节点都展示）。 */
	List<DepartmentTreeNode> getDepartmentTree();

	/** 返回科室详情。 */
	DepartmentResponse getDepartmentDetail(Long id);

	/** 获取某科室的所有祖先节点（根在前，当前节点最后），用于 "面包屑"。 */
	List<DepartmentResponse> getAncestors(Long id);
}




