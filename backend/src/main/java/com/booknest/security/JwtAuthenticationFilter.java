package com.booknest.security;

	import java.io.IOException;
	import java.util.List;

	import jakarta.servlet.FilterChain;
	import jakarta.servlet.ServletException;
	import jakarta.servlet.http.HttpServletRequest;
	import jakarta.servlet.http.HttpServletResponse;

	import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
	import org.springframework.security.core.authority.SimpleGrantedAuthority;
	import org.springframework.security.core.context.SecurityContextHolder;
	import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
	import org.springframework.web.filter.OncePerRequestFilter;
	import org.springframework.stereotype.Component;

	import com.booknest.entity.user;
	import com.booknest.repository.UserRepository;

	import io.jsonwebtoken.JwtException;

	
	//The filter reads the token from the Authorization header, validates it, and authenticates the user.
	@Component
	public class JwtAuthenticationFilter extends OncePerRequestFilter {

	    private final JwtService jwtService;
	    private final UserRepository userRepository;

	    public JwtAuthenticationFilter(
	            JwtService jwtService,
	            UserRepository userRepository) {
	        this.jwtService = jwtService;
	        this.userRepository = userRepository;
	    }

	    @Override
	    protected void doFilterInternal(
	            HttpServletRequest request,
	            HttpServletResponse response,
	            FilterChain filterChain)
	            throws ServletException, IOException {

	        String authHeader = request.getHeader("Authorization");
	        
	        System.out.println("Authorization header present: "
	                + (authHeader != null));

	        if (authHeader != null) {
	            System.out.println("Bearer token provided: "
	                    + authHeader.startsWith("Bearer "));
	        }

	        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	            filterChain.doFilter(request, response);
	            return;
	        }

	        String token = authHeader.substring(7);

	        try {
	            String email = jwtService.extractEmail(token);

	            if (email != null
	                    && SecurityContextHolder.getContext()
	                            .getAuthentication() == null) {

	                user user = userRepository.findByEmail(email)
	                        .orElse(null);

	                if (user != null && user.isVerified()) {

	                    var authentication =
	                            new UsernamePasswordAuthenticationToken(
	                                    user.getEmail(),
	                                    null,
	                                    List.of(new SimpleGrantedAuthority(
	                                            "ROLE_" + user.getRole())));

	                    authentication.setDetails(
	                            new WebAuthenticationDetailsSource()
	                                    .buildDetails(request));

	                    SecurityContextHolder.getContext()
	                            .setAuthentication(authentication);
	                }
	            }

	        } catch (JwtException | IllegalArgumentException e) {
	            SecurityContextHolder.clearContext();
	        }

	        filterChain.doFilter(request, response);
	    }
	}

