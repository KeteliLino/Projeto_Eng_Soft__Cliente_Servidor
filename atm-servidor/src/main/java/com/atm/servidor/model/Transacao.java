package com.atm.servidor.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transacao {

    private int id;
    private final TipoTransacao tipo;
    private final BigDecimal valor;
    private final LocalDateTime dataHora;

    public Transacao(int id, TipoTransacao tipo, BigDecimal valor, LocalDateTime dataHora) {
        this.id = id;
        this.tipo = tipo;
        this.valor = valor;
        this.dataHora = dataHora;
    }

    public Transacao(TipoTransacao tipo, BigDecimal valor, LocalDateTime dataHora) {
        this(0, tipo, valor, dataHora);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TipoTransacao getTipo() {
        return tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
