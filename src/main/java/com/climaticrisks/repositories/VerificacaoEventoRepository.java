package com.climaticrisks.repositories;

import com.climaticrisks.models.VerificacaoEvento;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class VerificacaoEventoRepository {

    @Inject
    DataSource dataSource;

    public VerificacaoEvento save(VerificacaoEvento verificacao) {
        String sql = """
            INSERT INTO gs_verificacao_evento (usuario_id, alagamento_id, deslizamento_id, confirmacao, created_at, updated_at)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, verificacao.getUsuarioId());

            if (verificacao.getAlagamentoId() != null) {
                stmt.setInt(2, verificacao.getAlagamentoId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }

            if (verificacao.getDeslizamentoId() != null) {
                stmt.setInt(3, verificacao.getDeslizamentoId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.setInt(4, verificacao.getConfirmacao() != null && verificacao.getConfirmacao() ? 1 : 0);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Buscar o ID do registro inserido
                String selectSql = """
                    SELECT id FROM gs_verificacao_evento 
                    WHERE usuario_id = ? AND created_at = (SELECT MAX(created_at) FROM gs_verificacao_evento WHERE usuario_id = ?)
                    """;

                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setInt(1, verificacao.getUsuarioId());
                    selectStmt.setInt(2, verificacao.getUsuarioId());

                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            verificacao.setId(rs.getInt("id"));
                        }
                    }
                }
            }

            return verificacao;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar verificação: " + e.getMessage(), e);
        }
    }

    public Optional<VerificacaoEvento> findById(Integer id) {
        String sql = """
            SELECT id, usuario_id, alagamento_id, deslizamento_id, confirmacao,
                   created_at, updated_at, deleted_at
            FROM gs_verificacao_evento 
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVerificacao(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar verificação por ID", e);
        }
    }

    public List<VerificacaoEvento> findByUsuarioId(Integer usuarioId) {
        String sql = """
            SELECT id, usuario_id, alagamento_id, deslizamento_id, confirmacao,
                   created_at, updated_at, deleted_at
            FROM gs_verificacao_evento 
            WHERE usuario_id = ? AND deleted_at IS NULL
            ORDER BY created_at DESC
            """;

        List<VerificacaoEvento> verificacoes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    verificacoes.add(mapResultSetToVerificacao(rs));
                }
            }

            return verificacoes;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar verificações por usuário", e);
        }
    }

    public List<VerificacaoEvento> findByAlagamentoId(Integer alagamentoId) {
        String sql = """
            SELECT id, usuario_id, alagamento_id, deslizamento_id, confirmacao,
                   created_at, updated_at, deleted_at
            FROM gs_verificacao_evento 
            WHERE alagamento_id = ? AND deleted_at IS NULL
            ORDER BY created_at DESC
            """;

        List<VerificacaoEvento> verificacoes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, alagamentoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    verificacoes.add(mapResultSetToVerificacao(rs));
                }
            }

            return verificacoes;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar verificações por alagamento", e);
        }
    }

    public List<VerificacaoEvento> findByDeslizamentoId(Integer deslizamentoId) {
        String sql = """
            SELECT id, usuario_id, alagamento_id, deslizamento_id, confirmacao,
                   created_at, updated_at, deleted_at
            FROM gs_verificacao_evento 
            WHERE deslizamento_id = ? AND deleted_at IS NULL
            ORDER BY created_at DESC
            """;

        List<VerificacaoEvento> verificacoes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deslizamentoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    verificacoes.add(mapResultSetToVerificacao(rs));
                }
            }

            return verificacoes;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar verificações por deslizamento", e);
        }
    }

    public boolean jaVerificouEvento(Integer usuarioId, Integer alagamentoId, Integer deslizamentoId) {
        String sql = """
            SELECT COUNT(*) as total
            FROM gs_verificacao_evento 
            WHERE usuario_id = ? 
            AND ((alagamento_id = ? AND ? IS NOT NULL) OR (deslizamento_id = ? AND ? IS NOT NULL))
            AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);

            if (alagamentoId != null) {
                stmt.setInt(2, alagamentoId);
                stmt.setInt(3, alagamentoId);
            } else {
                stmt.setNull(2, Types.INTEGER);
                stmt.setNull(3, Types.INTEGER);
            }

            if (deslizamentoId != null) {
                stmt.setInt(4, deslizamentoId);
                stmt.setInt(5, deslizamentoId);
            } else {
                stmt.setNull(4, Types.INTEGER);
                stmt.setNull(5, Types.INTEGER);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }

            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar se usuário já verificou evento", e);
        }
    }

    public EstatisticasVerificacao getEstatisticasAlagamento(Integer alagamentoId) {
        String sql = """
            SELECT 
                COUNT(*) as total_verificacoes,
                SUM(CASE WHEN confirmacao = 1 THEN 1 ELSE 0 END) as confirmacoes,
                SUM(CASE WHEN confirmacao = 0 THEN 1 ELSE 0 END) as negacoes
            FROM gs_verificacao_evento 
            WHERE alagamento_id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, alagamentoId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new EstatisticasVerificacao(
                            rs.getInt("total_verificacoes"),
                            rs.getInt("confirmacoes"),
                            rs.getInt("negacoes")
                    );
                }
            }

            return new EstatisticasVerificacao(0, 0, 0);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar estatísticas de alagamento", e);
        }
    }

    public EstatisticasVerificacao getEstatisticasDeslizamento(Integer deslizamentoId) {
        String sql = """
            SELECT 
                COUNT(*) as total_verificacoes,
                SUM(CASE WHEN confirmacao = 1 THEN 1 ELSE 0 END) as confirmacoes,
                SUM(CASE WHEN confirmacao = 0 THEN 1 ELSE 0 END) as negacoes
            FROM gs_verificacao_evento 
            WHERE deslizamento_id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deslizamentoId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new EstatisticasVerificacao(
                            rs.getInt("total_verificacoes"),
                            rs.getInt("confirmacoes"),
                            rs.getInt("negacoes")
                    );
                }
            }

            return new EstatisticasVerificacao(0, 0, 0);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar estatísticas de deslizamento", e);
        }
    }

    public List<VerificacaoEvento> findAll() {
        String sql = """
            SELECT id, usuario_id, alagamento_id, deslizamento_id, confirmacao,
                   created_at, updated_at, deleted_at
            FROM gs_verificacao_evento 
            WHERE deleted_at IS NULL
            ORDER BY created_at DESC
            """;

        List<VerificacaoEvento> verificacoes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                verificacoes.add(mapResultSetToVerificacao(rs));
            }

            return verificacoes;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar todas as verificações", e);
        }
    }

    public VerificacaoEvento update(VerificacaoEvento verificacao) {
        String sql = """
            UPDATE gs_verificacao_evento 
            SET confirmacao = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, verificacao.getConfirmacao() != null && verificacao.getConfirmacao() ? 1 : 0);
            stmt.setInt(2, verificacao.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Verificação não encontrada para atualização");
            }

            return verificacao;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar verificação", e);
        }
    }

    public void delete(Integer id) {
        String sql = """
            UPDATE gs_verificacao_evento 
            SET deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Verificação não encontrada para exclusão");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir verificação", e);
        }
    }

    private VerificacaoEvento mapResultSetToVerificacao(ResultSet rs) throws SQLException {
        VerificacaoEvento verificacao = new VerificacaoEvento();
        verificacao.setId(rs.getInt("id"));
        verificacao.setUsuarioId(rs.getInt("usuario_id"));

        int alagamentoId = rs.getInt("alagamento_id");
        if (!rs.wasNull()) {
            verificacao.setAlagamentoId(alagamentoId);
        }

        int deslizamentoId = rs.getInt("deslizamento_id");
        if (!rs.wasNull()) {
            verificacao.setDeslizamentoId(deslizamentoId);
        }

        verificacao.setConfirmacao(rs.getInt("confirmacao") == 1);

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            verificacao.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            verificacao.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            verificacao.setDeletedAt(deletedAt.toLocalDateTime());
        }

        return verificacao;
    }

    public static class EstatisticasVerificacao {
        private int totalVerificacoes;
        private int confirmacoes;
        private int negacoes;
        private double percentualConfirmacao;

        public EstatisticasVerificacao(int totalVerificacoes, int confirmacoes, int negacoes) {
            this.totalVerificacoes = totalVerificacoes;
            this.confirmacoes = confirmacoes;
            this.negacoes = negacoes;
            this.percentualConfirmacao = totalVerificacoes > 0 ?
                    (double) confirmacoes / totalVerificacoes * 100 : 0;
        }

        public int getTotalVerificacoes() { return totalVerificacoes; }
        public int getConfirmacoes() { return confirmacoes; }
        public int getNegacoes() { return negacoes; }
        public double getPercentualConfirmacao() { return percentualConfirmacao; }
    }
}
