package com.codeofcarbon.account.service;

import com.codeofcarbon.account.model.*;
import com.codeofcarbon.account.model.dto.UserDTO;
import com.codeofcarbon.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final UserRepository userRepository;

    public enum Operation {
        GRANT, REMOVE, LOCK, UNLOCK, DELETE
    }

    public List<UserDTO> getAllUsersData() {
        return userRepository.findAllOrderById().stream()
                .map(UserDTO::mapToUserDTO)
                .collect(Collectors.toList());
    }

    public Map<String, String> removeUser(User user, String path,
                                          String adminEmail) {
        userRepository.delete(user);
        auditService.logEvent(Action.DELETE_USER, adminEmail, user.getEmail(), path);
        return Map.of("user", user.getEmail(), "status", "Deleted successfully!");
    }

    public Map<String, String> lockOrUnlock(User user, Operation operation,
                                            String path, String adminEmail) {
        var logMessage = (Operation.LOCK == operation ? "Lock user " : "Unlock user ") + user.getEmail();
        var responseInfo = "User " + user.getEmail() + (Operation.LOCK == operation ? " locked!" : " unlocked!");
        var action = Operation.LOCK == operation ? Action.LOCK_USER : Action.UNLOCK_USER;

        if (Operation.UNLOCK == operation) user.setFailedAttempt(0);
        user.setAccountNonLocked(Operation.UNLOCK == operation);

        userRepository.save(user);
        auditService.logEvent(action, adminEmail, logMessage, path);
        return Map.of("status", responseInfo);
    }

    public UserDTO grantOrRevoke(User user, Operation operation,
                                 Role role, String path, String adminEmail) {
        var logMessage = Operation.GRANT == operation ? "Grant role %s to %s" : "Remove role %s from %s";
        var action = Operation.GRANT == operation ? Action.GRANT_ROLE : Action.REMOVE_ROLE;
        var requestedRole = role.name().split("_")[1];

        if (Operation.GRANT == operation) user.grantAuthority(role);
        if (Operation.REMOVE == operation) user.getRoles().remove(role);

        userRepository.save(user);
        auditService.logEvent(action, adminEmail, String.format(logMessage, requestedRole, user.getEmail()), path);
        return UserDTO.mapToUserDTO(user);
    }

    public Role validateRole(User user, Role role, AdminService.Operation operation) {
        Arrays.stream(Role.values())
                .filter(r -> r.name().equals(role.name()))
                .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!"));
        switch (operation) {
            case GRANT:
                if (user.getRoles().contains(Role.ROLE_ADMINISTRATOR))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "The user cannot combine administrative and business roles!");
                break;
            case REMOVE:
                if (role == Role.ROLE_ADMINISTRATOR)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
                if (!user.getRoles().contains(role))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user does not have a role!");
                if (user.getRoles().size() == 1)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user must have at least one role!");
                break;
            case DELETE:
                if (user.getRoles().contains(Role.ROLE_ADMINISTRATOR)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR!");
                }
                break;
            case LOCK:
            case UNLOCK:
                if (user.getRoles().contains(Role.ROLE_ADMINISTRATOR))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");
        }
        return role;
    }
}