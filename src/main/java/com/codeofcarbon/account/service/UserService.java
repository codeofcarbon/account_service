package com.codeofcarbon.account.service;

import com.codeofcarbon.account.model.Action;
import com.codeofcarbon.account.model.Role;
import com.codeofcarbon.account.model.User;
import com.codeofcarbon.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    public static String foundEmail;
    public static String notFoundEmail;
    public static String requestPath;
    private final AuditService auditService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2A, 13);
    private final List<String> breachedPasswords =
            List.of("PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
                    "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
                    "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");

    @Override
    public UserDetails loadUserByUsername(String email) {
        requestPath = ServletUriComponentsBuilder.fromCurrentRequest().build().getPath();

        var details = userRepository.findByEmail(email.toLowerCase());
        if (details != null) {
            foundEmail = email.toLowerCase();
            return details;
        } else {
            notFoundEmail = email.toLowerCase();
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }
    }

    public User addNewUser(User user, String requestPath) {
        var userToAdd = userRepository.findAll().stream()
                .filter(u -> user.getEmail().equalsIgnoreCase(u.getEmail())).findAny();
        if (userToAdd.isPresent()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User exist!");

        var validPassword = checkPasswordRequirements(user.getPassword(), "");
        user.setPassword(encoder.encode(validPassword));
        user.setEmail(user.getEmail().toLowerCase());
        user.grantAuthority(userRepository.findAll().isEmpty() ? Role.ROLE_ADMINISTRATOR : Role.ROLE_USER);

        auditService.logEvent(Action.CREATE_USER, null, user.getEmail(), requestPath);
        return userRepository.save(user);
    }

    public void updatePassword(String newPassword, String userEmail, String requestPath) {
        var user = (User) loadUserByUsername(userEmail);
        var validPassword = checkPasswordRequirements(newPassword, user.getPassword());
        user.setPassword(encoder.encode(validPassword));

        userRepository.save(user);

        auditService.logEvent(Action.CHANGE_PASSWORD, user.getEmail(), user.getEmail(), requestPath);
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
}