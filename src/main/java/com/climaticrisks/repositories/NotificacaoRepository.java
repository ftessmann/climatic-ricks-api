package com.climaticrisks.repositories;

import com.climaticrisks.models.Notificacao;
import com.climaticrisks.enums.TipoRisco;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class NotificacaoRepository {

    @Inject
    DataSource dataSource;

    public Notificacao save(Notificacao notificacao) {
        String sql = """
            INSERT INTO gs_notificacao (usuario_id, titulo, mensagem, prioridade, lida, created_at, updated_at)
            VALUES (?, ?, ?, ?, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, notificacao.getUsuarioId());
            stmt.setString(2, notificacao.getTitulo());

            if (notificacao.getMensagem() != null && !notificacao.getMensagem().trim().isEmpty()) {
                stmt.setString(3, notificacao.getMensagem());
            } else {
                stmt.setNull(3, Types.CLOB);
            }

            stmt.setString(4, notificacao.getPrioridade() != null ? notificacao.getPrioridade().getValor() : "baixo");

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                String selectSql = """
                    SELECT id FROM gs_notificacao 
                    WHERE usuario_id = ? AND titulo = ? AND created_at = (SELECT MAX(created_at) FROM gs_notificacao WHERE usuario_id = ?)
                    """;

                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setInt(1, notificacao.getUsuarioId());
                    selectStmt.setString(2, notificacao.getTitulo());
                    selectStmt.setInt(3, notificacao.getUsuarioId());

                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            notificacao.setId(rs.getInt("id"));
                        }
                    }
                }
            }

            notificacao.setLida(false);
            return notificacao;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar notificação: " + e.getMessage(), e);
        }
    }

    public List<Notificacao> findByUsuarioId(Integer usuarioId) {
        String sql = """
            SELECT id, usuario_id, titulo, mensagem, prioridade, lida,
                   created_at, updated_at, deleted_at
            FROM gs_notificacao 
            WHERE usuario_id = ? AND deleted_at IS NULL
            ORDER BY created_at DESC
            """;

        List<Notificacao> notificacoes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notificacoes.add(mapResultSetToNotificacao(rs));
                }
            }

            return notificacoes;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar notificações por usuário", e);
        }
    }

    public List<Notificacao> findNaoLidasByUsuarioId(Integer usuarioId) {
        String sql = """
            SELECT id, usuario_id, titulo, mensagem, prioridade, lida,
                   created_at, updated_at, deleted_at
            FROM gs_notificacao 
            WHERE usuario_id = ? AND lida = 0 AND deleted_at IS NULL
            ORDER BY created_at DESC
            """;

        List<Notificacao> notificacoes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notificacoes.add(mapResultSetToNotificacao(rs));
                }
            }

            return notificacoes;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar notificações não lidas", e);
        }
    }

    public void marcarComoLida(Integer id) {
        String sql = """
            UPDATE gs_notificacao 
            SET lida = 1, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Notificação não encontrada para marcar como lida");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao marcar notificação como lida", e);
        }
    }

    public void marcarTodasComoLidas(Integer usuarioId) {
        String sql = """
            UPDATE gs_notificacao 
            SET lida = 1, updated_at = CURRENT_TIMESTAMP
            WHERE usuario_id = ? AND lida = 0 AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao marcar todas as notificações como lidas", e);
        }
    }

    public List<Integer> findUsuariosByBairro(String bairro) {
        String sql = """
            SELECT DISTINCT u.id 
            FROM gs_usuario u 
            INNER JOIN gs_endereco e ON u.endereco_id = e.id 
            WHERE UPPER(e.bairro) = UPPER(?) AND u.deleted_at IS NULL AND e.deleted_at IS NULL
            """;

        List<Integer> usuarioIds = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bairro.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarioIds.add(rs.getInt("id"));
                }
            }

            return usuarioIds;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuários por bairro", e);
        }
    }

    public void notificarUsuariosPorBairro(String bairro, String titulo, String mensagem, TipoRisco prioridade) {
        List<Integer> usuarioIds = findUsuariosByBairro(bairro);

        for (Integer usuarioId : usuarioIds) {
            Notificacao notificacao = new Notificacao();
            notificacao.setUsuarioId(usuarioId);
            notificacao.setTitulo(titulo);
            notificacao.setMensagem(mensagem);
            notificacao.setPrioridade(prioridade);

            save(notificacao);
        }
    }

    public Optional<Notificacao> findById(Integer id) {
        String sql = """
            SELECT id, usuario_id, titulo, mensagem, prioridade, lida,
                   created_at, updated_at, deleted_at
            FROM gs_notificacao 
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToNotificacao(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar notificação por ID", e);
        }
    }

    private Notificacao mapResultSetToNotificacao(ResultSet rs) throws SQLException {
        Notificacao notificacao = new Notificacao();
        notificacao.setId(rs.getInt("id"));
        notificacao.setUsuarioId(rs.getInt("usuario_id"));
        notificacao.setTitulo(rs.getString("titulo"));
        notificacao.setMensagem(rs.getString("mensagem"));
        notificacao.setLida(rs.getInt("lida") == 1);

        String prioridade = rs.getString("prioridade");
        if (prioridade != null) {
            switch (prioridade.toLowerCase()) {
                case "baixo" -> notificacao.setPrioridade(TipoRisco.BAIXO);
                case "medio", "médio" -> notificacao.setPrioridade(TipoRisco.MEDIO);
                case "alto" -> notificacao.setPrioridade(TipoRisco.ALTO);
            }
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            notificacao.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            notificacao.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            notificacao.setDeletedAt(deletedAt.toLocalDateTime());
        }

        return notificacao;
    }
}
