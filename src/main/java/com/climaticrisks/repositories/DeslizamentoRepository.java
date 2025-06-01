package com.climaticrisks.repositories;

import com.climaticrisks.models.Deslizamento;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DeslizamentoRepository {

    @Inject
    DataSource dataSource;

    @Inject
    HistoricoRiscoRepository historicoRiscoRepository;

    public Deslizamento save(Deslizamento deslizamento) {
        String sql = """
        INSERT INTO gs_deslizamento (usuario_id, endereco_id, descricao, data_ocorrencia, ativo, created_at, updated_at)
        VALUES (?, ?, ?, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deslizamento.getUsuarioId());
            stmt.setInt(2, deslizamento.getEnderecoId());

            if (deslizamento.getDescricao() != null && !deslizamento.getDescricao().trim().isEmpty()) {
                stmt.setString(3, deslizamento.getDescricao());
            } else {
                stmt.setNull(3, Types.CLOB);
            }

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                String selectSql = """
                SELECT id FROM gs_deslizamento 
                WHERE usuario_id = ? AND endereco_id = ? 
                AND created_at = (SELECT MAX(created_at) FROM gs_deslizamento WHERE usuario_id = ?)
                """;

                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setInt(1, deslizamento.getUsuarioId());
                    selectStmt.setInt(2, deslizamento.getEnderecoId());
                    selectStmt.setInt(3, deslizamento.getUsuarioId());

                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            deslizamento.setId(rs.getInt("id"));
                        }
                    }
                }
            }

            deslizamento.setDataOcorrencia(LocalDateTime.now());
            deslizamento.setAtivo(true);

            if (deslizamento.getId() != null && deslizamento.getEnderecoId() != null) {
                historicoRiscoRepository.atualizarHistoricoRisco(deslizamento.getEnderecoId());
            }

            return deslizamento;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar deslizamento: " + e.getMessage(), e);
        }
    }


    public Optional<Deslizamento> findById(Integer id) {
        String sql = """
            SELECT id, usuario_id, endereco_id, descricao, data_ocorrencia, ativo,
                   created_at, updated_at, deleted_at
            FROM gs_deslizamento 
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDeslizamento(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar deslizamento por ID", e);
        }
    }

    public List<Deslizamento> findAll() {
        String sql = """
            SELECT id, usuario_id, endereco_id, descricao, data_ocorrencia, ativo,
                   created_at, updated_at, deleted_at
            FROM gs_deslizamento 
            WHERE deleted_at IS NULL
            ORDER BY data_ocorrencia DESC
            """;

        List<Deslizamento> deslizamentos = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                deslizamentos.add(mapResultSetToDeslizamento(rs));
            }

            return deslizamentos;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar todos os deslizamentos", e);
        }
    }

    public List<Deslizamento> findByUsuarioId(Integer usuarioId) {
        String sql = """
            SELECT id, usuario_id, endereco_id, descricao, data_ocorrencia, ativo,
                   created_at, updated_at, deleted_at
            FROM gs_deslizamento 
            WHERE usuario_id = ? AND deleted_at IS NULL
            ORDER BY data_ocorrencia DESC
            """;

        List<Deslizamento> deslizamentos = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    deslizamentos.add(mapResultSetToDeslizamento(rs));
                }
            }

            return deslizamentos;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar deslizamentos por usuário", e);
        }
    }

    public List<Deslizamento> findByEnderecoId(Integer enderecoId) {
        String sql = """
            SELECT id, usuario_id, endereco_id, descricao, data_ocorrencia, ativo,
                   created_at, updated_at, deleted_at
            FROM gs_deslizamento 
            WHERE endereco_id = ? AND deleted_at IS NULL
            ORDER BY data_ocorrencia DESC
            """;

        List<Deslizamento> deslizamentos = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, enderecoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    deslizamentos.add(mapResultSetToDeslizamento(rs));
                }
            }

            return deslizamentos;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar deslizamentos por endereço", e);
        }
    }

    public List<Deslizamento> findByPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        String sql = """
            SELECT id, usuario_id, endereco_id, descricao, data_ocorrencia, ativo,
                   created_at, updated_at, deleted_at
            FROM gs_deslizamento 
            WHERE data_ocorrencia BETWEEN ? AND ? AND deleted_at IS NULL
            ORDER BY data_ocorrencia DESC
            """;

        List<Deslizamento> deslizamentos = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(dataInicio));
            stmt.setTimestamp(2, Timestamp.valueOf(dataFim));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    deslizamentos.add(mapResultSetToDeslizamento(rs));
                }
            }

            return deslizamentos;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar deslizamentos por período", e);
        }
    }

    public Deslizamento update(Deslizamento deslizamento) {
        String sql = """
            UPDATE gs_deslizamento 
            SET descricao = ?, data_ocorrencia = ?, ativo = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, deslizamento.getDescricao());
            stmt.setTimestamp(2, deslizamento.getDataOcorrencia() != null ?
                    Timestamp.valueOf(deslizamento.getDataOcorrencia()) : null);
            stmt.setInt(3, deslizamento.getAtivo() != null && deslizamento.getAtivo() ? 1 : 0);
            stmt.setInt(4, deslizamento.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Deslizamento não encontrado para atualização");
            }

            return deslizamento;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar deslizamento", e);
        }
    }

    public void delete(Integer id) {
        String sql = """
            UPDATE gs_deslizamento 
            SET deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Deslizamento não encontrado para exclusão");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir deslizamento", e);
        }
    }

    private Deslizamento mapResultSetToDeslizamento(ResultSet rs) throws SQLException {
        Deslizamento deslizamento = new Deslizamento();
        deslizamento.setId(rs.getInt("id"));
        deslizamento.setUsuarioId(rs.getInt("usuario_id"));
        deslizamento.setEnderecoId(rs.getInt("endereco_id"));
        deslizamento.setDescricao(rs.getString("descricao"));
        deslizamento.setAtivo(rs.getInt("ativo") == 1);

        Timestamp dataOcorrencia = rs.getTimestamp("data_ocorrencia");
        if (dataOcorrencia != null) {
            deslizamento.setDataOcorrencia(dataOcorrencia.toLocalDateTime());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            deslizamento.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            deslizamento.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            deslizamento.setDeletedAt(deletedAt.toLocalDateTime());
        }

        return deslizamento;
    }
}
