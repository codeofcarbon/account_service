package com.codeofcarbon.account.controller;

import com.codeofcarbon.account.model.dto.UserDTO;
import com.codeofcarbon.account.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping()
    public ResponseEntity<Object> getUsersData() {
        return ResponseEntity.ok(adminService.getAllUsersData());
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Object> deleteUser(@PathVariable String email) {
        adminService.removeUser(email);
        return ResponseEntity.ok(Map.of("user", email, "status", "Deleted successfully!"));
    }

    @PutMapping("/role")
    public ResponseEntity<Object> editUserRoles(@RequestBody Map<String, String> command) {
        var modifiedUser = adminService.updateUserRoles(command);
        return ResponseEntity.ok(UserDTO.mapToUserDTO(modifiedUser));
    }
}