package codeofcarbon.account.controller;

import codeofcarbon.account.model.User;
import codeofcarbon.account.model.dto.UserDTO;
import codeofcarbon.account.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api")
public class AccountController {
    private final UserService userService;

    @Autowired
    public AccountController(UserService userService) {
        this.userService = userService;
    }

    //  new user registering
    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signUp(@Validated @RequestBody User user) {
        var newUser = userService.addNewUser(user);
        return ResponseEntity.ok(UserDTO.mapToUserDTO(newUser));
    }

    //  changing user password after successful authentication
    @PostMapping("/changepass")
    public ResponseEntity<Object> changePassword(@AuthenticationPrincipal UserDetails details,
                                                 @RequestBody Map<String, String> request) {
        if (details == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        var newPassword = request.get("new_password");
        userService.checkPasswordRequirements(newPassword, details.getPassword());
        userService.updatePassword(newPassword, (User) details);
        return ResponseEntity.ok(Map.of(
                "email", details.getUsername(),
                "status", "The password has been updated successfully"));
    }
}
