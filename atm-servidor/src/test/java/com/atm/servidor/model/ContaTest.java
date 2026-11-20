package com.atm.servidor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Teste de unidade da classe Conta (caixa branca): só a classe, sem banco nem rede. */
@DisplayName("Unidade - Conta (RF-03, RN-01)")
class ContaTest {

    private final Conta conta = new Conta("0001-1", new BigDecimal("100.00"));

    @Test
    @DisplayName("Saque diminui o saldo")
    void sacarDiminuiOSaldo() {
        conta.sacar(new BigDecimal("30.50"));
        assertEquals(new BigDecimal("69.50"), conta.getSaldo());
    }

    @Test
    @DisplayName("Saque acima do saldo é recusado e o saldo não muda")
    void sacarMaisQueOSaldoFalhaESaldoNaoMuda() {
        assertThrows(SaldoInsuficienteException.class, () -> conta.sacar(new BigDecimal("100.01")));
        assertEquals(new BigDecimal("100.00"), conta.getSaldo());
    }
}
