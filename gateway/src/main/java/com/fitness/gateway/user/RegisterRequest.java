package com.fitness.gateway.user;

import lombok.Data;

@Data
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String keycloakId;
    private String email;
    private String password;
}
