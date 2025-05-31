package com.climaticrisks.models;

import com.climaticrisks.enums.TipoRisco;

public class Notificacao extends BaseModel {
    private Integer usuarioId;
    private String titulo;
    private String mensagem;
    private TipoRisco prioridade;
    private Boolean lida;

    public Notificacao() {
        super();
        this.lida = false;
    }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public TipoRisco getPrioridade() { return prioridade; }
    public void setPrioridade(TipoRisco prioridade) { this.prioridade = prioridade; }

    public Boolean getLida() { return lida; }
    public void setLida(Boolean lida) { this.lida = lida; }
}