package com.climaticrisks.requests;

import com.climaticrisks.models.Endereco;

import java.time.LocalDateTime;

public class AlagamentoRequest {
    private Endereco endereco;
    private String descricao;
    private LocalDateTime dataOcorrencia;

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDateTime getDataOcorrencia() { return dataOcorrencia; }
    public void setDataOcorrencia(LocalDateTime dataOcorrencia) { this.dataOcorrencia = dataOcorrencia; }
}