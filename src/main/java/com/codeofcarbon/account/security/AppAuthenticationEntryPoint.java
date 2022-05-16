package com.codeofcarbon.account.security;

import com.codeofcarbon.account.model.Action;
import com.codeofcarbon.account.model.Role;
import com.codeofcarbon.account.repository.UserRepository;
import com.codeofcarbon.account.service.AuditService;
import com.codeofcarbon.account.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class AppAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final AuditService auditService;
    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        var authorization = request.getHeader("Authorization");
        if (authorization != null) {
            var username = new String(Base64.getDecoder().decode(authorization.split("\\s+")[1])).split(":")[0];
            var path = ServletUriComponentsBuilder.fromCurrentRequest().build().getPath();

            if (!userRepository.existsUserByEmailIgnoreCase(username))
                auditService.logEvent(Action.LOGIN_FAILED, username, path, path);
            else {
                userRepository.findByEmailIgnoreCase(username).ifPresent(user -> {
                    if (user.isAccountNonLocked()) {
                        auditService.logEvent(Action.LOGIN_FAILED, user.getEmail(), path, path);
                        if (!user.getRoles().contains(Role.ROLE_ADMINISTRATOR))
                            userService.increaseFailedAttempts(user, path);
                    }
                });
            }
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
}