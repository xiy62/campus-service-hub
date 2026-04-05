package com.campushub.authservice.service;

import com.campushub.authservice.model.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findByEmail(String email);
}
