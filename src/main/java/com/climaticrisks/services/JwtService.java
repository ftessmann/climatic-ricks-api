package com.climaticrisks.services;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.time.Duration;
import java.time.Instant;

@ApplicationScoped
public class JwtService {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    public String generateToken(String userId, String email, String nome) {
        try {
            String token = Jwt.issuer(issuer)
                    .upn(email)
                    .subject(userId)
                    .claim("userId", userId)
                    .claim("email", email)
                    .claim("nome", nome)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plus(Duration.ofHours(24)))
                    .sign();

            return token;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar token JWT", e);
        }
    }

    public String generateRefreshToken(String userId) {
        try {
            String refreshToken = Jwt.issuer(issuer)
                    .subject(userId)
                    .claim("type", "refresh")
                    .claim("userId", userId)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plus(Duration.ofDays(7)))
                    .sign();

            return refreshToken;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar refresh token", e);
        }
    }
}
