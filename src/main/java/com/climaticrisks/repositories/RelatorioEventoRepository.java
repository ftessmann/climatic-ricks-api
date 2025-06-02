package com.climaticrisks.repositories;

import com.climaticrisks.models.RelatorioEvento;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RelatorioEventoRepository {

    @Inject
    DataSource dataSource;

    public RelatorioEvento save(RelatorioEvento relatorio) {
        String sql = """
            INSERT INTO gs_relatorio_evento (periodo, regiao, total_alagamentos, total_deslizamentos, total_diagnosticos, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, relatorio.getPeriodo());
            stmt.setString(2, relatorio.getRegiao());
            stmt.setInt(3, relatorio.getTotalAlagamentos() != null ? relatorio.getTotalAlagamentos() : 0);
            stmt.setInt(4, relatorio.getTotalDeslizamentos() != null ? relatorio.getTotalDeslizamentos() : 0);
            stmt.setInt(5, relatorio.getTotalDiagnosticos() != null ? relatorio.getTotalDiagnosticos() : 0);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                String selectSql = """
                    SELECT id FROM gs_relatorio_evento 
                    WHERE periodo = ? AND regiao = ? AND created_at = (SELECT MAX(created_at) FROM gs_relatorio_evento WHERE periodo = ? AND regiao = ?)
                    """;

                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setString(1, relatorio.getPeriodo());
                    selectStmt.setString(2, relatorio.getRegiao());
                    selectStmt.setString(3, relatorio.getPeriodo());
                    selectStmt.setString(4, relatorio.getRegiao());

                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            relatorio.setId(rs.getInt("id"));
                        }
                    }
                }
            }

            return relatorio;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar relatório: " + e.getMessage(), e);
        }
    }

    public Optional<RelatorioEvento> findById(Integer id) {
        String sql = """
            SELECT id, periodo, regiao, total_alagamentos, total_deslizamentos, total_diagnosticos,
                   created_at, updated_at, deleted_at
            FROM gs_relatorio_evento 
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRelatorio(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar relatório por ID", e);
        }
    }

    public List<RelatorioEvento> findAll() {
        String sql = """
            SELECT id, periodo, regiao, total_alagamentos, total_deslizamentos, total_diagnosticos,
                   created_at, updated_at, deleted_at
            FROM gs_relatorio_evento 
            WHERE deleted_at IS NULL
            ORDER BY created_at DESC
            """;

        List<RelatorioEvento> relatorios = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                relatorios.add(mapResultSetToRelatorio(rs));
            }

            return relatorios;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar todos os relatórios", e);
        }
    }

    public List<RelatorioEvento> findByPeriodo(String periodo) {
        String sql = """
            SELECT id, periodo, regiao, total_alagamentos, total_deslizamentos, total_diagnosticos,
                   created_at, updated_at, deleted_at
            FROM gs_relatorio_evento 
            WHERE periodo = ? AND deleted_at IS NULL
            ORDER BY regiao
            """;

        List<RelatorioEvento> relatorios = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, periodo);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    relatorios.add(mapResultSetToRelatorio(rs));
                }
            }

            return relatorios;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar relatórios por período", e);
        }
    }

    public List<RelatorioEvento> findByRegiao(String regiao) {
        String sql = """
            SELECT id, periodo, regiao, total_alagamentos, total_deslizamentos, total_diagnosticos,
                   created_at, updated_at, deleted_at
            FROM gs_relatorio_evento 
            WHERE UPPER(regiao) = UPPER(?) AND deleted_at IS NULL
            ORDER BY periodo DESC
            """;

        List<RelatorioEvento> relatorios = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, regiao);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    relatorios.add(mapResultSetToRelatorio(rs));
                }
            }

            return relatorios;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar relatórios por região", e);
        }
    }

    public RelatorioEvento gerarRelatorioAutomatico(String periodo, String regiao) {
        try (Connection conn = dataSource.getConnection()) {

            String alagamentosSql = """
                SELECT COUNT(*) as total 
                FROM gs_alagamento a
                INNER JOIN gs_endereco e ON a.endereco_id = e.id
                WHERE UPPER(e.bairro) = UPPER(?) 
                AND a.deleted_at IS NULL 
                AND e.deleted_at IS NULL
                AND TO_CHAR(a.data_ocorrencia, 'YYYY-MM') = ?
                """;

            int totalAlagamentos = 0;
            try (PreparedStatement stmt = conn.prepareStatement(alagamentosSql)) {
                stmt.setString(1, regiao);
                stmt.setString(2, periodo);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        totalAlagamentos = rs.getInt("total");
                    }
                }
            }

            String deslizamentosSql = """
                SELECT COUNT(*) as total 
                FROM gs_deslizamento d
                INNER JOIN gs_endereco e ON d.endereco_id = e.id
                WHERE UPPER(e.bairro) = UPPER(?) 
                AND d.deleted_at IS NULL 
                AND e.deleted_at IS NULL
                AND TO_CHAR(d.data_ocorrencia, 'YYYY-MM') = ?
                """;

            int totalDeslizamentos = 0;
            try (PreparedStatement stmt = conn.prepareStatement(deslizamentosSql)) {
                stmt.setString(1, regiao);
                stmt.setString(2, periodo);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        totalDeslizamentos = rs.getInt("total");
                    }
                }
            }

            int totalDiagnosticos = totalAlagamentos + totalDeslizamentos;

            RelatorioEvento relatorio = new RelatorioEvento();
            relatorio.setPeriodo(periodo);
            relatorio.setRegiao(regiao);
            relatorio.setTotalAlagamentos(totalAlagamentos);
            relatorio.setTotalDeslizamentos(totalDeslizamentos);
            relatorio.setTotalDiagnosticos(totalDiagnosticos);

            return save(relatorio);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao gerar relatório automático: " + e.getMessage(), e);
        }
    }

    public RelatorioEvento gerarRelatorioConsolidado(String periodo) {
        try (Connection conn = dataSource.getConnection()) {

            String alagamentosSql = """
                SELECT COUNT(*) as total 
                FROM gs_alagamento a
                WHERE a.deleted_at IS NULL 
                AND TO_CHAR(a.data_ocorrencia, 'YYYY-MM') = ?
                """;

            int totalAlagamentos = 0;
            try (PreparedStatement stmt = conn.prepareStatement(alagamentosSql)) {
                stmt.setString(1, periodo);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        totalAlagamentos = rs.getInt("total");
                    }
                }
            }

            String deslizamentosSql = """
                SELECT COUNT(*) as total 
                FROM gs_deslizamento d
                WHERE d.deleted_at IS NULL 
                AND TO_CHAR(d.data_ocorrencia, 'YYYY-MM') = ?
                """;

            int totalDeslizamentos = 0;
            try (PreparedStatement stmt = conn.prepareStatement(deslizamentosSql)) {
                stmt.setString(1, periodo);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        totalDeslizamentos = rs.getInt("total");
                    }
                }
            }

            int totalDiagnosticos = totalAlagamentos + totalDeslizamentos;

            RelatorioEvento relatorio = new RelatorioEvento();
            relatorio.setPeriodo(periodo);
            relatorio.setRegiao("CONSOLIDADO");
            relatorio.setTotalAlagamentos(totalAlagamentos);
            relatorio.setTotalDeslizamentos(totalDeslizamentos);
            relatorio.setTotalDiagnosticos(totalDiagnosticos);

            return save(relatorio);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao gerar relatório consolidado: " + e.getMessage(), e);
        }
    }

    public List<RelatorioEvento> gerarRelatoriosPorPeriodo(String periodo) {
        List<RelatorioEvento> relatorios = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {

            String regioesSql = """
                SELECT DISTINCT e.bairro as regiao
                FROM gs_endereco e
                WHERE e.id IN (
                    SELECT endereco_id FROM gs_alagamento WHERE deleted_at IS NULL AND TO_CHAR(data_ocorrencia, 'YYYY-MM') = ?
                    UNION
                    SELECT endereco_id FROM gs_deslizamento WHERE deleted_at IS NULL AND TO_CHAR(data_ocorrencia, 'YYYY-MM') = ?
                )
                AND e.deleted_at IS NULL
                ORDER BY e.bairro
                """;

            List<String> regioes = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(regioesSql)) {
                stmt.setString(1, periodo);
                stmt.setString(2, periodo);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        regioes.add(rs.getString("regiao"));
                    }
                }
            }

            for (String regiao : regioes) {
                RelatorioEvento relatorio = gerarRelatorioAutomatico(periodo, regiao);
                relatorios.add(relatorio);
            }

            RelatorioEvento consolidado = gerarRelatorioConsolidado(periodo);
            relatorios.add(consolidado);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao gerar relatórios por período: " + e.getMessage(), e);
        }

        return relatorios;
    }

    public RelatorioEvento update(RelatorioEvento relatorio) {
        String sql = """
            UPDATE gs_relatorio_evento 
            SET periodo = ?, regiao = ?, total_alagamentos = ?, total_deslizamentos = ?, 
                total_diagnosticos = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, relatorio.getPeriodo());
            stmt.setString(2, relatorio.getRegiao());
            stmt.setInt(3, relatorio.getTotalAlagamentos() != null ? relatorio.getTotalAlagamentos() : 0);
            stmt.setInt(4, relatorio.getTotalDeslizamentos() != null ? relatorio.getTotalDeslizamentos() : 0);
            stmt.setInt(5, relatorio.getTotalDiagnosticos() != null ? relatorio.getTotalDiagnosticos() : 0);
            stmt.setInt(6, relatorio.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Relatório não encontrado para atualização");
            }

            return relatorio;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar relatório", e);
        }
    }

    public void delete(Integer id) {
        String sql = """
            UPDATE gs_relatorio_evento 
            SET deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Relatório não encontrado para exclusão");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir relatório", e);
        }
    }

    private RelatorioEvento mapResultSetToRelatorio(ResultSet rs) throws SQLException {
        RelatorioEvento relatorio = new RelatorioEvento();
        relatorio.setId(rs.getInt("id"));
        relatorio.setPeriodo(rs.getString("periodo"));
        relatorio.setRegiao(rs.getString("regiao"));
        relatorio.setTotalAlagamentos(rs.getInt("total_alagamentos"));
        relatorio.setTotalDeslizamentos(rs.getInt("total_deslizamentos"));
        relatorio.setTotalDiagnosticos(rs.getInt("total_diagnosticos"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            relatorio.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            relatorio.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            relatorio.setDeletedAt(deletedAt.toLocalDateTime());
        }

        return relatorio;
    }
}
