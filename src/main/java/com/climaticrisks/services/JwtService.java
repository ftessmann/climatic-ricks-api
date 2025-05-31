package com.climaticrisks.services;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@ApplicationScoped
public class JwtService {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    public String generateToken(String userId, String email, String nome, Set<String> roles) {
        return Jwt.issuer(issuer)
                .upn(email)
                .subject(userId)
                .claim("userId", userId)
                .claim("email", email)
                .claim("nome", nome)
                .groups(roles)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofHours(24)))
                .sign();
    }

    public String generateRefreshToken(String userId) {
        return Jwt.issuer(issuer)
                .subject(userId)
                .claim("type", "refresh")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofDays(7)))
                .sign();
    }
}
