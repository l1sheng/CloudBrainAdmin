package com.neuCloudBrainMedical.admin.service.impl;

import com.neuCloudBrainMedical.admin.dto.DoctorOptionDTO;
import com.neuCloudBrainMedical.admin.entity.Doctor;
import com.neuCloudBrainMedical.admin.entity.SysUser;
import com.neuCloudBrainMedical.admin.repository.DoctorRepository;
import com.neuCloudBrainMedical.admin.repository.SysUserRepository;
import com.neuCloudBrainMedical.admin.service.DoctorQueryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DoctorQueryServiceImpl implements DoctorQueryService {

	private final DoctorRepository doctorRepository;
	private final SysUserRepository sysUserRepository;

	public DoctorQueryServiceImpl(DoctorRepository doctorRepository,
			SysUserRepository sysUserRepository) {
		this.doctorRepository = doctorRepository;
		this.sysUserRepository = sysUserRepository;
	}

	@Override
	public List<DoctorOptionDTO> listEnabledDoctors(Long departmentId) {
		List<Doctor> doctors;
		if (departmentId != null) {
			doctors = doctorRepository.findByDeptIdAndStatusOrderByDoctorIdAsc(
					departmentId, Doctor.STATUS_ENABLED);
		} else {
			// 不传科室时，返回所有启用医生（供前端动态提取职称/医生类型）
			doctors = doctorRepository.findByStatus(Doctor.STATUS_ENABLED);
		}
		if (doctors.isEmpty()) {
			return List.of();
		}

		// 通过 doctor.user_id 关联 sys_user 获取 real_name（医生姓名）
		Set<Long> userIds = doctors.stream()
				.map(Doctor::getUserId)
				.collect(Collectors.toSet());
		Map<Long, SysUser> usersByUserId = sysUserRepository.findAllById(userIds)
				.stream()
				.collect(Collectors.toMap(SysUser::getUserId, Function.identity()));

		return doctors.stream()
				.map(doctor -> toDTO(doctor, usersByUserId.get(doctor.getUserId())))
				.toList();
	}

	private DoctorOptionDTO toDTO(Doctor doctor, SysUser user) {
		DoctorOptionDTO dto = new DoctorOptionDTO();
		dto.setDoctorId(doctor.getDoctorId());
		dto.setDoctorNo(doctor.getDoctorNo());
		dto.setDoctorName(user != null ? user.getRealName() : "");
		dto.setDoctorType(doctor.getDoctorType());
		dto.setTitle(doctor.getTitle());
		dto.setSpecialty(doctor.getSpecialty());
		return dto;
	}
}