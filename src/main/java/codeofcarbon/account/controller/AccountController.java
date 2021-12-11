package codeofcarbon.account.controller;

import codeofcarbon.account.model.User;
import codeofcarbon.account.model.dto.UserDTO;
import codeofcarbon.account.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api")
public class AccountController {
    private final UserService userService;

    @Autowired
    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signUp(@Validated @RequestBody User user) {
        var newUser = userService.addNewUser(user);
        return ResponseEntity.ok(UserDTO.mapToUserDTO(newUser));
    }
}
