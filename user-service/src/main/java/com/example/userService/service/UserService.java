package com.example.userService.service;

import com.example.userService.exception.DuplicateUserException;
import com.example.userService.exception.UserNotFoundException;
import com.example.userService.model.User;
import com.example.userService.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
public class UserService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User findById(Integer id) {
        System.out.println("HI ra rakesh");
        return repo.findById(id).orElseThrow(()-> new UserNotFoundException("User not found with id:" + id));
    }

    public User findByEmail(String email) {
        System.out.println("HI ra rakesh");
        return repo.findByEmail(email).orElseThrow(()-> new UserNotFoundException("User not found with id:" + email));
    }

    public void addUser(@Valid User user) {
        if(repo.existsByEmail(user.getEmail()))
            throw new DuplicateUserException("User already exists");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        repo.save(user);
    }

    public boolean deleteUserById(Integer id) {
        if(repo.existsById(id)){
            repo.deleteById(id);
            return true;
        }
        else
            throw new UserNotFoundException("User not found");
    }

    public List<User> findAllUsers() {
        return repo.findAll();
    }

    public void addMultipleUsers(@Valid List<User> multipleUsers) {
        multipleUsers.forEach(user ->
                user.setPassword(passwordEncoder.encode(user.getPassword()))
        );
        repo.saveAll(multipleUsers);
    }
}
