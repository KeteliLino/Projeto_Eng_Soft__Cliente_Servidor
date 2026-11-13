package com.atm.cliente.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Transacao(int id, TipoTransacao tipo, BigDecimal valor, LocalDateTime dataHora) {
}
