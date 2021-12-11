package codeofcarbon.account.service;

import codeofcarbon.account.exception.*;
import codeofcarbon.account.model.Role;
import codeofcarbon.account.model.User;
import codeofcarbon.account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        user.grantAuthority(Role.USER);
        return userRepository.save(user);
    }

    @Transactional
    public void updatePassword(String newPassword, User user) {
        var validPassword = checkPasswordRequirements(newPassword, user.getPassword());
        user.setPassword(encoder.encode(validPassword));
        userRepository.save(user);
    }

    private String checkPasswordRequirements(String newPassword, String currentPassword) {
        if (newPassword.length() < 12) throw new ShortPasswordException();
        if (breachedPasswords.contains(newPassword)) throw new BreachedPasswordException();
        if (encoder.matches(newPassword, currentPassword)) throw new IdenticalPasswordException();
        return newPassword;
    }
}