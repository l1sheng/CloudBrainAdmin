package com.neuCloudBrainMedical.admin.service.auth;

import com.neuCloudBrainMedical.admin.dto.auth.LoginRequest;
import com.neuCloudBrainMedical.admin.dto.auth.LoginResponse;
import com.neuCloudBrainMedical.admin.dto.auth.AdminInfoResponse;

public interface AuthService {

	LoginResponse login(LoginRequest request);

	AdminInfoResponse getAdminInfo(String authorizationHeader);
}





