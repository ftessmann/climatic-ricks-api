package com.climaticrisks.enums;

public enum TipoSolo {
    VEGETACAO("vegetação"), TERRA("terra"), ASFALTO("asfalto");

    private final String valor;
    TipoSolo(String valor) { this.valor = valor; }
    public String getValor() { return valor; }
}
