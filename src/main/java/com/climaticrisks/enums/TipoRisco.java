package com.climaticrisks.enums;

public enum TipoRisco {
    BAIXO("baixo"), MEDIO("médio"), ALTO("alto");

    private final String valor;
    TipoRisco(String valor) { this.valor = valor; }
    public String getValor() { return valor; }
}
