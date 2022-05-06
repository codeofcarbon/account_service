package com.codeofcarbon.account.security;

import com.codeofcarbon.account.model.Action;
import com.codeofcarbon.account.service.AuditService;
import com.codeofcarbon.account.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final AuditService auditService;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        var currentRedirectPath = request.getServletPath();
        var failedUser = UserService.notFoundEmail == null ? UserService.foundEmail : UserService.notFoundEmail;
        var requestPath = UserService.requestPath;
        requestPath = requestPath.endsWith("/") ? requestPath.substring(0, requestPath.length() - 1) : requestPath;

        if ("/error".equals(currentRedirectPath)) {
            auditService.logEvent(Action.LOGIN_FAILED, failedUser, requestPath, requestPath);
            UserService.notFoundEmail = null;
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized!");
    }
}