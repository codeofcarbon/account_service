package com.codeofcarbon.account.security;

import com.codeofcarbon.account.model.Action;
import com.codeofcarbon.account.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final AuditService auditService;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        var requestPath = request.getServletPath();
        var deniedUser = request.getUserPrincipal().getName();
        auditService.logEvent(Action.ACCESS_DENIED, deniedUser, requestPath, requestPath);
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied!");
    }
}