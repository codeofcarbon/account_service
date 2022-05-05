package com.codeofcarbon.account.service;

import com.codeofcarbon.account.exception.*;
import com.codeofcarbon.account.model.Role;
import com.codeofcarbon.account.model.User;
import com.codeofcarbon.account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2A, 13);
    private final List<String> breachedPasswords =
            List.of("PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
                    "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
                    "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email.toLowerCase()).orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public User addNewUser(User user) {
        if (userRepository.findAll().stream()
                .anyMatch(u -> user.getEmail().equalsIgnoreCase(u.getEmail()))) throw new UserExistsException();
        var validPassword = checkPasswordRequirements(user.getPassword(), "");
        user.setPassword(encoder.encode(validPassword));
        user.setEmail(user.getEmail().toLowerCase());
        user.grantAuthority(Role.ROLE_USER);
        return userRepository.save(user);
    }

    @Transactional
    public void updatePassword(String newPassword, User user) {
        var validPassword = checkPasswordRequirements(newPassword, user.getPassword());
        user.setPassword(encoder.encode(validPassword));
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

}