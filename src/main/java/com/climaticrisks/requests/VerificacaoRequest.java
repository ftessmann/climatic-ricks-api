package com.climaticrisks.requests;

public class VerificacaoRequest {
    private Integer alagamentoId;
    private Integer deslizamentoId;
    private Boolean confirmacao;

    public Integer getAlagamentoId() { return alagamentoId; }
    public void setAlagamentoId(Integer alagamentoId) { this.alagamentoId = alagamentoId; }

    public Integer getDeslizamentoId() { return deslizamentoId; }
    public void setDeslizamentoId(Integer deslizamentoId) { this.deslizamentoId = deslizamentoId; }

    public Boolean getConfirmacao() { return confirmacao; }
    public void setConfirmacao(Boolean confirmacao) { this.confirmacao = confirmacao; }
}