package com.codeofcarbon.account.security;

import com.codeofcarbon.account.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
//@EnableGlobalMethodSecurity(prePostEnabled = false, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final UserService detailsService;
    private final MyPasswordEncoder encoder;
    final DataSource dataSource;

    /*
//    @Autowired CustomAuthenticationEntryPoint authenticationEntryPoint;
//    @Autowired CustomAccessDeniedHandler accessDeniedHandler;
//    @Autowired UserService detailsService;
//    @Autowired MyPasswordEncoder encoder;
//    @Autowired DataSource dataSource;

//    private final CustomAuthenticationProvider customAuthenticationProvider;
//    private final CustomAuthenticationFailureHandler authenticationFailureHandler;
//    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;
//    private final AuditService auditService;

//    @Bean
//    public CustomAuthenticationEntryPoint authenticationEntryPoint() {
//        return new CustomAuthenticationEntryPoint(auditService);
//    }
//
//    @Bean
//    public CustomAccessDeniedHandler accessDeniedHandler() {
//        return new CustomAccessDeniedHandler(auditService);
//    }
*/

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(detailsService).passwordEncoder(encoder);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.httpBasic()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
                .and()
                .csrf().disable().headers().frameOptions().disable()
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/actuator/shutdown", "/api/auth/signup").permitAll()
                .antMatchers("/api/auth/changepass").authenticated()
                .antMatchers("/api/admin/**").hasRole("ADMINISTRATOR")
                .antMatchers("/api/empl/**").hasAnyRole("ACCOUNTANT", "USER")
                .antMatchers("/api/security/**").hasRole("AUDITOR")
                .antMatchers("/api/acct/**").hasRole("ACCOUNTANT")
                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}