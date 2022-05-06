package com.codeofcarbon.account.controller;

import com.codeofcarbon.account.model.User;
import com.codeofcarbon.account.model.dto.UserDTO;
import com.codeofcarbon.account.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping()
    public ResponseEntity<Object> getUsersData() {
        var response = adminService.getAllUsersData();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Object> deleteUser(@AuthenticationPrincipal UserDetails admin,
                                             @PathVariable(name = "email") String userEmail,
                                             HttpServletRequest request) {
        adminService.removeUser(userEmail, request.getServletPath(), admin.getUsername());
        return ResponseEntity.ok(Map.of("user", userEmail, "status", "Deleted successfully!"));
    }

    @PutMapping("/role")
    public ResponseEntity<Object> grantOrRevokeRole(@AuthenticationPrincipal UserDetails admin,
                                                    @RequestBody Map<String, String> commandJson,
                                                    HttpServletRequest request) {
        var response = adminService.prepareOperationOnUser(commandJson, request.getServletPath(), admin.getUsername());
        var modifiedUser = UserDTO.mapToUserDTO((User) response);
        return ResponseEntity.ok(modifiedUser);
    }

    @PutMapping("/access")
    public ResponseEntity<Object> lockUnlockUser(@AuthenticationPrincipal UserDetails admin,
                                                 @RequestBody Map<String, String> commandJson,
                                                 HttpServletRequest request) {
        var response = adminService.prepareOperationOnUser(commandJson, request.getServletPath(), admin.getUsername());
        return ResponseEntity.ok(response);
    }
}