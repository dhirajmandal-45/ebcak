package ecommerce.back.controller;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ecommerce.back.repository.UserRepository;
import ecommerce.back.entity.User;
import ecommerce.back.security.JwtService;
import ecommerce.back.dto.LoginRequest;
import ecommerce.back.dto.SignupRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/freshers")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // ============================================================
    // SIGNUP
    // ============================================================

    @PostMapping("/signup")
    public String signup(
            @Valid @RequestBody SignupRequest request) {

        // Check whether email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email already exists";
        }

        // Create a new User object
        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // Encrypt password before saving it
        user.setPassword(
                passwordEncoder.encode(request.getPassword()));

        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setPincode(request.getPincode());

        // IMPORTANT:
        // Normal users are created with USER role.
        //
        // A user cannot choose ADMIN from the frontend.
        user.setRole("USER");

        // Save user in database
        userRepository.save(user);

        return "Signup successfully";
    }

    // ============================================================
    // LOGIN
    // ============================================================

    @PostMapping("/login")
    public String login(
            @RequestBody LoginRequest login) {

        // Find user by email
        Optional<User> user = userRepository.findByEmail(login.getEmail());

        // User does not exist
        if (user.isEmpty()) {
            return "User not found";
        }

        // Check entered password against
        // the encrypted password in database
        boolean isCorrect = passwordEncoder.matches(
                login.getPassword(),
                user.get().getPassword());

        // Password is incorrect
        if (!isCorrect) {
            return "Wrong password";
        }

        // ========================================================
        // GENERATE JWT
        // ========================================================
        // We now pass the complete User object.
        //
        // JwtService will put these values into the JWT:
        //
        // email → user.getEmail()
        // role → user.getRole()
        //
        // Example:
        //
        // email = admin@gmail.com
        // role = ADMIN
        // ========================================================

        // Generate JWT using user's email
        // String token = jwtService.generateToken(user.get());

        // Return token + role to frontend
        String token = jwtService.generateToken(user.get());

        return token;
    }
}