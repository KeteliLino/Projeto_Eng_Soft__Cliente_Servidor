package com.atm.servidor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ContaTest {

    private final Conta conta = new Conta("0001-1", new BigDecimal("100.00"));

    @Test
    void sacarDiminuiOSaldo() {
        conta.sacar(new BigDecimal("30.50"));
        assertEquals(new BigDecimal("69.50"), conta.getSaldo());
    }

    @Test
    void sacarTodoOSaldoEhPermitido() {
        conta.sacar(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("0.00"), conta.getSaldo());
    }

    @Test
    void sacarMaisQueOSaldoFalhaESaldoNaoMuda() {
        assertThrows(SaldoInsuficienteException.class, () -> conta.sacar(new BigDecimal("100.01")));
        assertEquals(new BigDecimal("100.00"), conta.getSaldo());
    }

    @Test
    void depositarAumentaOSaldo() {
        conta.depositar(new BigDecimal("0.10"));
        assertEquals(new BigDecimal("100.10"), conta.getSaldo());
    }

    @Test
    void valorNaoPositivoEhRecusado() {
        assertThrows(IllegalArgumentException.class, () -> conta.sacar(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> conta.depositar(new BigDecimal("-1")));
    }
}
