package ecommerce.back.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

```
private final JwtAuthFilter jwtAuthFilter;

public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
    this.jwtAuthFilter = jwtAuthFilter;
}

// PASSWORD ENCODER
@Bean
public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// SECURITY FILTER CHAIN
@Bean
public SecurityFilterChain securityFilterChain(
        HttpSecurity http) throws Exception {

    http
        .csrf(csrf -> csrf.disable())

        .cors(cors -> cors.configurationSource(
            corsConfigurationSource()
        ))

        .sessionManagement(session -> session
            .sessionCreationPolicy(
                SessionCreationPolicy.STATELESS
            )
        )

        .authorizeHttpRequests(auth -> auth

            // Allow CORS preflight requests
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

            // Public authentication APIs
            .requestMatchers(
                "/freshers/signup",
                "/freshers/login"
            ).permitAll()

            // Public product APIs
            .requestMatchers(
                "/products/**",
                "/categories/**"
            ).permitAll()

            // Admin APIs
            .requestMatchers("/admin/**")
                .hasRole("ADMIN")

            // Growth admin APIs
            .requestMatchers("/growth/**")
                .hasRole("ADMIN")

            // All remaining APIs require authentication
            .anyRequest().authenticated()
        )

        .formLogin(form -> form.disable())
        .httpBasic(basic -> basic.disable())

        .addFilterBefore(
            jwtAuthFilter,
            UsernamePasswordAuthenticationFilter.class
        );

    return http.build();
}

// CORS CONFIGURATION
@Bean
public CorsConfigurationSource corsConfigurationSource() {

    CorsConfiguration configuration =
        new CorsConfiguration();

    configuration.setAllowedOrigins(List.of(
        "http://localhost:5173",
        "https://ecommerce-frontend-beta-ecru.vercel.app",
        "https://ecommerce-frontend-i0o8we0b9-dk1397938-8910.vercel.app"
    ));

    configuration.setAllowedMethods(List.of(
        "GET",
        "POST",
        "PUT",
        "PATCH",
        "DELETE",
        "OPTIONS"
    ));

    configuration.setAllowedHeaders(List.of("*"));

    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source =
        new UrlBasedCorsConfigurationSource();

    source.registerCorsConfiguration(
        "/**",
        configuration
    );

    return source;
}
```

}
