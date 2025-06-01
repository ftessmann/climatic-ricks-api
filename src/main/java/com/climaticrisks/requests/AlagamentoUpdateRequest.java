package com.climaticrisks.requests;

import java.time.LocalDateTime;

public class AlagamentoUpdateRequest {
    private String descricao;
    private LocalDateTime dataOcorrencia;

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDateTime getDataOcorrencia() { return dataOcorrencia; }
    public void setDataOcorrencia(LocalDateTime dataOcorrencia) { this.dataOcorrencia = dataOcorrencia; }
}