package com.neuCloudBrainMedical.admin.service.impl;

import com.neuCloudBrainMedical.admin.dto.DoctorOptionDTO;
import com.neuCloudBrainMedical.admin.entity.Doctor;
import com.neuCloudBrainMedical.admin.repository.DoctorRepository;
import com.neuCloudBrainMedical.admin.service.DoctorQueryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorQueryServiceImpl implements DoctorQueryService {

	private final DoctorRepository doctorRepository;

	public DoctorQueryServiceImpl(DoctorRepository doctorRepository) {
		this.doctorRepository = doctorRepository;
	}

	@Override
	public List<DoctorOptionDTO> listEnabledDoctors(Long departmentId) {
		return doctorRepository.findByDeptIdAndStatusOrderByDoctorIdAsc(departmentId, Doctor.STATUS_ENABLED)
				.stream()
				.map(this::toDTO)
				.toList();
	}

	private DoctorOptionDTO toDTO(Doctor doctor) {
		DoctorOptionDTO dto = new DoctorOptionDTO();
		dto.setDoctorId(doctor.getDoctorId());
		dto.setDoctorNo(doctor.getDoctorNo());
		dto.setDoctorName(doctor.getDoctorName());
		dto.setDoctorType(doctor.getDoctorType());
		dto.setTitle(doctor.getTitle());
		dto.setSpecialty(doctor.getSpecialty());
		return dto;
	}
}