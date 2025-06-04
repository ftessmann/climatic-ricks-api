package com.climaticrisks.repositories;

import com.climaticrisks.models.HistoricoRisco;
import com.climaticrisks.enums.TipoRisco;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class HistoricoRiscoRepository {

    @Inject
    DataSource dataSource;

    public HistoricoRisco save(HistoricoRisco historico) {
        String sql = """
            INSERT INTO gs_historico_risco (endereco_id, nivel_risco, total_eventos, created_at, updated_at)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, historico.getEnderecoId());
            stmt.setString(2, historico.getNivelRisco() != null ? historico.getNivelRisco().getValor() : "baixo");
            stmt.setInt(3, historico.getTotalEventos() != null ? historico.getTotalEventos() : 0);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                String selectSql = """
                    SELECT id FROM gs_historico_risco 
                    WHERE endereco_id = ? AND created_at = (SELECT MAX(created_at) FROM gs_historico_risco WHERE endereco_id = ?)
                    """;

                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setInt(1, historico.getEnderecoId());
                    selectStmt.setInt(2, historico.getEnderecoId());

                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            historico.setId(rs.getInt("id"));
                        }
                    }
                }
            }

            return historico;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar histórico de risco: " + e.getMessage(), e);
        }
    }

    public Optional<HistoricoRisco> findById(Integer id) {
        String sql = """
            SELECT id, endereco_id, nivel_risco, total_eventos,
                   created_at, updated_at, deleted_at
            FROM gs_historico_risco 
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToHistorico(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar histórico por ID", e);
        }
    }

    public Optional<HistoricoRisco> findByEnderecoId(Integer enderecoId) {
        String sql = """
            SELECT id, endereco_id, nivel_risco, total_eventos,
                   created_at, updated_at, deleted_at
            FROM gs_historico_risco 
            WHERE endereco_id = ? AND deleted_at IS NULL
            ORDER BY updated_at DESC
            FETCH FIRST 1 ROWS ONLY
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, enderecoId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToHistorico(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar histórico por endereço", e);
        }
    }

    public List<HistoricoRisco> findAll() {
        String sql = """
            SELECT id, endereco_id, nivel_risco, total_eventos,
                   created_at, updated_at, deleted_at
            FROM gs_historico_risco 
            WHERE deleted_at IS NULL
            ORDER BY total_eventos DESC, updated_at DESC
            """;

        List<HistoricoRisco> historicos = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                historicos.add(mapResultSetToHistorico(rs));
            }

            return historicos;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar todos os históricos", e);
        }
    }

    public List<HistoricoRisco> findByNivelRisco(TipoRisco nivelRisco) {
        String sql = """
            SELECT id, endereco_id, nivel_risco, total_eventos,
                   created_at, updated_at, deleted_at
            FROM gs_historico_risco 
            WHERE nivel_risco = ? AND deleted_at IS NULL
            ORDER BY total_eventos DESC
            """;

        List<HistoricoRisco> historicos = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nivelRisco.getValor());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    historicos.add(mapResultSetToHistorico(rs));
                }
            }

            return historicos;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar históricos por nível de risco", e);
        }
    }

    public HistoricoRisco update(HistoricoRisco historico) {
        String sql = """
            UPDATE gs_historico_risco 
            SET nivel_risco = ?, total_eventos = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, historico.getNivelRisco() != null ? historico.getNivelRisco().getValor() : "baixo");
            stmt.setInt(2, historico.getTotalEventos() != null ? historico.getTotalEventos() : 0);
            stmt.setInt(3, historico.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Histórico não encontrado para atualização");
            }

            return historico;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar histórico", e);
        }
    }

    public void delete(Integer id) {
        String sql = """
            UPDATE gs_historico_risco 
            SET deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Histórico não encontrado para exclusão");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir histórico", e);
        }
    }

    public void atualizarHistoricoRisco(Integer enderecoId) {
        try (Connection conn = dataSource.getConnection()) {
            String countSql = """
                SELECT 
                    (SELECT COUNT(*) FROM gs_alagamento WHERE endereco_id = ? AND deleted_at IS NULL) +
                    (SELECT COUNT(*) FROM gs_deslizamento WHERE endereco_id = ? AND deleted_at IS NULL) as total_eventos
                FROM DUAL
                """;

            int totalEventos = 0;
            try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
                countStmt.setInt(1, enderecoId);
                countStmt.setInt(2, enderecoId);

                try (ResultSet rs = countStmt.executeQuery()) {
                    if (rs.next()) {
                        totalEventos = rs.getInt("total_eventos");
                    }
                }
            }

            TipoRisco nivelRisco = calcularNivelRisco(totalEventos);

            Optional<HistoricoRisco> historicoExistente = findByEnderecoId(enderecoId);

            if (historicoExistente.isPresent()) {
                HistoricoRisco historico = historicoExistente.get();
                historico.setTotalEventos(totalEventos);
                historico.setNivelRisco(nivelRisco);
                update(historico);
            } else {
                HistoricoRisco novoHistorico = new HistoricoRisco();
                novoHistorico.setEnderecoId(enderecoId);
                novoHistorico.setTotalEventos(totalEventos);
                novoHistorico.setNivelRisco(nivelRisco);
                save(novoHistorico);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar histórico de risco: " + e.getMessage(), e);
        }
    }

    private TipoRisco calcularNivelRisco(int totalEventos) {
        if (totalEventos >= 5) {
            return TipoRisco.ALTO;
        } else if (totalEventos >= 2) {
            return TipoRisco.MEDIO;
        } else {
            return TipoRisco.BAIXO;
        }
    }

    public void recalcularTodosHistoricos() {
        String sql = """
            SELECT DISTINCT endereco_id 
            FROM (
                SELECT endereco_id FROM gs_alagamento WHERE deleted_at IS NULL
                UNION
                SELECT endereco_id FROM gs_deslizamento WHERE deleted_at IS NULL
            )
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Integer> enderecoIds = new ArrayList<>();
            while (rs.next()) {
                enderecoIds.add(rs.getInt("endereco_id"));
            }

            for (Integer enderecoId : enderecoIds) {
                atualizarHistoricoRisco(enderecoId);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao recalcular históricos", e);
        }
    }

    private HistoricoRisco mapResultSetToHistorico(ResultSet rs) throws SQLException {
        HistoricoRisco historico = new HistoricoRisco();
        historico.setId(rs.getInt("id"));
        historico.setEnderecoId(rs.getInt("endereco_id"));
        historico.setTotalEventos(rs.getInt("total_eventos"));

        String nivelRisco = rs.getString("nivel_risco");
        if (nivelRisco != null) {
            switch (nivelRisco.toLowerCase()) {
                case "baixo" -> historico.setNivelRisco(TipoRisco.BAIXO);
                case "medio", "médio" -> historico.setNivelRisco(TipoRisco.MEDIO);
                case "alto" -> historico.setNivelRisco(TipoRisco.ALTO);
            }
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            historico.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            historico.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            historico.setDeletedAt(deletedAt.toLocalDateTime());
        }

        return historico;
    }
}
