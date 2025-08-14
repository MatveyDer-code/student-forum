package io.student.pet.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.student.pet.exception.InvalidJwtTokenException;
import io.student.pet.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Slf4j
@Service
public class JwtProvider {

    private final Key key;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.access-expiration}") long accessTokenExpirationMs,
                       @Value("${jwt.refresh-expiration}") long refreshTokenExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);
        log.info("Генерация access токена для пользователя {}", user.getUsername());

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        try {
            boolean valid = validateToken(token, accessTokenExpirationMs);
            log.debug("Access токен валиден: {}", valid);
            return valid;
        } catch (InvalidJwtTokenException ex) {
            log.warn("Ошибка валидации access токена: {}", ex.getMessage());
            throw ex;
        }    }

    public boolean validateRefreshToken(String token) {
        try {
            boolean valid = validateToken(token, refreshTokenExpirationMs);
            log.debug("Refresh токен валиден: {}", valid);
            return valid;
        } catch (InvalidJwtTokenException ex) {
            log.warn("Ошибка валидации refresh токена: {}", ex.getMessage());
            throw ex;
        }
    }

    private boolean validateToken(String token, long expirationMs) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidJwtTokenException("JWT token is invalid or expired", ex);
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
}