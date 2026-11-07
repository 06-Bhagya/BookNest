package com.booknest.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.booknest.dto.LoginRequest;
import com.booknest.dto.LoginResponse;
import com.booknest.dto.RegisterRequest;
import com.booknest.dto.VerifyOtpRequest;
import com.booknest.service.UserService;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {

        userService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Registration successful. Check your email for OTP.");
    }
    
    
    //Email otp verification

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        String result = userService.verifyOtp(
                request.getEmail(), request.getOtp());

        return ResponseEntity.ok(result);
        
    } 
        
        //login using jwt
        
        
        @PostMapping("/login")
        public ResponseEntity<LoginResponse> login(
                @Valid @RequestBody LoginRequest request) {

            String token = userService.login(
                    request.getEmail(),
                    request.getPassword());

            return ResponseEntity.ok(
                    new LoginResponse(token, "Login successful"));
        }
    }
 