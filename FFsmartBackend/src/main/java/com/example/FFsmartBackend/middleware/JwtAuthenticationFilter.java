package com.example.FFsmartBackend.middleware;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.FFsmartBackend.lib.utils.JwtService;
import com.example.FFsmartBackend.models.User;
import com.example.FFsmartBackend.services.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JwtAuthenticationFilter extends OncePerRequestFilter to ensure that it is applied
 * exactly once per request. This filter checks the incoming request for a valid JWT,
 * and if valid, places the authenticated User in the SecurityContext.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    // Replace UserRepository with UserService
    @Autowired
    private UserService userService;

    /**
     * doFilterInternal checks whether the request is for /login or /signup and, if so,
     * bypasses JWT checks (allowing open access). Otherwise, it looks for a JWT in cookies,
     * validates it, and sets up the authentication in Spring Security's context if valid.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
                                 throws ServletException, IOException {

        // Get the request path (e.g., /api/auth/login)
        String requestURI = request.getRequestURI();
        System.out.println("Request URI: " + requestURI);

        // 1. Bypass authentication for login or signup endpoints.
        if (requestURI.equals("/api/auth/login") || requestURI.equals("/api/auth/signup")) {
            System.out.println("Skipping authentication for login and signup URIs: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Retrieve the JWT token from cookies (if present).
        String token = getJwtFromCookies(request);
        if (token != null) {
            System.out.println("JWT token found: " + token);

            // Validate the token and retrieve the user ID.
            String userId = jwtService.validateTokenAndGetUserId(token);

            // 3. If userId is valid and no authentication is set yet, load User from JSON (UserService).
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<User> userOptional = userService.findById(userId);
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    System.out.println("Authenticated user: " + user.getUsername());

                    // Create a UsernamePasswordAuthenticationToken with no authorities.
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, null);

                    // Set it in Spring Security's context.
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    System.out.println("User not found for ID: " + userId);
                }
            } else {
                // If token is invalid or an authentication is already set, clear the cookie.
                System.out.println("Invalid JWT token or authentication context not empty");
                clearJwtCookie(response);
            }
        } else {
            System.out.println("No JWT token found in cookies");
        }

        // 4. Proceed with the rest of the filter chain.
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the incoming request cookies.
     * @param request the HTTP request
     * @return the JWT token if present, otherwise null
     */
    private String getJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Clears the JWT cookie by setting its value to null and maxAge to 0.
     * @param response the HTTP response
     */
    private void clearJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Only transmit over HTTPS
        cookie.setMaxAge(0);
        cookie.setPath("/");

        // Add the cookie manually to include SameSite attribute
        response.addHeader("Set-Cookie",
                String.format("jwt=null; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=Strict"));
    }
}
