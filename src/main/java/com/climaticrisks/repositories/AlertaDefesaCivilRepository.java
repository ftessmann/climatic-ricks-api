package com.climaticrisks.repositories;

import com.climaticrisks.models.AlertaDefesaCivil;
import com.climaticrisks.enums.TipoRisco;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AlertaDefesaCivilRepository {

    @Inject
    DataSource dataSource;

    @Inject
    NotificacaoRepository notificacaoRepository;

    public AlertaDefesaCivil save(AlertaDefesaCivil alerta) {
        String sql = """
            INSERT INTO gs_alerta_defesa_civil (titulo, descricao, nivel_alerta, bairros_afetados, data_inicio, ativo, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, alerta.getTitulo());

            if (alerta.getDescricao() != null && !alerta.getDescricao().trim().isEmpty()) {
                stmt.setString(2, alerta.getDescricao());
            } else {
                stmt.setNull(2, Types.CLOB);
            }

            stmt.setString(3, alerta.getNivelAlerta() != null ? alerta.getNivelAlerta().getValor() : "baixo");
            stmt.setString(4, alerta.getBairrosAfetados());

            if (alerta.getDataInicio() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(alerta.getDataInicio()));
            } else {
                stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            }

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                String selectSql = """
                    SELECT id FROM gs_alerta_defesa_civil 
                    WHERE titulo = ? AND created_at = (SELECT MAX(created_at) FROM gs_alerta_defesa_civil WHERE titulo = ?)
                    """;

                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setString(1, alerta.getTitulo());
                    selectStmt.setString(2, alerta.getTitulo());

                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            alerta.setId(rs.getInt("id"));
                        }
                    }
                }

                if (alerta.getBairrosAfetados() != null && !alerta.getBairrosAfetados().trim().isEmpty()) {
                    String bairroAfetado = alerta.getBairrosAfetados().trim();
                    String tituloNotificacao = alerta.getTitulo();
                    String mensagemNotificacao = String.format(
                            "Novo alerta para o bairro %s: %s",
                            bairroAfetado,
                            alerta.getDescricao() != null ? alerta.getDescricao() : "Verifique as informações oficiais."
                    );

                    notificacaoRepository.notificarUsuariosPorBairro(
                            bairroAfetado,
                            tituloNotificacao,
                            mensagemNotificacao,
                            alerta.getNivelAlerta()
                    );
                }
            }

            alerta.setAtivo(true);
            return alerta;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar alerta: " + e.getMessage(), e);
        }
    }

    public Optional<AlertaDefesaCivil> findById(Integer id) {
        String sql = """
            SELECT id, titulo, descricao, nivel_alerta, bairros_afetados, data_inicio, ativo,
                   created_at, updated_at, deleted_at
            FROM gs_alerta_defesa_civil 
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAlerta(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar alerta por ID", e);
        }
    }

    public List<AlertaDefesaCivil> findAll() {
        String sql = """
            SELECT id, titulo, descricao, nivel_alerta, bairros_afetados, data_inicio, ativo,
                   created_at, updated_at, deleted_at
            FROM gs_alerta_defesa_civil 
            WHERE deleted_at IS NULL
            ORDER BY data_inicio DESC
            """;

        List<AlertaDefesaCivil> alertas = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                alertas.add(mapResultSetToAlerta(rs));
            }

            return alertas;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar todos os alertas", e);
        }
    }

    public List<AlertaDefesaCivil> findAtivos() {
        String sql = """
            SELECT id, titulo, descricao, nivel_alerta, bairros_afetados, data_inicio, ativo,
                   created_at, updated_at, deleted_at
            FROM gs_alerta_defesa_civil 
            WHERE ativo = 1 AND deleted_at IS NULL
            ORDER BY data_inicio DESC
            """;

        List<AlertaDefesaCivil> alertas = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                alertas.add(mapResultSetToAlerta(rs));
            }

            return alertas;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar alertas ativos", e);
        }
    }

    public List<AlertaDefesaCivil> findByNivelAlerta(TipoRisco nivelAlerta) {
        String sql = """
            SELECT id, titulo, descricao, nivel_alerta, bairros_afetados, data_inicio, ativo,
                   created_at, updated_at, deleted_at
            FROM gs_alerta_defesa_civil 
            WHERE nivel_alerta = ? AND ativo = 1 AND deleted_at IS NULL
            ORDER BY data_inicio DESC
            """;

        List<AlertaDefesaCivil> alertas = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nivelAlerta.getValor());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alertas.add(mapResultSetToAlerta(rs));
                }
            }

            return alertas;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar alertas por nível", e);
        }
    }

    public AlertaDefesaCivil update(AlertaDefesaCivil alerta) {
        String sql = """
            UPDATE gs_alerta_defesa_civil 
            SET titulo = ?, descricao = ?, nivel_alerta = ?, bairros_afetados = ?, 
                data_inicio = ?, ativo = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, alerta.getTitulo());
            stmt.setString(2, alerta.getDescricao());
            stmt.setString(3, alerta.getNivelAlerta() != null ? alerta.getNivelAlerta().getValor() : "baixo");
            stmt.setString(4, alerta.getBairrosAfetados());
            stmt.setTimestamp(5, alerta.getDataInicio() != null ?
                    Timestamp.valueOf(alerta.getDataInicio()) : null);
            stmt.setInt(6, alerta.getAtivo() != null && alerta.getAtivo() ? 1 : 0);
            stmt.setInt(7, alerta.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Alerta não encontrado para atualização");
            }

            return alerta;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar alerta", e);
        }
    }

    public void delete(Integer id) {
        String sql = """
            UPDATE gs_alerta_defesa_civil 
            SET deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Alerta não encontrado para exclusão");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir alerta", e);
        }
    }

    public void desativar(Integer id) {
        String sql = """
            UPDATE gs_alerta_defesa_civil 
            SET ativo = 0, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Alerta não encontrado para desativação");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar alerta", e);
        }
    }

    private AlertaDefesaCivil mapResultSetToAlerta(ResultSet rs) throws SQLException {
        AlertaDefesaCivil alerta = new AlertaDefesaCivil();
        alerta.setId(rs.getInt("id"));
        alerta.setTitulo(rs.getString("titulo"));
        alerta.setDescricao(rs.getString("descricao"));
        alerta.setBairrosAfetados(rs.getString("bairros_afetados"));
        alerta.setAtivo(rs.getInt("ativo") == 1);

        String nivelAlerta = rs.getString("nivel_alerta");
        if (nivelAlerta != null) {
            switch (nivelAlerta.toLowerCase()) {
                case "baixo" -> alerta.setNivelAlerta(TipoRisco.BAIXO);
                case "medio", "médio" -> alerta.setNivelAlerta(TipoRisco.MEDIO);
                case "alto" -> alerta.setNivelAlerta(TipoRisco.ALTO);
            }
        }

        Timestamp dataInicio = rs.getTimestamp("data_inicio");
        if (dataInicio != null) {
            alerta.setDataInicio(dataInicio.toLocalDateTime());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            alerta.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            alerta.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            alerta.setDeletedAt(deletedAt.toLocalDateTime());
        }

        return alerta;
    }
}
