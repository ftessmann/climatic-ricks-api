package com.climaticrisks.enums;

public enum AltitudeRua {
    NIVEL("nível"), ABAIXO("abaixo"), ACIMA("acima");

    private final String valor;
    AltitudeRua(String valor) { this.valor = valor; }
    public String getValor() { return valor; }
}
