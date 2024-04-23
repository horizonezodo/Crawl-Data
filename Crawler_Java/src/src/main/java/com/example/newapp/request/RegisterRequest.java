package com.example.newapp.request;


import lombok.Data;

@Data
public class RegisterRequest {
    String username;
    String email;
    String user_role;
}
