package com.example.userService.controller;

import com.example.userService.dto.AuthRequest;
import com.example.userService.dto.AuthResponse;
import com.example.userService.exception.UserNotFoundException;
import com.example.userService.model.User;
import com.example.userService.repository.UserRepository;
import com.example.userService.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword()
                )
        );
        User user = userRepository.findByEmail(authRequest.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        final String jwt = jwtUtil.generateToken(user.getId(), user.getEmail());

        return ResponseEntity.ok(new AuthResponse(jwt));
    }
}
