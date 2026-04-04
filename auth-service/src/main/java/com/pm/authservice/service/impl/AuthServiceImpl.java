package com.pm.authservice.service.impl;

import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.service.AuthService;
import com.pm.authservice.service.UserService;
import com.pm.authservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public Optional<String> authenticate(LoginRequestDTO loginRequestDTO) {
        Optional<String> token = userService
                .findByEmail(loginRequestDTO.getEmail())
                .filter(u -> passwordEncoder.matches(loginRequestDTO.getPassword(), u.getPassword()))
                .map(u -> jwtUtil.generateToken(u.getEmail(), u.getRole()));

        return token;
    }

    @Override
    public boolean validateToken(String substring) {
        try {
            jwtUtil.validateToken(substring);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
