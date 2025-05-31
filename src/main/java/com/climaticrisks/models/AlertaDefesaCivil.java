package com.climaticrisks.models;

import com.climaticrisks.enums.TipoRisco;

import java.time.LocalDateTime;

public class AlertaDefesaCivil extends BaseModel {
    private String titulo;
    private String descricao;
    private TipoRisco nivelAlerta;
    private String bairrosAfetados;
    private LocalDateTime dataInicio;
    private Boolean ativo;

    public AlertaDefesaCivil() {
        super();
        this.ativo = true;
    }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public TipoRisco getNivelAlerta() { return nivelAlerta; }
    public void setNivelAlerta(TipoRisco nivelAlerta) { this.nivelAlerta = nivelAlerta; }

    public String getBairrosAfetados() { return bairrosAfetados; }
    public void setBairrosAfetados(String bairrosAfetados) { this.bairrosAfetados = bairrosAfetados; }

    public LocalDateTime getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
