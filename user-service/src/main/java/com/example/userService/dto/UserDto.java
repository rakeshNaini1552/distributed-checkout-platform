package com.example.userService.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDto {
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;

}

