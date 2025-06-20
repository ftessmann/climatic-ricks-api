package com.climaticrisks.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoConstrucao {
    MADEIRA("madeira"),
    ALVERNARIA("alvernaria"),
    MISTA("mista");

    private final String valor;

    TipoConstrucao(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static TipoConstrucao fromString(String value) {
        for (TipoConstrucao tipo : TipoConstrucao.values()) {
            if (tipo.getValor().equalsIgnoreCase(value)) {
                return tipo;
            }
        }

        if ("alvenaria".equalsIgnoreCase(value)) {
            return ALVERNARIA;
        }

        throw new IllegalArgumentException("Tipo de construção inválido: " + value);
    }
}
