package com.neuCloudBrainMedical.admin.service;

import com.neuCloudBrainMedical.admin.dto.DoctorOptionDTO;

import java.util.List;

public interface DoctorQueryService {

	List<DoctorOptionDTO> listEnabledDoctors(Long departmentId);
}
