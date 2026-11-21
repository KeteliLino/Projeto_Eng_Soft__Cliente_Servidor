package com.atm.servidor.model;

import java.math.BigDecimal;
import java.util.List;

public record Extrato(String conta, BigDecimal saldo, List<Transacao> transacoes) {
}
