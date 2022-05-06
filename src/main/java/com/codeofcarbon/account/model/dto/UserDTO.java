package com.codeofcarbon.account.model.dto;

import com.codeofcarbon.account.model.Role;
import com.codeofcarbon.account.model.User;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class UserDTO {
    private final long id;
    private final String name;
    private final String lastname;
    private final String email;
    private final List<Role> roles;

    public static UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .lastname(user.getLastname())
                .email(user.getEmail().toLowerCase())
                .roles(user.getRoles().stream().sorted().collect(Collectors.toList()))
                .build();
    }
}