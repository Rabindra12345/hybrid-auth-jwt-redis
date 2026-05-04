package com.raija.auth.controller;

import com.raija.auth.dtos.AuthResponse;
import com.raija.auth.dtos.LoginRequest;
import com.raija.auth.dtos.SessionData;
import com.raija.auth.dtos.SignUpRequest;
import com.raija.auth.repos.UserRepository;
import com.raija.auth.services.AuthService;
import com.raija.auth.services.JwtService;
import com.raija.auth.services.SessionService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("auth")
public class LoginController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {

        // validate user (DB check)
        if (!userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("User not found");
        }
        String userId = req.getUsername();
        String sessionId = UUID.randomUUID().toString();
        sessionService.createSession(sessionId,
                new SessionData(userId, UUID.randomUUID().toString(), System.currentTimeMillis(),0,null,null,null));
        String accessToken = jwtService.generateAccessToken(userId, sessionId);
        String refreshToken = jwtService.generateRefreshToken(userId, sessionId);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(refreshToken))
                .body(Map.of("accessToken", accessToken));
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue("refreshToken") String refreshToken) {
        Claims claims = jwtService.parse(refreshToken);
        String userId = claims.getSubject();
        String sessionId = claims.get("sid", String.class);
        SessionData session = sessionService.getSession(sessionId);
        if (session == null) {
            throw new RuntimeException("Session expired");
        }
        // rotating refresh token
        String newRefresh = jwtService.generateRefreshToken(userId, sessionId);
        String newAccess = jwtService.generateAccessToken(userId, sessionId);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(newRefresh))
                .body(Map.of("accessToken", newAccess));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignUpRequest req,
                                    HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        AuthResponse res = authService.signup(req, ip, userAgent);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(res.getRefreshToken()))
                .body(Map.of("accessToken", res.getAccessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = request.getHeader("X-Api-Authorization").substring(7);
        Claims claims = jwtService.parse(token);
        String sessionId = claims.get("sid", String.class);
        authService.logout(sessionId);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }


    @GetMapping("/temp")
    public ResponseEntity<?> getTemp(@RequestHeader("X-Api-Authorization") String apiHeader){
        return ResponseEntity.ok("THis is temp!!");
    }

    private String buildRefreshCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build()
                .toString();
    }
}
