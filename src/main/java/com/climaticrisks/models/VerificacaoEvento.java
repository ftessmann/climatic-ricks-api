package com.climaticrisks.models;

public class VerificacaoEvento extends BaseModel {
    private Integer usuarioId;
    private Integer alagamentoId;
    private Integer deslizamentoId;
    private Boolean confirmacao;

    public VerificacaoEvento() {
        super();
    }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public Integer getAlagamentoId() { return alagamentoId; }
    public void setAlagamentoId(Integer alagamentoId) { this.alagamentoId = alagamentoId; }

    public Integer getDeslizamentoId() { return deslizamentoId; }
    public void setDeslizamentoId(Integer deslizamentoId) { this.deslizamentoId = deslizamentoId; }

    public Boolean getConfirmacao() { return confirmacao; }
    public void setConfirmacao(Boolean confirmacao) { this.confirmacao = confirmacao; }
}