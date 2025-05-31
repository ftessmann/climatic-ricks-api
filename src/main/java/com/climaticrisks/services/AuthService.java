package com.climaticrisks.services;

import com.climaticrisks.models.Usuario;
import com.climaticrisks.repositories.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class AuthService {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    JwtService jwtService;

    public AuthResult authenticate(String email, String senha) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return new AuthResult(false, "Credenciais inválidas", null, null);
        }

        Usuario usuario = usuarioOpt.get();

        if (!verificarSenha(senha, usuario.getSenha())) {
            return new AuthResult(false, "Credenciais inválidas", null, null);
        }

        Set<String> roles = usuario.getDefesaCivil() ?
                Set.of("USER", "DEFESA_CIVIL") : Set.of("USER");

        String token = jwtService.generateToken(
                usuario.getId().toString(),
                usuario.getEmail(),
                usuario.getNome(),
                roles
        );

        String refreshToken = jwtService.generateRefreshToken(usuario.getId().toString());

        return new AuthResult(true, "Login realizado com sucesso", token, refreshToken);
    }

    private boolean verificarSenha(String senhaPlana, String senhaHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedPassword = md.digest(senhaPlana.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().equals(senhaHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao verificar senha", e);
        }
    }

    public String hashSenha(String senhaPlana) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedPassword = md.digest(senhaPlana.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash da senha", e);
        }
    }

    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final String token;
        private final String refreshToken;

        public AuthResult(boolean success, String message, String token, String refreshToken) {
            this.success = success;
            this.message = message;
            this.token = token;
            this.refreshToken = refreshToken;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getToken() { return token; }
        public String getRefreshToken() { return refreshToken; }
    }
}
