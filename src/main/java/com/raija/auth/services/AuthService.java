package com.raija.auth.services;

import com.raija.auth.dtos.AuthResponse;
import com.raija.auth.dtos.SessionData;
import com.raija.auth.dtos.SignUpRequest;
import com.raija.auth.entity.User;
import com.raija.auth.repos.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SessionService sessionService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       SessionService sessionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.sessionService = sessionService;
    }

    public AuthResponse signup(SignUpRequest req, String ip, String userAgent) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Email already in use");
        }
        String hashedPassword = passwordEncoder.encode(req.getPassword());
        User user = new User();
        user.setUsername(req.getUsername());
//        user.setEmail(req.getEmail());
        user.setPassword(hashedPassword);
        user.setRole("ROLE_USER");
        user.setCreatedAt(System.currentTimeMillis());

        userRepository.save(user);
        // Setting  auto login for now once user is created
        // ** need to remove in future **
        String sessionId = UUID.randomUUID().toString();
        SessionData session = new SessionData(
                user.getId(),
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                ip,
                userAgent,
                req.getDeviceId()
        );
        sessionService.createSession(sessionId, session);
//        String accessToken = jwtService.generateAccessToken(user.getId(), sessionId);
//        String refreshToken = jwtService.generateRefreshToken(user.getId(), sessionId);
        return new AuthResponse();
    }

    // AuthService
    public void logout(String sessionId) {
        sessionService.deleteSession(sessionId);
    }
}
