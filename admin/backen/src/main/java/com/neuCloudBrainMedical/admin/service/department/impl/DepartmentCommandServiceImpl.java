package com.neuCloudBrainMedical.admin.service.department.impl;

import com.neuCloudBrainMedical.admin.dto.department.DepartmentCreateRequest;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentResponse;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentUpdateRequest;
import com.neuCloudBrainMedical.admin.entity.department.Department;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.repository.department.DepartmentRepository;
import com.neuCloudBrainMedical.admin.repository.doctor.DoctorRepository;
import com.neuCloudBrainMedical.admin.repository.schedule.ScheduleRepository;
import com.neuCloudBrainMedical.admin.service.department.IDepartmentCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 科室写操作实现。
 * 写操作与查询分离，本类只依赖三个 Repository 完成业务校验与持久化。
 */
@Service
@Transactional
public class DepartmentCommandServiceImpl implements IDepartmentCommandService {

	private final DepartmentRepository departmentRepository;
	private final DoctorRepository doctorRepository;
	private final ScheduleRepository scheduleRepository;
	private final DepartmentMapper departmentMapper;

	public DepartmentCommandServiceImpl(DepartmentRepository departmentRepository,
			DoctorRepository doctorRepository, ScheduleRepository scheduleRepository,
			DepartmentMapper departmentMapper) {
		this.departmentRepository = departmentRepository;
		this.doctorRepository = doctorRepository;
		this.scheduleRepository = scheduleRepository;
		this.departmentMapper = departmentMapper;
	}

	@Override
	public DepartmentResponse createDepartment(DepartmentCreateRequest request) {
		if (request.getName() == null || request.getName().isBlank()) {
			throw new BusinessException(400, "科室名称不能为空");
		}

		// 若指定了上级科室，必须存在
		if (request.getParentId() != null && !departmentRepository.existsById(request.getParentId())) {
			throw new BusinessException(400, "上级科室不存在");
		}

		Department department = new Department();
		department.setDeptName(request.getName().trim());
		// 优先使用用户提供的 code；若未提供则自动生成
		department.setDeptCode(normalizeCode(request.getCode()));
		department.setDeptType(normalizeType(request.getDepartmentType()));
		department.setParentId(request.getParentId());
		department.setDescription(request.getDescription());
		department.setFloor(request.getFloor());
		department.setPhone(request.getPhone());
		department.setStatus(Department.STATUS_ENABLED);
		department.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
		LocalDateTime now = LocalDateTime.now();
		department.setCreatedAt(now);
		department.setUpdatedAt(now);

		return departmentMapper.toResponse(departmentRepository.save(department));
	}

	@Override
	public DepartmentResponse updateDepartment(Long id, DepartmentUpdateRequest request) {
		Department existing = departmentRepository.findById(id)
				.orElseThrow(() -> new BusinessException(404, "科室不存在"));

		// 不允许把 parentId 设为自身或其子孙
		if (request.getParentId() != null && request.getParentId().equals(id)) {
			throw new BusinessException(400, "上级科室不能为自身");
		}

		if (request.getName() != null) {
			String trimmed = request.getName().trim();
			if (trimmed.isEmpty()) throw new BusinessException(400, "科室名称不能为空");
			existing.setDeptName(trimmed);
		}
		if (request.getCode() != null) existing.setDeptCode(normalizeCode(request.getCode()));
		if (request.getDescription() != null) existing.setDescription(request.getDescription());
		if (request.getFloor() != null) existing.setFloor(request.getFloor());
		if (request.getPhone() != null) existing.setPhone(request.getPhone());
		if (request.getDepartmentType() != null) {
			existing.setDeptType(normalizeType(request.getDepartmentType()));
		}
		if (request.getSortOrder() != null) existing.setSortOrder(request.getSortOrder());
		if (request.getStatus() != null) existing.setStatus(request.getStatus());

		if (request.getParentId() != null) {
			if (!departmentRepository.existsById(request.getParentId())) {
				throw new BusinessException(400, "上级科室不存在");
			}
			existing.setParentId(request.getParentId());
		}
		existing.setUpdatedAt(LocalDateTime.now());
		return departmentMapper.toResponse(departmentRepository.save(existing));
	}

	@Override
	public void toggleStatus(Long id) {
		Department existing = departmentRepository.findById(id)
				.orElseThrow(() -> new BusinessException(404, "科室不存在"));
		int next = Department.STATUS_ENABLED == existing.getStatus()
				? Department.STATUS_DISABLED
				: Department.STATUS_ENABLED;
		existing.setStatus(next);
		existing.setUpdatedAt(LocalDateTime.now());
		departmentRepository.save(existing);
	}

	@Override
	public void deleteDepartment(Long id) {
		if (!departmentRepository.existsById(id)) {
			throw new BusinessException(404, "科室不存在");
		}
		long children = departmentRepository.countByParentId(id);
		if (children > 0) {
			throw new BusinessException(400, "该科室下存在子科室，无法删除");
		}
		long doctors = doctorRepository.countByDeptId(id);
		if (doctors > 0) {
			throw new BusinessException(400, "该科室下存在关联医生，无法删除");
		}
		long schedules = scheduleRepository.countByDeptId(id);
		if (schedules > 0) {
			throw new BusinessException(400, "该科室下存在排班记录，无法删除");
		}
		departmentRepository.deleteById(id);
	}

	// ------------------------------------------------------------------
	// 内部辅助
	// ------------------------------------------------------------------
	private static String normalizeType(String type) {
		if (type == null || type.isBlank()) return "门诊";
		return type.trim();
	}

	private static String normalizeCode(String code) {
		if (code != null && !code.isBlank()) {
			return code.trim();
		}
		// 用户未提供 code 时自动生成
		return "D" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
	}
}




