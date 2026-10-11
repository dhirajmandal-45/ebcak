package ecommerce.back.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

        private final JwtAuthFilter jwtAuthFilter;

        public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
                this.jwtAuthFilter = jwtAuthFilter;
        }

        // ============================================================
        // PASSWORD ENCODER
        // ============================================================

        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        // ============================================================
        // SECURITY FILTER CHAIN
        // ============================================================

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http) throws Exception {

                http

                                // Disable CSRF because we use JWT
                                .csrf(csrf -> csrf.disable())

                                // Enable CORS
                                .cors(cors -> cors
                                                .configurationSource(corsConfigurationSource()))

                                // JWT = stateless
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(
                                                                SessionCreationPolicy.STATELESS))

                                // ====================================================
                                // AUTHORIZATION
                                // ====================================================

                                .authorizeHttpRequests(auth -> auth

                                                // -------------------------------
                                                // PUBLIC APIs
                                                // -------------------------------

                                                .requestMatchers(
                                                                "/freshers/signup",
                                                                "/freshers/login")
                                                .permitAll()

                                                .requestMatchers(
                                                                "/products/**",
                                                                "/categories/**")
                                                .permitAll()

                                                // -------------------------------
                                                // ADMIN ONLY
                                                // -------------------------------

                                                .requestMatchers("/admin/**")
                                                .hasRole("ADMIN")

                                                // -------------------------------
                                                // GROWTH ADMIN ONLY
                                                // -------------------------------

                                                .requestMatchers("/growth/**")
                                                .hasRole("ADMIN")

                                                // -------------------------------
                                                // EVERYTHING ELSE
                                                // -------------------------------

                                                .anyRequest()
                                                .authenticated())

                                // Disable default login
                                .formLogin(form -> form.disable())

                                // Disable HTTP Basic
                                .httpBasic(basic -> basic.disable())

                                // JWT filter
                                .addFilterBefore(
                                                jwtAuthFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        // ============================================================
        // CORS CONFIGURATION
        // ============================================================

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {

                CorsConfiguration configuration = new CorsConfiguration();

                // React frontend
                configuration.setAllowedOrigins(
                                List.of("http://localhost:5173", "https://ecommerce-frontend-beta-ecru.vercel.app/"));

                // Allowed HTTP methods
                configuration.setAllowedMethods(
                                List.of(
                                                "GET",
                                                "POST",
                                                "PUT",
                                                "DELETE",
                                                "OPTIONS"));

                // Allow Authorization header
                configuration.setAllowedHeaders(
                                List.of("*"));

                // Allow credentials
                configuration.setAllowCredentials(true);

                // Apply CORS to all APIs
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                source.registerCorsConfiguration(
                                "/**",
                                configuration);

                return source;
        }
}