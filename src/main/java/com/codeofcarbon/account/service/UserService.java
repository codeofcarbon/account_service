package com.codeofcarbon.account.service;

import com.codeofcarbon.account.model.Action;
import com.codeofcarbon.account.model.Role;
import com.codeofcarbon.account.model.User;
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

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    public static String foundEmail;
    public static final int MAX_FAILED_ATTEMPTS = 4;
    private final AuditService auditService;
    private final UserRepository userRepository;
    private final AppPasswordEncoder encoder;
    private final List<String> breachedPasswords =
            List.of("PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
                    "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
                    "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");

    @Override
    public UserDetails loadUserByUsername(String email) {
        foundEmail = email.toLowerCase();
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));
    }

    public User addNewUser(User user, String requestPath) {
        userRepository.findByEmail(user.getEmail().toLowerCase())
                .ifPresent(userToAdd -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User exist!");
                });

        var validPassword = checkPasswordRequirements(user.getPassword(), "");
        user.setPassword(encoder.encode(validPassword));
        user.setEmail(user.getEmail().toLowerCase());
        user.grantAuthority(userRepository.findAll().isEmpty() ? Role.ROLE_ADMINISTRATOR : Role.ROLE_USER);

        auditService.logEvent(Action.CREATE_USER, null, user.getEmail(), requestPath);

        return userRepository.save(user);
    }

    public void updatePassword(String newPassword, String userEmail, String requestPath) {
        var user = userRepository.findUserByEmailIgnoreCase(userEmail);
        var validPassword = checkPasswordRequirements(newPassword, user.getPassword());
        user.setPassword(encoder.encode(validPassword));

        auditService.logEvent(Action.CHANGE_PASSWORD, user.getEmail(), user.getEmail(), requestPath);

        userRepository.save(user);
    }

    private String checkPasswordRequirements(String newPassword, String currentPassword) {
        if (newPassword.length() < 12)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password length must be at least 12 chars!");
        if (breachedPasswords.contains(newPassword))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
        if (encoder.matches(newPassword, currentPassword))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The passwords must be different!");
        return newPassword;
    }

    public void increaseFailedAttempts(User user, String path) {
        user.setFailedAttempt(user.getFailedAttempt() + 1);
        if (user.getFailedAttempt() > MAX_FAILED_ATTEMPTS) lockUser(user, path);
        userRepository.save(user);
    }

    public void lockUser(User user, String path) {
        user.setAccountNonLocked(false);
        auditService.logEvent(Action.BRUTE_FORCE, user.getEmail(), path, path);
        auditService.logEvent(Action.LOCK_USER, user.getEmail(), String.format("Lock user %s", user.getEmail()), path);
    }
}