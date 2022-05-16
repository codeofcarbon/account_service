package com.codeofcarbon.account.controller;

import com.codeofcarbon.account.model.dto.EventLogDTO;
import com.codeofcarbon.account.service.AuditService;
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