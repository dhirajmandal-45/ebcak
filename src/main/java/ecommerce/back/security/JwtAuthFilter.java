package ecommerce.back.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    // ============================================================
    // SKIP LOGIN AND SIGNUP
    // ============================================================

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return path.equals("/freshers/signup")
                || path.equals("/freshers/login");
    }

    // ============================================================
    // JWT FILTER
    // ============================================================

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // ========================================================
        // NO TOKEN
        // ========================================================

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            System.out.println(
                    "JWT: No token -> "
                            + request.getServletPath());

            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

            // ====================================================
            // EXTRACT USER INFORMATION
            // ====================================================

            String email = jwtService.extractUsername(token);

            String role = jwtService.extractRole(token);

            // ====================================================
            // DEBUG INFORMATION
            // ====================================================

            System.out.println(
                    "==========================================");

            System.out.println(
                    "JWT REQUEST : "
                            + request.getServletPath());

            System.out.println(
                    "JWT EMAIL   : "
                            + email);

            System.out.println(
                    "JWT ROLE    : "
                            + role);

            // ====================================================
            // VALIDATE TOKEN
            // ====================================================

            boolean valid = email != null
                    && jwtService.isTokenValid(token);

            System.out.println(
                    "JWT VALID   : "
                            + valid);

            // ====================================================
            // CREATE AUTHENTICATION
            // ====================================================

            if (valid
                    && role != null
                    && !role.isBlank()) {

                // Remove ROLE_ if JWT already contains it
                String normalizedRole = role.toUpperCase();

                if (normalizedRole.startsWith("ROLE_")) {

                    normalizedRole = normalizedRole.substring(5);
                }

                // Spring Security expects:
                //
                // ADMIN -> ROLE_ADMIN
                // USER -> ROLE_USER

                String authorityName = "ROLE_" + normalizedRole;

                System.out.println(
                        "AUTHORITY   : "
                                + authorityName);

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
                        authorityName);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(authority));

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication);

                System.out.println(
                        "AUTHENTICATED: YES");

            } else {

                System.out.println(
                        "AUTHENTICATED: NO");
            }

            System.out.println(
                    "==========================================");

        } catch (Exception e) {

            System.out.println(
                    "JWT ERROR: "
                            + e.getMessage());

            SecurityContextHolder
                    .clearContext();
        }

        // ========================================================
        // CONTINUE REQUEST
        // ========================================================

        filterChain.doFilter(
                request,
                response);
    }
}