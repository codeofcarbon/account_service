package com.codeofcarbon.account.controller;

import com.codeofcarbon.account.model.Role;
import com.codeofcarbon.account.model.dto.UserDTO;
import com.codeofcarbon.account.repository.UserRepository;
import com.codeofcarbon.account.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/admin/user")
@ResponseStatus(HttpStatus.OK)
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final UserRepository userRepository;
    private final HttpServletRequest request;

    @GetMapping()
    public List<UserDTO> getUsersData() {
        return adminService.getAllUsersData();
    }

    @DeleteMapping("/{email}")
    public Map<String, String> deleteUser(@AuthenticationPrincipal UserDetails admin,
                                          @PathVariable(name = "email") String userEmail) {
        var user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));
        adminService.validateRole(user, Role.ROLE_ADMINISTRATOR, AdminService.Operation.DELETE);
        return adminService.removeUser(user, request.getRequestURI(), admin.getUsername());
    }

    @PutMapping("/role")
    public UserDTO changeUserRole(@AuthenticationPrincipal UserDetails admin,
                                  @RequestBody Map<String, String> req) {
        var user = userRepository.findByEmailIgnoreCase(req.get("user"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));
        var operation = Arrays.stream(AdminService.Operation.values())
                .filter(op -> op.name().equals(req.get("operation")))
                .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operation aborted"));
        var role = adminService.validateRole(user, Role.valueOf("ROLE_" + req.get("role")), operation);
        return adminService.grantOrRevoke(user, operation, role, request.getRequestURI(), admin.getUsername());
    }

    @PutMapping("/access")
    public Map<String, String> changeUserAccess(@AuthenticationPrincipal UserDetails admin,
                                                @RequestBody Map<String, String> req) {
        var user = userRepository.findByEmailIgnoreCase(req.get("user"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));
        var operation = Arrays.stream(AdminService.Operation.values())
                .filter(op -> op.name().equals(req.get("operation")))
                .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operation aborted"));
        adminService.validateRole(user, Role.ROLE_ADMINISTRATOR, operation);
        return adminService.lockOrUnlock(user, operation, request.getRequestURI(), admin.getUsername());
    }
}