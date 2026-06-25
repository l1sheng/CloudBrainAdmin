package com.neuCloudBrainMedical.admin.repository.department;

import com.neuCloudBrainMedical.admin.entity.department.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

	/** 按状态查询（返回所有，不做默认过滤）。 */
	List<Department> findByStatus(Integer status);

	/** 按状态查询（排序优先 sort_order，其次 dept_id）。 */
	List<Department> findByStatusOrderBySortOrderAscDeptIdAsc(Integer status);

	/** 名称模糊查询，用于关键字检索。 */
	List<Department> findByDeptNameContaining(String name);

	/** 同时按名称关键字与状态过滤。 */
	List<Department> findByDeptNameContainingAndStatusOrderBySortOrderAscDeptIdAsc(String name, Integer status);

	/** 仅按名称查询（有数据则已存在，用于更新唯一性校验）。 */
	List<Department> findByDeptName(String name);

	/** 查询所有记录（全量排序）。 */
	List<Department> findAllByOrderBySortOrderAscDeptIdAsc();

	/** 查询某个父节点的直接子节点。 */
	List<Department> findByParentIdOrderBySortOrderAscDeptIdAsc(Long parentId);

	/** 统计某个父节点的子节点数量（删除前的"是否存在子科室"校验）。 */
	long countByParentId(Long parentId);
}




