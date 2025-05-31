package com.climaticrisks.models;

import java.time.LocalDateTime;

public class Deslizamento extends BaseModel {
    private Integer usuarioId;
    private Integer enderecoId;
    private String descricao;
    private LocalDateTime dataOcorrencia;
    private Boolean ativo;

    public Deslizamento() {
        super();
        this.ativo = true;
    }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public Integer getEnderecoId() { return enderecoId; }
    public void setEnderecoId(Integer enderecoId) { this.enderecoId = enderecoId; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDateTime getDataOcorrencia() { return dataOcorrencia; }
    public void setDataOcorrencia(LocalDateTime dataOcorrencia) { this.dataOcorrencia = dataOcorrencia; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}