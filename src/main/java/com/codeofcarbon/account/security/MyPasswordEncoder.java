package com.codeofcarbon.account.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class MyPasswordEncoder implements PasswordEncoder {
    final BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2A, 13);

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return bCrypt;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return bCrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return bCrypt.matches(rawPassword, encodedPassword);
    }
}
