package codeofcarbon.account.controller;

import codeofcarbon.account.model.User;
import codeofcarbon.account.model.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class EmployeeController {

    @GetMapping("/empl/payment")
    public ResponseEntity<UserDTO> authenticate(@AuthenticationPrincipal UserDetails details) {
        return ResponseEntity.ok(UserDTO.mapToUserDTO((User) details));
    }
}