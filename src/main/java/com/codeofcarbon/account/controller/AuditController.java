package com.codeofcarbon.account.controller;

import com.codeofcarbon.account.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
public class AuditController {
    private final AuditService auditService;

    @GetMapping("/events")
    public ResponseEntity<Object> showSecurityEvents() {
        var response = auditService.getSecurityEvents();
        return ResponseEntity.ok(response);
    }
}