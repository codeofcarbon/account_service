package com.codeofcarbon.accountservice.controller;

import com.codeofcarbon.accountservice.model.Role;
import com.codeofcarbon.accountservice.model.dto.UserDTO;
import com.codeofcarbon.accountservice.service.AdminService;
import com.codeofcarbon.accountservice.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/admin/user")
@ResponseStatus(HttpStatus.OK)
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final HttpServletRequest request;
    private final Validator validator;

    @GetMapping()
    public List<UserDTO> getUsersData() {
        return adminService.getAllUsersData();
    }

    @DeleteMapping("/{email}")
    public Map<String, String> deleteUser(@AuthenticationPrincipal UserDetails admin,
                                          @PathVariable(name = "email") String userEmail) {
        var user = validator.validateUser(userEmail);
        validator.validateRole(user, Role.ROLE_ADMINISTRATOR.name(), AdminService.Operation.DELETE);
        return adminService.removeUser(user, request.getRequestURI(), admin.getUsername());
    }

    @PutMapping("/role")
    public UserDTO changeUserRole(@AuthenticationPrincipal UserDetails admin,
                                  @RequestBody Map<String, String> req) {
        var user = validator.validateUser(req.get("user"));
        var operation = validator.validateOperation(req.get("operation"));
        var role = validator.validateRole(user, "ROLE_" + req.get("role"), operation);
        return adminService.grantOrRevoke(user, operation, role, request.getRequestURI(), admin.getUsername());
    }

    @PutMapping("/access")
    public Map<String, String> changeUserAccess(@AuthenticationPrincipal UserDetails admin,
                                                @RequestBody Map<String, String> req) {
        var user = validator.validateUser(req.get("user"));
        var operation = validator.validateOperation(req.get("operation"));
        validator.validateRole(user, Role.ROLE_ADMINISTRATOR.name(), operation);
        return adminService.lockOrUnlock(user, operation, request.getRequestURI(), admin.getUsername());
    }
}