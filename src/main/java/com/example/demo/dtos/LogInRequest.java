package com.example.demo.dtos;

import lombok.Data;

@Data
public class LogInRequest {
    private String email;
    private String password;
}
