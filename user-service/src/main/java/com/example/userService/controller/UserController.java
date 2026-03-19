package com.example.userService.controller;

import com.example.userService.dto.UserDto;
import com.example.userService.model.User;
import com.example.userService.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
            System.out.println(id);
            User user = userService.findById(id);
            return ResponseEntity.ok( new UserDto(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName()
            ));
    }

    @GetMapping("/allUsers")
    public ResponseEntity<?> getAllUsers(){
            List<User> allUsers = userService.findAllUsers();
            if(!allUsers.isEmpty())
                return ResponseEntity.ok(allUsers);
            else
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("There are no users at all in database");
    }

    @PostMapping("/user")
    public ResponseEntity<?> addUser(@Valid @RequestBody User user){
        System.out.println(user);
        userService.addUser(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                    .body(user);
    }

    @PostMapping("/multipleUser")
    public ResponseEntity<?> addMultipleUsers( @RequestBody @Valid List<User> multipleUsers){
        System.out.println(multipleUsers);
        userService.addMultipleUsers(multipleUsers);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(multipleUsers);
    }

    @DeleteMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUserById(@PathVariable Integer id) {
        boolean deleted = userService.deleteUserById(id);
        return ResponseEntity.ok("User is deleted");
    }

}
