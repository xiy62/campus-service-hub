package com.campushub.authservice.service;

import com.campushub.authservice.dto.LoginRequestDTO;

import java.util.Optional;

public interface AuthService {
    Optional<String> authenticate(LoginRequestDTO loginRequestDTO);

    boolean validateToken(String substring);
}
