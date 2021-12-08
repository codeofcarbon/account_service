package codeofcarbon.account.model.dto;

import codeofcarbon.account.model.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDTO {
    public long id;
    private final String name;
    private final String lastname;
    private final String email;

    public static UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .lastname(user.getLastname())
                .email(user.getEmail().toLowerCase())
                .build();
    }
}
