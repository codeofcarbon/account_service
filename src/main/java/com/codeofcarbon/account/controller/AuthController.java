package com.codeofcarbon.account.controller;

import com.codeofcarbon.account.model.User;
import com.codeofcarbon.account.model.dto.UserDTO;
import com.codeofcarbon.account.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@ResponseStatus(HttpStatus.OK)
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final HttpServletRequest request;

    @PostMapping("/signup")
    public UserDTO signUp(@Validated @RequestBody User user) {
        return userService.addNewUser(user, request.getRequestURI());
    }

    @PostMapping("/changepass")
    public Map<String, String> changePassword(@AuthenticationPrincipal UserDetails user,
                                              @RequestBody Map<String, String> req) {
        return userService.updatePassword(req, user.getUsername(), request.getRequestURI());
    }
}