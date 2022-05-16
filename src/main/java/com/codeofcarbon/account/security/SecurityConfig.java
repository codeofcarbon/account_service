package com.codeofcarbon.account.security;

import com.codeofcarbon.account.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final AppAuthenticationEntryPoint authenticationEntryPoint;
    private final AppAccessDeniedHandler accessDeniedHandler;
    private final AppPasswordEncoder encoder;
    private final UserService userDetailsService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(encoder);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.httpBasic()
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .csrf().disable().headers().frameOptions().disable()
                .and()
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/actuator/shutdown", "/api/auth/signup").permitAll()
                .antMatchers("/api/auth/changepass").authenticated()
                .antMatchers("/api/admin/**").hasRole("ADMINISTRATOR")
                .antMatchers("/api/empl/**").hasAnyRole("ACCOUNTANT", "USER")
                .antMatchers("/api/security/**").hasRole("AUDITOR")
                .antMatchers("/api/acct/**").hasRole("ACCOUNTANT")
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}