package java.com.codeofcarbon.account;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class MockUser {
    private String name;
    private String lastname;
    private String email;
    private String password;

    public MockUser(MockUser another) {
        this(another.name, another.lastname, another.email, another.password);
    }

    public MockUser(String name, String lastname, String email, String password) {
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
    }

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }

    public MockUser setName(String name) {
        this.name = name;
        return this;
    }

    public MockUser setLastname(String lastname) {
        this.lastname = lastname;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public MockUser setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public MockUser setPassword(String password) {
        this.password = password;
        return this;
    }
}