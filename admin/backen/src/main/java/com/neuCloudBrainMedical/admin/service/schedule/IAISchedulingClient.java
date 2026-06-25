package com.neuCloudBrainMedical.admin.service.schedule;

import com.neuCloudBrainMedical.admin.dto.doctor.DoctorInfo;

import java.time.LocalDate;
import java.util.List;

public interface IAISchedulingClient {

	String requestSchedulingSuggestion(Long departmentId,
			LocalDate startDate,
			LocalDate endDate,
			List<DoctorInfo> doctorList,
			String contextInfo);
}





