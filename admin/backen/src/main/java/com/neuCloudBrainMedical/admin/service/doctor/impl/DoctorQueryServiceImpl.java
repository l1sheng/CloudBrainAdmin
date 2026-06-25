package com.neuCloudBrainMedical.admin.service.doctor.impl;

import com.neuCloudBrainMedical.admin.dto.doctor.DoctorOptionDTO;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorResponse;
import com.neuCloudBrainMedical.admin.dto.PageResponse;
import com.neuCloudBrainMedical.admin.entity.department.Department;
import com.neuCloudBrainMedical.admin.entity.doctor.Doctor;
import com.neuCloudBrainMedical.admin.entity.SysUser;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.repository.department.DepartmentRepository;
import com.neuCloudBrainMedical.admin.repository.doctor.DoctorRepository;
import com.neuCloudBrainMedical.admin.repository.SysUserRepository;
import com.neuCloudBrainMedical.admin.service.doctor.IDoctorQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DoctorQueryServiceImpl implements IDoctorQueryService {

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final DoctorRepository doctorRepository;
	private final SysUserRepository sysUserRepository;
	private final DepartmentRepository departmentRepository;

	public DoctorQueryServiceImpl(DoctorRepository doctorRepository,
	                              SysUserRepository sysUserRepository,
	                              DepartmentRepository departmentRepository) {
		this.doctorRepository = doctorRepository;
		this.sysUserRepository = sysUserRepository;
		this.departmentRepository = departmentRepository;
	}

	@Override
	public PageResponse<DoctorResponse> listDoctors(Long departmentId, String keyword,
	                                                Integer status, String title,
	                                                int pageNum, int pageSize) {
		int safePage = Math.max(1, pageNum) - 1;
		int safeSize = Math.min(Math.max(1, pageSize), 200);
		Pageable pageable = PageRequest.of(safePage, safeSize,
				Sort.by(Sort.Direction.DESC, "doctorId"));

		Page<Doctor> page = doctorRepository.findForAdmin(departmentId, keyword, status, title, pageable);

		if (page.isEmpty()) {
			return PageResponse.of(page.getTotalElements(), pageNum, safeSize, List.of());
		}

		Map<Long, SysUser> users = batchLoadUsers(page.getContent());
		Map<Long, Department> depts = batchLoadDepartments(page.getContent());

		List<DoctorResponse> list = page.stream()
				.map(d -> toResponse(d, users.get(d.getUserId()), depts.get(d.getDeptId())))
				.toList();

		return PageResponse.of(page.getTotalElements(), pageNum, safeSize, list);
	}

	@Override
	public DoctorResponse getDoctorDetail(Long id) {
		Doctor doctor = doctorRepository.findById(id)
				.orElseThrow(() -> new BusinessException(404, "医生不存在"));
		SysUser user = sysUserRepository.findById(doctor.getUserId()).orElse(null);
		Department dept = departmentRepository.findById(doctor.getDeptId()).orElse(null);
		return toResponse(doctor, user, dept);
	}

	@Override
	public List<DoctorResponse> exportDoctors(Long departmentId) {
		List<Doctor> doctors = doctorRepository.findAllForExport(departmentId);
		if (doctors.isEmpty()) {
			return List.of();
		}
		Map<Long, SysUser> users = batchLoadUsers(doctors);
		Map<Long, Department> depts = batchLoadDepartments(doctors);
		return doctors.stream()
				.map(d -> toResponse(d, users.get(d.getUserId()), depts.get(d.getDeptId())))
				.toList();
	}

	@Override
	public List<DoctorOptionDTO> listEnabledDoctors(Long departmentId) {
		List<Doctor> doctors = departmentId != null
				? doctorRepository.findByDeptIdAndStatusOrderByDoctorIdAsc(departmentId, Doctor.STATUS_ENABLED)
				: doctorRepository.findByStatus(Doctor.STATUS_ENABLED);

		if (doctors.isEmpty()) {
			return List.of();
		}

		Map<Long, SysUser> users = batchLoadUsers(doctors);

		return doctors.stream().map(d -> toOptionDTO(d, users.get(d.getUserId()))).toList();
	}

	private DoctorOptionDTO toOptionDTO(Doctor d, SysUser user) {
		DoctorOptionDTO o = new DoctorOptionDTO();
		o.setDoctorId(d.getDoctorId());
		o.setDoctorNo(d.getDoctorNo());
		o.setDoctorName(user != null ? user.getRealName() : "");
		o.setDoctorType(d.getDoctorType());
		o.setTitle(d.getTitle());
		o.setSpecialty(d.getSpecialty());
		return o;
	}

	private Map<Long, SysUser> batchLoadUsers(Collection<Doctor> doctors) {
		Set<Long> ids = doctors.stream().map(Doctor::getUserId).collect(Collectors.toSet());
		return sysUserRepository.findAllById(ids)
				.stream().collect(Collectors.toMap(SysUser::getUserId, Function.identity()));
	}

	private Map<Long, Department> batchLoadDepartments(Collection<Doctor> doctors) {
		Set<Long> ids = doctors.stream().map(Doctor::getDeptId).collect(Collectors.toSet());
		return departmentRepository.findAllById(ids)
				.stream().collect(Collectors.toMap(Department::getDeptId, Function.identity()));
	}

	private DoctorResponse toResponse(Doctor d, SysUser user, Department dept) {
		DoctorResponse r = new DoctorResponse();
		r.setDoctorId(d.getDoctorId());
		r.setDoctorNo(d.getDoctorNo());
		r.setDoctorName(user != null ? user.getRealName() : "");
		r.setPhone(user != null ? user.getPhone() : null);
		r.setEmail(user != null ? user.getEmail() : null);
		r.setDepartmentId(d.getDeptId());
		r.setDepartmentName(dept != null ? dept.getDeptName() : "");
		r.setDoctorType(d.getDoctorType());
		r.setTitle(d.getTitle());
		r.setSpecialty(d.getSpecialty());
		r.setStatus(d.getStatus());
		r.setStatusText(d.getStatus() != null && d.getStatus() == Doctor.STATUS_ENABLED ? "启用" : "停用");
		r.setHireDate(d.getHireDate());
		r.setIntroduction(d.getIntroduction());
		if (d.getCreatedAt() != null) r.setCreatedAt(d.getCreatedAt().format(FMT));
		if (d.getUpdatedAt() != null) r.setUpdatedAt(d.getUpdatedAt().format(FMT));
		return r;
	}
}