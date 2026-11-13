package com.atm.cliente.model;

import java.math.BigDecimal;
import java.util.List;

public record Extrato(String conta, BigDecimal saldo, List<Transacao> transacoes) {
}
