package com.climaticrisks.requests;

public class RelatorioRequest {
    private String periodo;
    private String regiao;

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public String getRegiao() { return regiao; }
    public void setRegiao(String regiao) { this.regiao = regiao; }
}