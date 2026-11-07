package com.booknest.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.booknest.entity.user;

public interface UserRepository extends JpaRepository<user, Long> {

	Optional<user>findByEmail(String email);  //Bcz during login we need to find the user using their email
	
}
