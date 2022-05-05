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
    private final UserService userService;
    private final UserRepository userRepository;

    public List<UserDTO> getAllUsersData() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparingLong(User::getId))
                .map(UserDTO::mapToUserDTO)
                .collect(Collectors.toList());
    }

    public void removeUser(String userEmail) {
        User userToRemove;
        var userDetails = userService.loadUserByUsername(userEmail);

        if (userDetails != null) userToRemove = (User) userDetails;
        else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");

        if (userToRemove.getRoles().contains(Role.ROLE_ADMINISTRATOR))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");

        userRepository.delete(userToRemove);
    }

    public User updateUserRoles(Map<String, String> command) {
        var userDetails = userService.loadUserByUsername(command.get("user"));
        if (userDetails == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");

        var user = (User) userDetails;
        var role = Arrays.stream(Role.values())
                .filter(r -> r.name().contains(command.get("role")))
                .findAny().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!"));
        var operation = Arrays.stream(Operation.values())
                .filter(op -> op.name().equals(command.get("operation"))).findFirst();

        if (operation.isPresent()) {
            switch (operation.get()) {
                case GRANT:
                    if (role != Role.ROLE_ADMINISTRATOR && !user.getRoles().contains(Role.ROLE_ADMINISTRATOR))
                        user.grantAuthority(role);
                    else throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "The user cannot combine administrative and business roles!");
                    break;
                case REMOVE:
                    if (role.equals(Role.ROLE_ADMINISTRATOR))
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
                    if (!user.getRoles().contains(role))
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user does not have a role!");
                    if (user.getRoles().size() == 1)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user must have at least one role!");
                    user.getRoles().remove(role);
            }
        }
        return userRepository.save(user);
    }
}

enum Operation {
    GRANT, REMOVE
}