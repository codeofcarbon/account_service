package com.codeofcarbon.accountservice.security;

import com.codeofcarbon.accountservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

@Configuration
@RequiredArgsConstructor
public class AppAuthSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {
    private final UserRepository userRepository;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        userRepository.findByEmailIgnoreCase(event.getAuthentication().getName())
                .ifPresent(user -> {
                    user.setFailedAttempt(0);
                    userRepository.save(user);
                });
    }
}