package com.booknest.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="users")
public class user {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	
	@Column(unique = true, nullable=false)
	private String email;
	private String password;
	private String role;
	private boolean verified;
	
	//Email otp verification
	
	private String verificationOtp;
	private LocalDateTime otpExpiry;
	
	

}
