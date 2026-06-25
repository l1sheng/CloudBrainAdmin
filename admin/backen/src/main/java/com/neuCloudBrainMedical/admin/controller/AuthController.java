package com.neuCloudBrainMedical.admin.controller;

import com.neuCloudBrainMedical.admin.dto.AdminInfoResponse;
import com.neuCloudBrainMedical.admin.dto.LoginRequest;
import com.neuCloudBrainMedical.admin.dto.LoginResponse;
import com.neuCloudBrainMedical.admin.service.AuthService;
import com.neuCloudBrainMedical.admin.util.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		return Result.success(authService.login(request));
	}

	@GetMapping("/info")
	public Result<AdminInfoResponse> getAdminInfo(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
		return Result.success(authService.getAdminInfo(authorizationHeader));
	}
}
