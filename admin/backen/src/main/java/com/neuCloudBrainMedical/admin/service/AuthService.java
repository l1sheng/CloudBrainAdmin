package com.neuCloudBrainMedical.admin.service;

import com.neuCloudBrainMedical.admin.dto.LoginRequest;
import com.neuCloudBrainMedical.admin.dto.LoginResponse;
import com.neuCloudBrainMedical.admin.dto.AdminInfoResponse;

public interface AuthService {

	LoginResponse login(LoginRequest request);

	AdminInfoResponse getAdminInfo(String authorizationHeader);
}
