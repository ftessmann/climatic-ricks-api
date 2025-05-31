package com.climaticrisks.enums;

public enum TipoConstrucao {
    MADEIRA("madeira"), ALVERNARIA("alvernaria"), MISTA("mista");

    private final String valor;
    TipoConstrucao(String valor) { this.valor = valor; }
    public String getValor() { return valor; }
}
