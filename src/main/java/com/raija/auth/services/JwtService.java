package com.raija.auth.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;


@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET;

    private final long ACCESS_EXP = 15 * 60 * 1000;
    private final long REFRESH_EXP = 7 * 24 * 60 * 60 * 1000;

    public String generateAccessToken(String userId, String sessionId) {
        return Jwts.builder()
                .subject(userId)
                .claim("sid", sessionId)
                .expiration(new Date(System.currentTimeMillis() + ACCESS_EXP))
                .signWith(getKey(), Jwts.SIG.HS384)
                .compact();
    }

    public String generateRefreshToken(String userId, String sessionId) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("sid", sessionId)
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXP))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), Jwts.SIG.HS384)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }
}
