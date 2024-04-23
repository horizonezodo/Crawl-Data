package com.example.newapp.response;

import lombok.Data;

@Data
public class LoginResponse {
    String email;
    String username;
    String user_role;
    String user_image;
    String token;
    String refreshToken;
}
