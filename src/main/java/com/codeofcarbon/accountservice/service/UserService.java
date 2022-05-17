package com.codeofcarbon.accountservice.service;

import com.codeofcarbon.accountservice.model.*;
import com.codeofcarbon.accountservice.model.dto.UserDTO;
import com.codeofcarbon.accountservice.repository.UserRepository;
import com.codeofcarbon.accountservice.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private final AuditService auditService;
    private final UserRepository userRepository;
    private final Validator validator;

    @Override
    public UserDetails loadUserByUsername(String email) {
        return validator.validateUser(email);
    }

    public UserDTO addNewUser(User newUser, String path) {
        validator.isNewUser(newUser);
        var validPassword = validator.validatePassword(newUser.getPassword(), "");

        newUser.setPassword(validPassword);
        newUser.setEmail(newUser.getEmail().toLowerCase());
        newUser.grantAuthority(userRepository.findAll().isEmpty() ? Role.ROLE_ADMINISTRATOR : Role.ROLE_USER);

        userRepository.save(newUser);
        auditService.logEvent(Action.CREATE_USER, "Anonymous", newUser.getEmail(), path);
        return UserDTO.mapToUserDTO(newUser);
    }

    public Map<String, String> updatePassword(Map<String, String> request, String userEmail, String path) {
        var user = validator.validateUser(userEmail);
        var validPassword = validator.validatePassword(request.get("new_password"), user.getPassword());

        user.setPassword(validPassword);

        userRepository.save(user);
        auditService.logEvent(Action.CHANGE_PASSWORD, userEmail, userEmail, path);
        return Map.of("email", userEmail, "status", "The password has been updated successfully");
    }

    public void increaseFailedAttempts(User user, String path) {
        user.setFailedAttempt(user.getFailedAttempt() + 1);
        if (user.getFailedAttempt() >= MAX_FAILED_LOGIN_ATTEMPTS) {
            lockUser(user, path);
        }
        userRepository.save(user);
    }

    private void lockUser(User user, String path) {
        user.setAccountNonLocked(false);
        auditService.logEvent(Action.BRUTE_FORCE, user.getEmail(), path, path);
        auditService.logEvent(Action.LOCK_USER, user.getEmail(), "Lock user " + user.getEmail(), path);
    }
}