package com.codeofcarbon.account.service;

import com.codeofcarbon.account.model.Action;
import com.codeofcarbon.account.model.Role;
import com.codeofcarbon.account.model.User;
import com.codeofcarbon.account.model.dto.UserDTO;
import com.codeofcarbon.account.repository.UserRepository;
import com.codeofcarbon.account.security.AppPasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    public static final int MAX_FAILED_ATTEMPTS = 5;
    private final AuditService auditService;
    private final UserRepository userRepository;
    private final AppPasswordEncoder encoder;
    private final List<String> breachedPasswords =
            List.of("PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
                    "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
                    "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));
    }

    public UserDTO addNewUser(User newUser, String path) {
        if (userRepository.existsUserByEmailIgnoreCase(newUser.getEmail()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User exist!");

        var validPassword = validatePassword(newUser.getPassword(), "");

        newUser.setPassword(validPassword);
        newUser.setEmail(newUser.getEmail().toLowerCase());
        newUser.grantAuthority(userRepository.findAll().isEmpty() ? Role.ROLE_ADMINISTRATOR : Role.ROLE_USER);

        userRepository.save(newUser);
        auditService.logEvent(Action.CREATE_USER, "Anonymous", newUser.getEmail(), path);
        return UserDTO.mapToUserDTO(newUser);
    }

    public Map<String, String> updatePassword(Map<String, String> request, String userEmail, String path) {
        var user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));
        var validPassword = validatePassword(request.get("new_password"), user.getPassword());

        user.setPassword(validPassword);

        userRepository.save(user);
        auditService.logEvent(Action.CHANGE_PASSWORD, userEmail, userEmail, path);
        return Map.of("email", userEmail, "status", "The password has been updated successfully");
    }

    public void increaseFailedAttempts(User user, String path) {
        user.setFailedAttempt(user.getFailedAttempt() + 1);
        if (user.getFailedAttempt() >= MAX_FAILED_ATTEMPTS) {
            lockUser(user, path);
        }
        userRepository.save(user);
    }

    private void lockUser(User user, String path) {
        user.setAccountNonLocked(false);
        auditService.logEvent(Action.BRUTE_FORCE, user.getEmail(), path, path);
        auditService.logEvent(Action.LOCK_USER, user.getEmail(), "Lock user " + user.getEmail(), path);
    }

    public String validatePassword(String newPassword, String currentPassword) {
        if (newPassword.length() < 12)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password length must be at least 12 chars!");
        if (breachedPasswords.contains(newPassword))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
        if (encoder.matches(newPassword, currentPassword))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The passwords must be different!");
        return encoder.encode(newPassword);
    }
}