package com.codeofcarbon.account.service;

import com.codeofcarbon.account.model.*;
import com.codeofcarbon.account.model.dto.UserDTO;
import com.codeofcarbon.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {
    private final AuditService auditService;
    private final UserService userService;
    private final UserRepository userRepository;
    public enum Operation {
        GRANT, REMOVE, LOCK, UNLOCK
    }

    public List<UserDTO> getAllUsersData() {
        return userRepository.findAllOrderById().stream()
                .map(UserDTO::mapToUserDTO)
                .collect(Collectors.toList());
    }

    public void removeUser(String userEmail, String requestPath, String adminEmail) {
        var user = userRepository.findByEmail(userEmail.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));
        if (user.getRoles().contains(Role.ROLE_ADMINISTRATOR))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
        auditService.logEvent(Action.DELETE_USER, adminEmail, userEmail, requestPath);
        userRepository.delete(user);
    }

    public Object prepareOperationOnUser(Map<String, String> command, String requestPath, String adminEmail) {
        var user = userRepository.findByEmail(command.get("user").toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));
        var operation = Arrays.stream(Operation.values())
                .filter(op -> op.name().equals(command.get("operation")))
                .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operation aborted"));

        if (Operation.LOCK == operation || Operation.UNLOCK == operation) {
            var updatedUser = performTheOperation(operation, user, null, requestPath, adminEmail);
            return Map.of("status", String.format("User %s %s!", updatedUser.getEmail(),
                    Operation.LOCK == operation ? "locked" : "unlocked"));
        } else {
            var role = Arrays.stream(Role.values())
                    .filter(r -> r.name().contains(command.get("role")))
                    .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!"));
            return performTheOperation(operation, user, role, requestPath, adminEmail);
        }
    }

    private User performTheOperation(Operation operation, User user, @Nullable Role role,
                                     String requestPath, String adminEmail) {
        switch (operation) {
            case GRANT:
                assert role != null;
                if (role == Role.ROLE_ADMINISTRATOR || user.getRoles().contains(Role.ROLE_ADMINISTRATOR))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "The user cannot combine administrative and business roles!");

                user.grantAuthority(role);
                var message = String.format("Grant role %s to %s", role.name().split("_")[1], user.getEmail());
                auditService.logEvent(Action.GRANT_ROLE, adminEmail, message, requestPath);
                break;
            case REMOVE:
                assert role != null;
                if (role == Role.ROLE_ADMINISTRATOR)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
                if (!user.getRoles().contains(role))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user does not have a role!");
                if (user.getRoles().size() == 1)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user must have at least one role!");

                user.getRoles().remove(role);
                message = String.format("Remove role %s from %s", role.name().split("_")[1], user.getEmail());
                auditService.logEvent(Action.REMOVE_ROLE, adminEmail, message, requestPath);
                break;
            case LOCK:
                if (user.getRoles().contains(Role.ROLE_ADMINISTRATOR))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");

                user.setAccountNonLocked(false);
                message = String.format("Lock user %s", user.getEmail());
                auditService.logEvent(Action.LOCK_USER, user.getEmail(), message, requestPath);
                break;
            case UNLOCK:
                user.setAccountNonLocked(true);
                user.setFailedAttempt(0);
                message = String.format("Unlock user %s", user.getEmail());
                auditService.logEvent(Action.UNLOCK_USER, adminEmail, message, requestPath);
        }
        return userRepository.save(user);
    }
}