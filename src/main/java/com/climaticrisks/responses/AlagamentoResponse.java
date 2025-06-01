package com.climaticrisks.responses;

import com.climaticrisks.models.Alagamento;
import com.climaticrisks.models.Endereco;

import java.time.LocalDateTime;

public class AlagamentoResponse {
    private Integer id;
    private Integer usuarioId;
    private String descricao;
    private LocalDateTime dataOcorrencia;
    private Boolean ativo;
    private Endereco endereco;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AlagamentoResponse(Alagamento alagamento, Endereco endereco) {
        this.id = alagamento.getId();
        this.usuarioId = alagamento.getUsuarioId();
        this.descricao = alagamento.getDescricao();
        this.dataOcorrencia = alagamento.getDataOcorrencia();
        this.ativo = alagamento.getAtivo();
        this.endereco = endereco;
        this.createdAt = alagamento.getCreatedAt();
        this.updatedAt = alagamento.getUpdatedAt();
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