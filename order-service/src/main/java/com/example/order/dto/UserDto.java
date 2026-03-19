package com.example.order.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserDto {
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;

}

