package com.booknest.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {                //validation and exception Handling
	
	    @ExceptionHandler(BookNotFoundException.class)
	    public ResponseEntity<Map<String, String>> handleBookNotFound(
	            BookNotFoundException ex) {

	        Map<String, String> error = new HashMap<>();
	        error.put("error", "Not Found");
	        error.put("message", ex.getMessage());

	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	    }

	    @ExceptionHandler(MethodArgumentNotValidException.class)
	    public ResponseEntity<Map<String, String>> handleValidationErrors(
	            MethodArgumentNotValidException ex) {

	        Map<String, String> errors = new HashMap<>();

	        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
	                errors.put(fieldError.getField(),
	                        fieldError.getDefaultMessage()));

	        return ResponseEntity.badRequest().body(errors);
	        
	    }
	    @ExceptionHandler(Exception.class)
	    public ResponseEntity<Map<String, String>> handleUnexpectedError(
	            Exception ex) {

	        Map<String, String> error = new HashMap<>();
	        error.put("error", "Internal Server Error");
	        error.put("message", "Something went wrong. Please try again later.");

	        return ResponseEntity
	                .status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(error);
	    }
	    }

	

