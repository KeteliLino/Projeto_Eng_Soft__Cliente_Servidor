package com.atm.cliente.model;

public enum TipoTransacao {
    SAQUE("Saque"),
    DEPOSITO("Depósito");

    private final String rotulo;

    TipoTransacao(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
