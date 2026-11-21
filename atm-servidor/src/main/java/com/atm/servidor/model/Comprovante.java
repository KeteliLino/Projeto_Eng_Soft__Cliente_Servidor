package com.atm.servidor.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Comprovante(int transacaoId, String conta, TipoTransacao tipo, BigDecimal valor,
                          BigDecimal saldo, LocalDateTime dataHora) {
}
