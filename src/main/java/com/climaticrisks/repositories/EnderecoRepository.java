package com.climaticrisks.repositories;

import com.climaticrisks.models.Endereco;
import com.climaticrisks.enums.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EnderecoRepository {

    @Inject
    DataSource dataSource;

    public Endereco save(Endereco endereco) {
        String sql = """
            INSERT INTO gs_endereco (logradouro, bairro, cep, tipo_solo, altitude_rua,
                                   tipo_construcao, bairro_risco, proximo_corrego, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, endereco.getLogradouro());
            stmt.setString(2, endereco.getBairro());
            stmt.setString(3, endereco.getCep());
            stmt.setString(4, endereco.getTipoSolo() != null ? endereco.getTipoSolo().getValor() : null);
            stmt.setString(5, endereco.getAltitudeRua() != null ? endereco.getAltitudeRua().getValor() : null);
            stmt.setString(6, endereco.getTipoConstrucao() != null ? endereco.getTipoConstrucao().getValor() : null);
            stmt.setString(7, endereco.getBairroRisco() != null ? endereco.getBairroRisco().getValor() : null);
            stmt.setInt(8, endereco.getProximoCorrego() != null && endereco.getProximoCorrego() ? 1 : 0);
            stmt.setTimestamp(9, Timestamp.valueOf(endereco.getCreatedAt()));
            stmt.setTimestamp(10, Timestamp.valueOf(endereco.getUpdatedAt()));

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    endereco.setId(rs.getInt(1));
                }
            }

            return endereco;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar endereço", e);
        }
    }

    public Optional<Endereco> findById(Integer id) {
        String sql = """
            SELECT id, logradouro, bairro, cep, tipo_solo, altitude_rua,
                   tipo_construcao, bairro_risco, proximo_corrego, 
                   created_at, updated_at, deleted_at
            FROM gs_endereco 
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEndereco(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar endereço por ID", e);
        }
    }

    public List<Endereco> findAll() {
        String sql = """
            SELECT id, logradouro, bairro, cep, tipo_solo, altitude_rua,
                   tipo_construcao, bairro_risco, proximo_corrego, 
                   created_at, updated_at, deleted_at
            FROM gs_endereco 
            WHERE deleted_at IS NULL
            ORDER BY id
            """;

        List<Endereco> enderecos = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                enderecos.add(mapResultSetToEndereco(rs));
            }

            return enderecos;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar todos os endereços", e);
        }
    }

    public Endereco update(Endereco endereco) {
        String sql = """
            UPDATE gs_endereco
            SET logradouro = ?, bairro = ?, cep = ?, tipo_solo = ?,
                altitude_rua = ?, tipo_construcao = ?, bairro_risco = ?, 
                proximo_corrego = ?, updated_at = ?
            WHERE id = ? AND deleted_at IS NULL
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            endereco.setUpdatedAt(LocalDateTime.now());

            stmt.setString(1, endereco.getLogradouro());
            stmt.setString(2, endereco.getBairro());
            stmt.setString(3, endereco.getCep());
            stmt.setString(4, endereco.getTipoSolo() != null ? endereco.getTipoSolo().getValor() : null);
            stmt.setString(5, endereco.getAltitudeRua() != null ? endereco.getAltitudeRua().getValor() : null);
            stmt.setString(6, endereco.getTipoConstrucao() != null ? endereco.getTipoConstrucao().getValor() : null);
            stmt.setString(7, endereco.getBairroRisco() != null ? endereco.getBairroRisco().getValor() : null);
            stmt.setInt(8, endereco.getProximoCorrego() != null && endereco.getProximoCorrego() ? 1 : 0);
            stmt.setTimestamp(9, Timestamp.valueOf(endereco.getUpdatedAt()));
            stmt.setInt(10, endereco.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Endereço não encontrado para atualização");
            }

            return endereco;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar endereço", e);
        }
    }

    public void delete(Integer id) {
        String sql = """
            UPDATE gs_endereco
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
                throw new RuntimeException("Endereço não encontrado para exclusão");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir endereço", e);
        }
    }

    private Endereco mapResultSetToEndereco(ResultSet rs) throws SQLException {
        Endereco endereco = new Endereco();
        endereco.setId(rs.getInt("id"));
        endereco.setLogradouro(rs.getString("logradouro"));
        endereco.setBairro(rs.getString("bairro"));
        endereco.setCep(rs.getString("cep"));

        String tipoSolo = rs.getString("tipo_solo");
        if (tipoSolo != null) {
            endereco.setTipoSolo(TipoSolo.valueOf(tipoSolo.toUpperCase().replace("ã", "A")));
        }

        String altitudeRua = rs.getString("altitude_rua");
        if (altitudeRua != null) {
            endereco.setAltitudeRua(AltitudeRua.valueOf(altitudeRua.toUpperCase()));
        }

        String tipoConstrucao = rs.getString("tipo_construcao");
        if (tipoConstrucao != null) {
            endereco.setTipoConstrucao(TipoConstrucao.valueOf(tipoConstrucao.toUpperCase()));
        }

        String bairroRisco = rs.getString("bairro_risco");
        if (bairroRisco != null) {
            endereco.setBairroRisco(TipoRisco.valueOf(bairroRisco.toUpperCase().replace("É", "E")));
        }

        endereco.setProximoCorrego(rs.getInt("proximo_corrego") == 1);

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            endereco.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            endereco.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            endereco.setDeletedAt(deletedAt.toLocalDateTime());
        }

        return endereco;
    }
}
