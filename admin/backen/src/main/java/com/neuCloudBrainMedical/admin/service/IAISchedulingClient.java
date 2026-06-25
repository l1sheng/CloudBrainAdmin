package com.neuCloudBrainMedical.admin.service;

import com.neuCloudBrainMedical.admin.dto.DoctorInfo;

import java.time.LocalDate;
import java.util.List;

public interface IAISchedulingClient {

	String requestSchedulingSuggestion(Long departmentId,
			LocalDate startDate,
			LocalDate endDate,
			List<DoctorInfo> doctorList,
			String contextInfo);
}
