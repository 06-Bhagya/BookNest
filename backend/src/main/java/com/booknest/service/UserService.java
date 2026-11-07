package com.booknest.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.booknest.dto.RegisterRequest;
import com.booknest.entity.user;
import com.booknest.repository.UserRepository;
import com.booknest.security.JwtService;

import org.springframework.security.authentication.BadCredentialsException;

@Service
public class UserService {

    private final UserRepository userRepository;     //register
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;        //otp mail verf
    private final JwtService jwtService;           //login using jwt

    private final SecureRandom random = new SecureRandom();

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.jwtService = jwtService;
    }

    public user register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        user user = new user();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword()));
        user.setRole("MEMBER");
        user.setVerified(false);
        
        
        //Emailotp verification

        String otp = String.format("%06d", random.nextInt(1000000));

        user.setVerificationOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        user savedUser = userRepository.save(user);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("BookNest Email Verification");
            message.setText(
                    "Hello " + user.getName()
                    + ",\n\nYour BookNest verification OTP is: " + otp
                    + "\nIt expires in 5 minutes."
                    + "\n\nDo not share this OTP with anyone.");

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to send verification email. Please try again.");
        }

        return savedUser;
    }

    public String verifyOtp(String email, String otp) {

        user user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.isVerified()) {
            return "Email is already verified";
        }

        if (user.getVerificationOtp() == null
                || !user.getVerificationOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        if (user.getOtpExpiry() == null
                || LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new RuntimeException("OTP has expired");
        }

        user.setVerified(true);
        user.setVerificationOtp(null);
        user.setOtpExpiry(null);

        userRepository.save(user);

        return "Email verified successfully";
    }   
        
        //user login usingJwt
        
        
    
        public String login(String email, String password) {

            user user = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new BadCredentialsException("Invalid email or password"));

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new BadCredentialsException("Invalid email or password");
            }

            if (!user.isVerified()) {
                throw new BadCredentialsException("Please verify your email first");
            }

            return jwtService.generateToken(user.getEmail());
            
            
            
            
        }
    
        
        
    }
