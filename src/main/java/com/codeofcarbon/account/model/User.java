package com.codeofcarbon.account.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long id;
    @NotBlank(message = "name is required")
    private String name;
    @NotBlank(message = "lastname is required")
    private String lastname;
    @Email(regexp = ".*@acme.com$")
    @NotBlank(message = "email is required")
    private String email;
    @NotBlank(message = "password is required")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @JsonIgnore
    private boolean accountNonExpired;
    @JsonIgnore
    private boolean accountNonLocked;
    @JsonIgnore
    private boolean credentialsNonExpired;
    @JsonIgnore
    private boolean enabled;
    @JsonIgnore
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Role> roles = new ArrayList<>();

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.toString()))
                .collect(Collectors.toList());
    }

    public void grantAuthority(Role role) {
        roles.add(role);
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;//accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;//accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;//credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return true;//enabled;
    }
}