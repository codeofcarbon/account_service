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

    public List<UserDTO> getAllUsersData() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparingLong(User::getId))
                .map(UserDTO::mapToUserDTO)
                .collect(Collectors.toList());
    }

    public void removeUser(String userEmail, String requestPath, String adminEmail) {
        User userToRemove;
        var userDetails = userService.loadUserByUsername(userEmail);

        if (userDetails != null) userToRemove = (User) userDetails;
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");

        if (userToRemove.getRoles().contains(Role.ROLE_ADMINISTRATOR))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");

        auditService.logEvent(Action.DELETE_USER, adminEmail, userEmail, requestPath);
        userRepository.delete(userToRemove);
    }

    public Object prepareOperationOnUser(Map<String, String> command, String requestPath, String adminEmail) {
        var userDetails = userService.loadUserByUsername(command.get("user"));
        if (userDetails == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        var user = (User) userDetails;
        var operation = Arrays.stream(Operation.values())
                .filter(op -> op.name().equals(command.get("operation")))
                .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operation aborted"));

            var role = Arrays.stream(Role.values())
                    .filter(r -> r.name().contains(command.get("role")))
                    .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!"));
            return performTheOperation(operation, user, role, requestPath, adminEmail);
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
        }
        return userRepository.save(user);
    }
}