package com.atm.servidor.model;

import java.util.ArrayList;
import java.util.List;

public class Cliente {

    /** Após esta quantidade de PINs errados seguidos, o cartão fica bloqueado. */
    public static final int MAX_TENTATIVAS_PIN = 3;

    private final String nome;
    private final String cartao;
    /** Hash do PIN (ver {@link com.atm.servidor.service.Senhas}), nunca o PIN em si. */
    private final String pin;
    /** PINs errados seguidos desde o último acesso correto. */
    private int tentativasPin;
    private final List<Conta> contas = new ArrayList<>();

    public Cliente(String nome, String cartao, String pin, int tentativasPin) {
        this.nome = nome;
        this.cartao = cartao;
        this.pin = pin;
        this.tentativasPin = tentativasPin;
    }

    public boolean isBloqueado() {
        return tentativasPin >= MAX_TENTATIVAS_PIN;
    }

    /** Conta mais um PIN errado e informa se, com ele, o cartão ficou bloqueado. */
    public boolean registrarPinErrado() {
        tentativasPin++;
        return isBloqueado();
    }

    public void zerarTentativas() {
        tentativasPin = 0;
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

    public int getTentativasPin() {
        return tentativasPin;
    }

    public List<Conta> getContas() {
        return contas;
    }
}
