package codeofcarbon.account.service;

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
        try {
            return userRepository.findByEmail(email.toLowerCase()).orElseThrow(Exception::new);
        } catch (Exception ignored) {
                return null;
        }
    }

    @Transactional
    public User addNewUser(User user) {
        if (userRepository.findAll().stream()
                .anyMatch(u -> user.getEmail().equalsIgnoreCase(u.getEmail()))) new Exception();
        var validPassword = checkPasswordRequirements(user.getPassword(), "");
        user.setPassword(encoder.encode(validPassword));
        user.setEmail(user.getEmail().toLowerCase());
        user.grantAuthority(Role.USER);
        return userRepository.save(user);
    }

    public String checkPasswordRequirements(String newPassword, String currentPassword) {
        if (newPassword.length() < 12) new Exception();
        if (breachedPasswords.contains(newPassword)) new Exception();
        if (encoder.matches(newPassword, currentPassword)) new Exception();
        return newPassword;
    }

    @Transactional
    public void updatePassword(String newPassword, User user) {
        var validPassword = checkPasswordRequirements(newPassword, user.getPassword());
        user.setPassword(encoder.encode(validPassword));
        userRepository.save(user);
    }
}