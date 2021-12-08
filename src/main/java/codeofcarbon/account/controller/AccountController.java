package codeofcarbon.account.controller;

import codeofcarbon.account.model.User;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api")
public class AccountController {

    @PostMapping("/auth/signup")
    public ResponseEntity<User> signUp(@Validated @RequestBody User user) {
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
