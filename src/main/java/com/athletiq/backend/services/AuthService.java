package com.athletiq.backend.services;

import com.athletiq.backend.dtos.request.LoginRequest;
import com.athletiq.backend.dtos.request.RegisterRequest;
import com.athletiq.backend.dtos.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}
