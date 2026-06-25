package com.neuCloudBrainMedical.admin.controller;

import com.neuCloudBrainMedical.admin.dto.DoctorOptionDTO;
import com.neuCloudBrainMedical.admin.service.DoctorQueryService;
import com.neuCloudBrainMedical.admin.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/doctors")
public class DoctorController {

	private final DoctorQueryService doctorQueryService;

	public DoctorController(DoctorQueryService doctorQueryService) {
		this.doctorQueryService = doctorQueryService;
	}

	@GetMapping
	public Result<List<DoctorOptionDTO>> listEnabledDoctors(@RequestParam("departmentId") Long departmentId) {
		return Result.success(doctorQueryService.listEnabledDoctors(departmentId));
	}
}
