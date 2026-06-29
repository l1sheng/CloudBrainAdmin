package com.neuCloudBrainMedical.admin.service.doctor.impl;

import com.neuCloudBrainMedical.admin.dto.doctor.BatchImportResult;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorCreateRequest;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorDisableCheckResponse;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorResponse;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorRoleOption;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorUpdateRequest;
import com.neuCloudBrainMedical.admin.entity.doctor.Doctor;
import com.neuCloudBrainMedical.admin.entity.SysRole;
import com.neuCloudBrainMedical.admin.entity.SysUser;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.repository.doctor.DoctorRepository;
import com.neuCloudBrainMedical.admin.repository.RegistrationRepository;
import com.neuCloudBrainMedical.admin.repository.schedule.ScheduleRepository;
import com.neuCloudBrainMedical.admin.repository.SysRoleRepository;
import com.neuCloudBrainMedical.admin.repository.SysUserRepository;
import com.neuCloudBrainMedical.admin.service.doctor.IDoctorCommandService;
import com.neuCloudBrainMedical.admin.service.doctor.IDoctorQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class DoctorCommandServiceImpl implements IDoctorCommandService {

	private static final String DEFAULT_PASSWORD = "123456";

	/**
	 * 医生角色在 sys_role.role_code 里都应该以这个前缀开头，
	 * 方便在管理后台筛选可用角色。
	 */
	private static final String DOCTOR_ROLE_PREFIX = "DOCTOR";

	private final DoctorRepository doctorRepository;
	private final SysUserRepository sysUserRepository;
	private final SysRoleRepository sysRoleRepository;
	private final ScheduleRepository scheduleRepository;
	private final RegistrationRepository registrationRepository;
	private final IDoctorQueryService doctorQueryService;

	public DoctorCommandServiceImpl(DoctorRepository doctorRepository,
	                                SysUserRepository sysUserRepository,
	                                SysRoleRepository sysRoleRepository,
	                                ScheduleRepository scheduleRepository,
	                                RegistrationRepository registrationRepository,
	                                IDoctorQueryService doctorQueryService) {
		this.doctorRepository = doctorRepository;
		this.sysUserRepository = sysUserRepository;
		this.sysRoleRepository = sysRoleRepository;
		this.scheduleRepository = scheduleRepository;
		this.registrationRepository = registrationRepository;
		this.doctorQueryService = doctorQueryService;
	}

	@Override
	public List<DoctorRoleOption> listDoctorRoles() {
		return sysRoleRepository.findByRoleCodeStartingWithOrderByRoleId(DOCTOR_ROLE_PREFIX)
				.stream()
				.map(r -> new DoctorRoleOption(r.getRoleId(), r.getRoleCode(), r.getRoleName(), r.getDescription()))
				.toList();
	}

	@Override
	@Transactional
	public DoctorResponse createDoctor(DoctorCreateRequest request) {
		validateUniqueness(request);

		String username = hasText(request.getLoginUsername()) ? request.getLoginUsername() : request.getDoctorNo();
		String password = hasText(request.getLoginPassword()) ? request.getLoginPassword() : DEFAULT_PASSWORD;

		// 校验 username 唯一
		if (sysUserRepository.findByUsername(username).isPresent()) {
			throw new BusinessException(400, "登录账号【" + username + "】已存在");
		}

		Long roleId = resolveDoctorRoleId(request.getRoleId());

		SysUser user = new SysUser();
		user.setUsername(username);
		user.setPassword(password);
		user.setRealName(request.getName());
		user.setRoleId(roleId);
		user.setPhone(request.getPhone());
		user.setEmail(request.getEmail());
		user.setStatus(Doctor.STATUS_ENABLED);
		user.setCreatedAt(LocalDateTime.now());
		user.setUpdatedAt(LocalDateTime.now());
		user = sysUserRepository.save(user);

		Doctor doctor = new Doctor();
		doctor.setUserId(user.getUserId());
		doctor.setDeptId(request.getDepartmentId());
		doctor.setDoctorNo(request.getDoctorNo());
		doctor.setDoctorType(request.getDoctorType() != null ? request.getDoctorType() : "主治");
		doctor.setTitle(request.getTitle());
		doctor.setSpecialty(request.getSpecialty());
		doctor.setHireDate(request.getHireDate());
		doctor.setIntroduction(request.getIntroduction());
		doctor.setStatus(Doctor.STATUS_ENABLED);
		doctor.setCreatedAt(LocalDateTime.now());
		doctor.setUpdatedAt(LocalDateTime.now());
		doctorRepository.save(doctor);

		return doctorQueryService.getDoctorDetail(doctor.getDoctorId());
	}

	@Override
	@Transactional
	public DoctorResponse updateDoctor(Long id, DoctorUpdateRequest req) {
		Doctor doctor = doctorRepository.findById(id)
				.orElseThrow(() -> new BusinessException(404, "医生不存在"));

		boolean doctorChanged = applyDoctorUpdates(doctor, req);
		if (doctorChanged) {
			doctor.setUpdatedAt(LocalDateTime.now());
			doctorRepository.save(doctor);
		}

		SysUser user = sysUserRepository.findById(doctor.getUserId()).orElse(null);
		if (user != null) {
			boolean userChanged = applyUserUpdates(user, req);
			if (userChanged) {
				user.setUpdatedAt(LocalDateTime.now());
				sysUserRepository.save(user);
			}
		}

		return doctorQueryService.getDoctorDetail(id);
	}

	@Override
	public DoctorDisableCheckResponse checkBeforeDisable(Long id) {
		long schedules = scheduleRepository.countPendingSchedules(id, LocalDate.now());
		long registrations = registrationRepository.countPendingRegistrations(id);
		return new DoctorDisableCheckResponse(schedules, registrations);
	}

	@Override
	@Transactional
	public DoctorResponse toggleStatus(Long id, boolean force) {
		Doctor doctor = doctorRepository.findById(id)
				.orElseThrow(() -> new BusinessException(404, "医生不存在"));

		if (doctor.getStatus() == Doctor.STATUS_ENABLED) {
			DoctorDisableCheckResponse check = checkBeforeDisable(id);
			if (check.hasPending() && !force) {
				throw new BusinessException(409,
						"该医生仍有 " + check.getPendingSchedules() + " 个排班和 "
								+ check.getPendingRegistrations() + " 个挂号未完成，请确认是否仍要禁用");
			}
			doctor.setStatus(Doctor.STATUS_DISABLED);
		} else {
			doctor.setStatus(Doctor.STATUS_ENABLED);
		}
		doctor.setUpdatedAt(LocalDateTime.now());
		doctorRepository.save(doctor);

		syncUserStatus(doctor);

		return doctorQueryService.getDoctorDetail(id);
	}

	@Override
	@Transactional
	public BatchImportResult batchImport(List<DoctorCreateRequest> list) {
		BatchImportResult result = new BatchImportResult();
		if (list == null || list.isEmpty()) {
			return result;
		}
		for (int i = 0; i < list.size(); i++) {
			try {
				createDoctor(list.get(i));
				result.recordSuccess();
			} catch (Exception e) {
				result.recordError(i + 1, e.getMessage());
			}
		}
		return result;
	}

	@Override
	@Transactional
	public void deleteDoctor(Long id) {
		Doctor doctor = doctorRepository.findById(id)
				.orElseThrow(() -> new BusinessException(404, "医生不存在"));

		if (doctor.getStatus() == Doctor.STATUS_ENABLED) {
			throw new BusinessException(400, "请先禁用该医生再删除");
		}

		Long userId = doctor.getUserId();
		doctorRepository.delete(doctor);
		if (userId != null) {
			sysUserRepository.deleteById(userId);
		}
	}

	/**
	 * 解析角色 id。如果前端传了 id，则校验是否是一个"医生"角色；
	 * 否则尝试取默认角色；否则取第一个 DOCTOR 前缀角色；否则直接报错。
	 */
	private Long resolveDoctorRoleId(Long requestedRoleId) {
		if (requestedRoleId != null) {
			SysRole role = sysRoleRepository.findById(requestedRoleId)
					.orElseThrow(() -> new BusinessException(400, "角色不存在"));
			if (role.getRoleCode() == null || !role.getRoleCode().startsWith(DOCTOR_ROLE_PREFIX)) {
				throw new BusinessException(400, "必须选择医生权限角色");
			}
			return role.getRoleId();
		}

		SysRole defaultRole = sysRoleRepository.findByRoleCode("DOCTOR_CLINIC").orElse(null);
		if (defaultRole != null) {
			return defaultRole.getRoleId();
		}

		List<SysRole> allDoctor = sysRoleRepository.findByRoleCodeStartingWithOrderByRoleId(DOCTOR_ROLE_PREFIX);
		if (allDoctor.isEmpty()) {
			throw new BusinessException(500, "系统中尚未配置任何医生角色");
		}
		return allDoctor.get(0).getRoleId();
	}

	private void validateUniqueness(DoctorCreateRequest request) {
		if (doctorRepository.existsByDoctorNo(request.getDoctorNo())) {
			throw new BusinessException(400, "工号【" + request.getDoctorNo() + "】已存在");
		}
		if (hasText(request.getLoginUsername()) && sysUserRepository.findByUsername(request.getLoginUsername()).isPresent()) {
			throw new BusinessException(400, "登录账号【" + request.getLoginUsername() + "】已存在");
		}
		if (request.getPhone() != null && !request.getPhone().isBlank()) {
			sysUserRepository.findByPhone(request.getPhone()).ifPresent(u -> {
				throw new BusinessException(400, "手机号已被用户【" + u.getUsername() + "】使用");
			});
		}
		if (request.getEmail() != null && !request.getEmail().isBlank()) {
			sysUserRepository.findByEmail(request.getEmail()).ifPresent(u -> {
				throw new BusinessException(400, "邮箱已被用户【" + u.getUsername() + "】使用");
			});
		}
	}

	private boolean applyDoctorUpdates(Doctor d, DoctorUpdateRequest req) {
		boolean changed = false;
		if (needsUpdate(req.getDepartmentId(), d.getDeptId())) { d.setDeptId(req.getDepartmentId()); changed = true; }
		if (needsUpdate(req.getDoctorType(), d.getDoctorType())) { d.setDoctorType(req.getDoctorType()); changed = true; }
		if (needsUpdate(req.getTitle(), d.getTitle())) { d.setTitle(req.getTitle()); changed = true; }
		if (needsUpdate(req.getSpecialty(), d.getSpecialty())) { d.setSpecialty(req.getSpecialty()); changed = true; }
		if (needsUpdate(req.getHireDate(), d.getHireDate())) { d.setHireDate(req.getHireDate()); changed = true; }
		if (needsUpdate(req.getIntroduction(), d.getIntroduction())) { d.setIntroduction(req.getIntroduction()); changed = true; }
		return changed;
	}

	private boolean applyUserUpdates(SysUser user, DoctorUpdateRequest req) {
		boolean changed = false;
		if (needsUpdate(req.getName(), user.getRealName())) { user.setRealName(req.getName()); changed = true; }
		if (needsUpdate(req.getPhone(), user.getPhone())) { user.setPhone(req.getPhone()); changed = true; }
		if (needsUpdate(req.getEmail(), user.getEmail())) { user.setEmail(req.getEmail()); changed = true; }

		// 登录账号（username）变更，必须确保唯一性
		if (hasText(req.getLoginUsername()) && !req.getLoginUsername().equals(user.getUsername())) {
			sysUserRepository.findByUsername(req.getLoginUsername()).ifPresent(other -> {
				if (!other.getUserId().equals(user.getUserId())) {
					throw new BusinessException(400, "登录账号【" + req.getLoginUsername() + "】已存在");
				}
			});
			user.setUsername(req.getLoginUsername());
			changed = true;
		}

		// 密码变更（只有当请求里有值时才会更新）
		if (hasText(req.getLoginPassword()) && !req.getLoginPassword().equals(user.getPassword())) {
			user.setPassword(req.getLoginPassword());
			changed = true;
		}

		// 角色变更
		if (req.getRoleId() != null && !req.getRoleId().equals(user.getRoleId())) {
			SysRole role = sysRoleRepository.findById(req.getRoleId())
					.orElseThrow(() -> new BusinessException(400, "角色不存在"));
			if (role.getRoleCode() == null || !role.getRoleCode().startsWith(DOCTOR_ROLE_PREFIX)) {
				throw new BusinessException(400, "必须选择医生权限角色");
			}
			user.setRoleId(req.getRoleId());
			changed = true;
		}
		return changed;
	}

	private static boolean hasText(String s) {
		return s != null && !s.isBlank();
	}

	private static <T> boolean needsUpdate(T incoming, T existing) {
		return incoming != null && !Objects.equals(incoming, existing);
	}

	private void syncUserStatus(Doctor doctor) {
		sysUserRepository.findById(doctor.getUserId()).ifPresent(user -> {
			if (!Objects.equals(user.getStatus(), doctor.getStatus())) {
				user.setStatus(doctor.getStatus());
				user.setUpdatedAt(LocalDateTime.now());
				sysUserRepository.save(user);
			}
		});
	}
}