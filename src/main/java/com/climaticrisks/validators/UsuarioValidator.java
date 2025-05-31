package com.climaticrisks.validators;

import com.climaticrisks.models.Endereco;
import com.climaticrisks.models.Usuario;
import com.climaticrisks.repositories.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@ApplicationScoped
public class UsuarioValidator {

    @Inject
    UsuarioRepository usuarioRepository;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    private static final Pattern TELEFONE_PATTERN =
            Pattern.compile("^\\(?\\d{2}\\)?[\\s-]?\\d{4,5}[\\s-]?\\d{4}$");

    public ValidationResult validate(Usuario usuario) {
        List<String> errors = new ArrayList<>();

        validateNome(usuario.getNome(), errors);

        validateEmail(usuario.getEmail(), errors);

        validateTelefone(usuario.getTelefone(), errors);

        validateSenha(usuario.getSenha(), errors);

        if (usuario.getEndereco() != null) {
            validateEndereco(usuario.getEndereco(), errors);
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    public ValidationResult validateForUpdate(Usuario usuario, Integer id) {
        List<String> errors = new ArrayList<>();

        validateNome(usuario.getNome(), errors);
        validateTelefone(usuario.getTelefone(), errors);

        if (usuario.getEmail() != null && !usuario.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(usuario.getEmail()).matches()) {
                errors.add("Email deve ter um formato válido");
            } else {
                Optional<Usuario> existingUser = usuarioRepository.findByEmail(usuario.getEmail());
                if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                    errors.add("Email já está sendo usado por outro usuário");
                }
            }
        }

        if (usuario.getEndereco() != null) {
            validateEndereco(usuario.getEndereco(), errors);
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    public ValidationResult validateEmailUnique(String email) {
        List<String> errors = new ArrayList<>();

        if (email != null && !email.trim().isEmpty()) {
            Optional<Usuario> existingUser = usuarioRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                errors.add("Email já está cadastrado");
            }
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    private void validateNome(String nome, List<String> errors) {
        if (nome == null || nome.trim().isEmpty()) {
            errors.add("Nome é obrigatório");
        } else if (nome.trim().length() < 2) {
            errors.add("Nome deve ter pelo menos 2 caracteres");
        } else if (nome.trim().length() > 100) {
            errors.add("Nome deve ter no máximo 100 caracteres");
        } else if (!nome.matches("^[a-zA-ZÀ-ÿ\\s]+$")) {
            errors.add("Nome deve conter apenas letras e espaços");
        }
    }

    private void validateEmail(String email, List<String> errors) {
        if (email == null || email.trim().isEmpty()) {
            errors.add("Email é obrigatório");
        } else if (email.length() > 100) {
            errors.add("Email deve ter no máximo 100 caracteres");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("Email deve ter um formato válido");
        } else {
            Optional<Usuario> existingUser = usuarioRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                errors.add("Email já está cadastrado");
            }
        }
    }

    private void validateTelefone(String telefone, List<String> errors) {
        if (telefone != null && !telefone.trim().isEmpty()) {
            if (telefone.length() > 20) {
                errors.add("Telefone deve ter no máximo 20 caracteres");
            } else if (!TELEFONE_PATTERN.matcher(telefone.replaceAll("\\s", "")).matches()) {
                errors.add("Telefone deve ter um formato válido (ex: (11) 99999-9999)");
            }
        }
    }

    private void validateSenha(String senha, List<String> errors) {
        if (senha == null || senha.trim().isEmpty()) {
            errors.add("Senha é obrigatória");
        } else if (senha.length() < 6) {
            errors.add("Senha deve ter pelo menos 6 caracteres");
        } else if (senha.length() > 255) {
            errors.add("Senha deve ter no máximo 255 caracteres");
        } else if (!senha.matches(".*[A-Za-z].*")) {
            errors.add("Senha deve conter pelo menos uma letra");
        } else if (!senha.matches(".*\\d.*")) {
            errors.add("Senha deve conter pelo menos um número");
        }
    }

    private void validateEndereco(Endereco endereco, List<String> errors) {
        if (endereco.getLogradouro() != null && endereco.getLogradouro().length() > 255) {
            errors.add("Logradouro deve ter no máximo 255 caracteres");
        }

        if (endereco.getBairro() != null && endereco.getBairro().length() > 100) {
            errors.add("Bairro deve ter no máximo 100 caracteres");
        }

        if (endereco.getCep() != null && !endereco.getCep().trim().isEmpty()) {
            String cep = endereco.getCep().replaceAll("[^0-9]", "");
            if (cep.length() != 8) {
                errors.add("CEP deve ter 8 dígitos");
            }
        }
    }

    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public String getErrorsAsString() {
            return String.join("; ", errors);
        }
    }
}
