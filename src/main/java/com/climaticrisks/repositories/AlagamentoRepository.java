package com.climaticrisks.repositories;

import com.climaticrisks.models.Alagamento;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AlagamentoRepository {

    @Inject
    DataSource dataSource;

    public Alagamento save(Alagamento alagamento) {
        String sql = """
        INSERT INTO gs_alagamento (usuario_id, endereco_id, descricao, data_ocorrencia, ativo, created_at, updated_at)
        VALUES (?, ?, ?, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, alagamento.getUsuarioId());
            stmt.setInt(2, alagamento.getEnderecoId());

            if (alagamento.getDescricao() != null && !alagamento.getDescricao().trim().isEmpty()) {
                stmt.setString(3, alagamento.getDescricao());
            } else {
                stmt.setNull(3, Types.CLOB);
            }

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                String selectSql = """
                SELECT id FROM gs_alagamento 
                WHERE usuario_id = ? AND endereco_id = ? 
                AND created_at = (SELECT MAX(created_at) FROM gs_alagamento WHERE usuario_id = ?)
                """;

                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setInt(1, alagamento.getUsuarioId());
                    selectStmt.setInt(2, alagamento.getEnderecoId());
                    selectStmt.setInt(3, alagamento.getUsuarioId());

                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            alagamento.setId(rs.getInt("id"));
                        }
                    }
                }
            }

            alagamento.setDataOcorrencia(LocalDateTime.now());
            alagamento.setAtivo(true);
            return alagamento;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar alagamento: " + e.getMessage(), e);
        }
    }

    public Optional<Alagamento> findById(Integer id) {
        String sql = """
            SELECT id, usuario_id, endereco_id, descricao, data_ocorrencia, ativo,
                   created_at, updated_at, deleted_at
            FROM gs_alagamento 
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAlagamento(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar alagamento por ID", e);
        }
    }

    public List<Alagamento> findAll() {
        String sql = """
            SELECT id, usuario_id, endereco_id, descricao, data_ocorrencia, ativo,
                   created_at, updated_at, deleted_at
            FROM gs_alagamento 
            WHERE deleted_at IS NULL
            ORDER BY data_ocorrencia DESC
            """;

        List<Alagamento> alagamentos = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                alagamentos.add(mapResultSetToAlagamento(rs));
            }

            return alagamentos;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar todos os alagamentos", e);
        }
    }

    public List<Alagamento> findByUsuarioId(Integer usuarioId) {
        String sql = """
            SELECT id, usuario_id, endereco_id, descricao, data_ocorrencia, ativo,
                   created_at, updated_at, deleted_at
            FROM gs_alagamento 
            WHERE usuario_id = ? AND deleted_at IS NULL
            ORDER BY data_ocorrencia DESC
            """;

        List<Alagamento> alagamentos = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alagamentos.add(mapResultSetToAlagamento(rs));
                }
            }

            return alagamentos;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar alagamentos por usuário", e);
        }
    }

    public List<Alagamento> findByEnderecoId(Integer enderecoId) {
        String sql = """
            SELECT id, usuario_id, endereco_id, descricao, data_ocorrencia, ativo,
                   created_at, updated_at, deleted_at
            FROM gs_alagamento 
            WHERE endereco_id = ? AND deleted_at IS NULL
            ORDER BY data_ocorrencia DESC
            """;

        List<Alagamento> alagamentos = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, enderecoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alagamentos.add(mapResultSetToAlagamento(rs));
                }
            }

            return alagamentos;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar alagamentos por endereço", e);
        }
    }

    public List<Alagamento> findByPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        String sql = """
            SELECT id, usuario_id, endereco_id, descricao, data_ocorrencia, ativo,
                   created_at, updated_at, deleted_at
            FROM gs_alagamento 
            WHERE data_ocorrencia BETWEEN ? AND ? AND deleted_at IS NULL
            ORDER BY data_ocorrencia DESC
            """;

        List<Alagamento> alagamentos = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(dataInicio));
            stmt.setTimestamp(2, Timestamp.valueOf(dataFim));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alagamentos.add(mapResultSetToAlagamento(rs));
                }
            }

            return alagamentos;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar alagamentos por período", e);
        }
    }

    public Alagamento update(Alagamento alagamento) {
        String sql = """
            UPDATE gs_alagamento 
            SET descricao = ?, data_ocorrencia = ?, ativo = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, alagamento.getDescricao());
            stmt.setTimestamp(2, alagamento.getDataOcorrencia() != null ?
                    Timestamp.valueOf(alagamento.getDataOcorrencia()) : null);
            stmt.setInt(3, alagamento.getAtivo() != null && alagamento.getAtivo() ? 1 : 0);
            stmt.setInt(4, alagamento.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Alagamento não encontrado para atualização");
            }

            return alagamento;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar alagamento", e);
        }
    }

    public void delete(Integer id) {
        String sql = """
            UPDATE gs_alagamento 
            SET deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Alagamento não encontrado para exclusão");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir alagamento", e);
        }
    }

    private Alagamento mapResultSetToAlagamento(ResultSet rs) throws SQLException {
        Alagamento alagamento = new Alagamento();
        alagamento.setId(rs.getInt("id"));
        alagamento.setUsuarioId(rs.getInt("usuario_id"));
        alagamento.setEnderecoId(rs.getInt("endereco_id"));
        alagamento.setDescricao(rs.getString("descricao"));
        alagamento.setAtivo(rs.getInt("ativo") == 1);

        Timestamp dataOcorrencia = rs.getTimestamp("data_ocorrencia");
        if (dataOcorrencia != null) {
            alagamento.setDataOcorrencia(dataOcorrencia.toLocalDateTime());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            alagamento.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            alagamento.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            alagamento.setDeletedAt(deletedAt.toLocalDateTime());
        }

        return alagamento;
    }
}
