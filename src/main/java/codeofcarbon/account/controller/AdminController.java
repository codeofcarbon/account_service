package codeofcarbon.account.controller;

import codeofcarbon.account.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
}