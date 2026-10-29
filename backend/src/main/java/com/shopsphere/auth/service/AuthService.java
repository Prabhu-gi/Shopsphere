package com.shopsphere.auth.service;

import com.shopsphere.auth.dto.AuthResponse;
import com.shopsphere.auth.dto.LoginRequest;
import com.shopsphere.auth.dto.RefreshTokenRequest;
import com.shopsphere.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request, String clientIp);
    AuthResponse login(LoginRequest request, String clientIp);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String refreshToken);
}

