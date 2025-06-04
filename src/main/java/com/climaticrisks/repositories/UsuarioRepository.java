package com.climaticrisks.repositories;

import com.climaticrisks.models.Usuario;
import com.climaticrisks.models.Endereco;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UsuarioRepository {

    @Inject
    DataSource dataSource;

    @Inject
    EnderecoRepository enderecoRepository;

    public Usuario save(Usuario usuario) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            if (usuario.getEndereco() != null) {
                Endereco savedEndereco = enderecoRepository.save(usuario.getEndereco());
                usuario.setEndereco(savedEndereco);
            }

            String sql = """
            INSERT INTO gs_usuario (nome, email, telefone, endereco_id, senha, is_defesa_civil)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID"})) {
                stmt.setString(1, usuario.getNome());
                stmt.setString(2, usuario.getEmail());
                stmt.setString(3, usuario.getTelefone());

                if (usuario.getEndereco() != null && usuario.getEndereco().getId() != null) {
                    stmt.setInt(4, usuario.getEndereco().getId());
                } else {
                    stmt.setNull(4, Types.INTEGER);
                }

                stmt.setString(5, usuario.getSenha());
                stmt.setInt(6, usuario.getDefesaCivil() != null && usuario.getDefesaCivil() ? 1 : 0);

                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        try {
                            usuario.setId(rs.getInt(1));
                        } catch (SQLException e) {
                            String idStr = rs.getString(1);
                            if (idStr != null && idStr.matches("\\d+")) {
                                usuario.setId(Integer.parseInt(idStr));
                            } else {
                                System.err.println("ID retornado não é numérico: '" + idStr + "', buscando último ID inserido");
                                usuario.setId(getLastInsertedId(conn, "gs_usuario"));
                            }
                        }
                    }
                }
            }

            conn.commit();
            return usuario;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    throw new RuntimeException("Erro ao fazer rollback", rollbackEx);
                }
            }
            throw new RuntimeException("Erro ao salvar usuário: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException("Erro ao fechar conexão", e);
                }
            }
        }
    }

    private Integer getLastInsertedId(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT MAX(id) FROM " + tableName;
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return null;
    }

    public Optional<Usuario> findById(Integer id) {
        String sql = """
            SELECT u.id, u.nome, u.email, u.telefone, u.endereco_id, u.senha, u.is_defesa_civil,
                   u.created_at, u.updated_at, u.deleted_at
            FROM gs_usuario u
            WHERE u.id = ? AND u.deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapResultSetToUsuario(rs);

                    Integer enderecoId = rs.getInt("endereco_id");
                    if (enderecoId != null && !rs.wasNull()) {
                        Optional<Endereco> endereco = enderecoRepository.findById(enderecoId);
                        endereco.ifPresent(usuario::setEndereco);
                    }

                    return Optional.of(usuario);
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário por ID", e);
        }
    }

    public List<Usuario> findAll() {
        String sql = """
            SELECT u.id, u.nome, u.email, u.telefone, u.endereco_id, u.senha, u.is_defesa_civil,
                   u.created_at, u.updated_at, u.deleted_at
            FROM gs_usuario u
            WHERE u.deleted_at IS NULL
            ORDER BY u.id
            """;

        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Usuario usuario = mapResultSetToUsuario(rs);

                Integer enderecoId = rs.getInt("endereco_id");
                if (enderecoId != null && !rs.wasNull()) {
                    Optional<Endereco> endereco = enderecoRepository.findById(enderecoId);
                    endereco.ifPresent(usuario::setEndereco);
                }

                usuarios.add(usuario);
            }

            return usuarios;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar todos os usuários", e);
        }
    }

    public Usuario update(Usuario usuario) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            if (usuario.getEndereco() != null) {
                if (usuario.getEndereco().getId() != null) {
                    enderecoRepository.update(usuario.getEndereco());
                } else {
                    Endereco savedEndereco = enderecoRepository.save(usuario.getEndereco());
                    usuario.setEndereco(savedEndereco);
                }
            }

            String sql = """
                UPDATE gs_usuario 
                SET nome = ?, email = ?, telefone = ?, endereco_id = ?, senha = ?, 
                    is_defesa_civil = ?, updated_at = ?
                WHERE id = ? AND deleted_at IS NULL
                """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                usuario.setUpdatedAt(LocalDateTime.now());

                stmt.setString(1, usuario.getNome());
                stmt.setString(2, usuario.getEmail());
                stmt.setString(3, usuario.getTelefone());
                stmt.setInt(4, usuario.getEndereco() != null ? usuario.getEndereco().getId() : null);
                stmt.setString(5, usuario.getSenha());
                stmt.setInt(6, usuario.getDefesaCivil() != null && usuario.getDefesaCivil() ? 1 : 0);
                stmt.setTimestamp(7, Timestamp.valueOf(usuario.getUpdatedAt()));
                stmt.setInt(8, usuario.getId());

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("Usuário não encontrado para atualização");
                }
            }

            conn.commit();
            return usuario;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    throw new RuntimeException("Erro ao fazer rollback", rollbackEx);
                }
            }
            throw new RuntimeException("Erro ao atualizar usuário", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException("Erro ao fechar conexão", e);
                }
            }
        }
    }

    public void delete(Integer id) {
        String sql = """
            UPDATE gs_usuario 
            SET deleted_at = ?, updated_at = ?
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            LocalDateTime now = LocalDateTime.now();
            stmt.setTimestamp(1, Timestamp.valueOf(now));
            stmt.setTimestamp(2, Timestamp.valueOf(now));
            stmt.setInt(3, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Usuário não encontrado para exclusão");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir usuário", e);
        }
    }

    public Optional<Usuario> findByEmail(String email) {
        String sql = """
            SELECT u.id, u.nome, u.email, u.telefone, u.endereco_id, u.senha, u.is_defesa_civil,
                   u.created_at, u.updated_at, u.deleted_at
            FROM gs_usuario u
            WHERE u.email = ? AND u.deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapResultSetToUsuario(rs);

                    Integer enderecoId = rs.getInt("endereco_id");
                    if (enderecoId != null && !rs.wasNull()) {
                        Optional<Endereco> endereco = enderecoRepository.findById(enderecoId);
                        endereco.ifPresent(usuario::setEndereco);
                    }

                    return Optional.of(usuario);
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário por email", e);
        }
    }

    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setEmail(rs.getString("email"));
        usuario.setTelefone(rs.getString("telefone"));
        usuario.setSenha(rs.getString("senha"));
        usuario.setDefesaCivil(rs.getInt("is_defesa_civil") == 1);

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            usuario.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            usuario.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            usuario.setDeletedAt(deletedAt.toLocalDateTime());
        }

        return usuario;
    }
}
