package com.atm.servidor.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Conta {

    private final String numero;
    private BigDecimal saldo;
    private final List<Transacao> transacoes = new ArrayList<>();

    public Conta(String numero, BigDecimal saldo) {
        this.numero = numero;
        this.saldo = saldo;
    }

    public void sacar(BigDecimal valor) {
        exigirPositivo(valor);
        if (saldo.compareTo(valor) < 0) {
            throw new SaldoInsuficienteException();
        }
        saldo = saldo.subtract(valor);
    }

    public void depositar(BigDecimal valor) {
        exigirPositivo(valor);
        saldo = saldo.add(valor);
    }

    private void exigirPositivo(BigDecimal valor) {
        if (valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor deve ser positivo");
        }
    }

    public String getNumero() {
        return numero;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public List<Transacao> getTransacoes() {
        return transacoes;
    }
}
