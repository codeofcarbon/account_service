package com.codeofcarbon.accountservice.model.dto;

import com.codeofcarbon.accountservice.model.*;
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
                .email(user.getEmail())
                .roles(user.getRoles().stream().sorted().collect(Collectors.toList()))
                .build();
    }
}