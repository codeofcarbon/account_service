package com.codeofcarbon.account.controller;

import com.codeofcarbon.account.model.User;
import com.codeofcarbon.account.model.dto.UserDTO;
import com.codeofcarbon.account.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signUp(@Validated @RequestBody User user,
                                          HttpServletRequest request) {
        var newUser = userService.addNewUser(user, request.getServletPath());
        return ResponseEntity.ok(UserDTO.mapToUserDTO(newUser));
    }

    @PostMapping("/changepass")
    public ResponseEntity<Object> changePassword(@AuthenticationPrincipal UserDetails details,
                                                 @RequestBody Map<String, String> requestJson,
                                                 HttpServletRequest request) {
        userService.updatePassword(requestJson.get("new_password"), ((User) details).getEmail(), request.getServletPath());
        return ResponseEntity.ok(Map.of("email", details.getUsername(),
                "status", "The password has been updated successfully"));
    }
}