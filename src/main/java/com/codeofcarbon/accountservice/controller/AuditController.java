package com.codeofcarbon.accountservice.controller;

import com.codeofcarbon.accountservice.model.dto.EventLogDTO;
import com.codeofcarbon.accountservice.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@ResponseStatus(HttpStatus.OK)
@RequiredArgsConstructor
public class AuditController {
    private final AuditService auditService;

    @GetMapping("/api/security/events")
    public List<EventLogDTO> showSecurityEvents() {
        return auditService.getSecurityEvents();
    }
}