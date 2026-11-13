package com.atm.servidor.model;

import java.util.ArrayList;
import java.util.List;

public class Cliente {

    private final String nome;
    private final String cartao;
    /** Hash do PIN (ver {@link com.atm.servidor.service.Senhas}), nunca o PIN em si. */
    private final String pin;
    private final List<Conta> contas = new ArrayList<>();

    public Cliente(String nome, String cartao, String pin) {
        this.nome = nome;
        this.cartao = cartao;
        this.pin = pin;
    }

    public String getNome() {
        return nome;
    }

    public String getCartao() {
        return cartao;
    }

    public String getPin() {
        return pin;
    }

    public List<Conta> getContas() {
        return contas;
    }
}
