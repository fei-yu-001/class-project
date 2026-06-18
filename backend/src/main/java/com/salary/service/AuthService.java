package com.salary.service;

import com.salary.dto.AuthResponse;
import com.salary.dto.LoginRequest;
import com.salary.dto.RegisterRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    boolean register(RegisterRequest request);
}
