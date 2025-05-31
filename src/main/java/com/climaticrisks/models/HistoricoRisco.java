package com.climaticrisks.models;

import com.climaticrisks.enums.TipoRisco;

public class HistoricoRisco extends BaseModel {
    private Integer enderecoId;
    private TipoRisco nivelRisco;
    private Integer totalEventos;

    public HistoricoRisco() {
    }

    public Integer getEnderecoId() { return enderecoId; }
    public void setEnderecoId(Integer enderecoId) { this.enderecoId = enderecoId; }

    public TipoRisco getNivelRisco() { return nivelRisco; }
    public void setNivelRisco(TipoRisco nivelRisco) { this.nivelRisco = nivelRisco; }

    public Integer getTotalEventos() { return totalEventos; }
    public void setTotalEventos(Integer totalEventos) { this.totalEventos = totalEventos; }
}