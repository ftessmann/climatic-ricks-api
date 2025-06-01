package com.climaticrisks.responses;

import com.climaticrisks.models.Deslizamento;
import com.climaticrisks.models.Endereco;

import java.time.LocalDateTime;

public class DeslizamentoResponse {
    private Integer id;
    private Integer usuarioId;
    private String descricao;
    private LocalDateTime dataOcorrencia;
    private Boolean ativo;
    private Endereco endereco;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DeslizamentoResponse(Deslizamento deslizamento, Endereco endereco) {
        this.id = deslizamento.getId();
        this.usuarioId = deslizamento.getUsuarioId();
        this.descricao = deslizamento.getDescricao();
        this.dataOcorrencia = deslizamento.getDataOcorrencia();
        this.ativo = deslizamento.getAtivo();
        this.endereco = endereco;
        this.createdAt = deslizamento.getCreatedAt();
        this.updatedAt = deslizamento.getUpdatedAt();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDateTime getDataOcorrencia() { return dataOcorrencia; }
    public void setDataOcorrencia(LocalDateTime dataOcorrencia) { this.dataOcorrencia = dataOcorrencia; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}