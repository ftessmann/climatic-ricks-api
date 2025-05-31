package com.climaticrisks.models;

import com.climaticrisks.enums.*;

public class Endereco extends BaseModel {
    private String logradouro;
    private String bairro;
    private String cep;
    private TipoSolo tipoSolo;
    private AltitudeRua altitudeRua;
    private TipoConstrucao tipoConstrucao;
    private TipoRisco bairroRisco;
    private Boolean proximoCorrego;

    public Endereco() {
        super();
    }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public TipoSolo getTipoSolo() { return tipoSolo; }
    public void setTipoSolo(TipoSolo tipoSolo) { this.tipoSolo = tipoSolo; }

    public AltitudeRua getAltitudeRua() { return altitudeRua; }
    public void setAltitudeRua(AltitudeRua altitudeRua) { this.altitudeRua = altitudeRua; }

    public TipoConstrucao getTipoConstrucao() { return tipoConstrucao; }
    public void setTipoConstrucao(TipoConstrucao tipoConstrucao) { this.tipoConstrucao = tipoConstrucao; }

    public TipoRisco getBairroRisco() { return bairroRisco; }
    public void setBairroRisco(TipoRisco bairroRisco) { this.bairroRisco = bairroRisco; }

    public Boolean getProximoCorrego() { return proximoCorrego; }
    public void setProximoCorrego(Boolean proximoCorrego) { this.proximoCorrego = proximoCorrego; }
}
