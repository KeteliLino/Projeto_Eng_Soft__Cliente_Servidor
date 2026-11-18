package com.atm.servidor.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.atm.servidor.model.Conta;
import com.atm.servidor.model.SaldoInsuficienteException;
import com.atm.servidor.service.ApiException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MovimentacaoCommandTest {

    @Test
    void valoresInvalidosSaoRecusadosAoCriarOComando() {
        assertEquals(400, recusar(null));
        assertEquals(400, recusar("0"));
        assertEquals(400, recusar("-10"));
        assertEquals(400, recusar("1.234"));
        assertEquals(400, recusar("1000000.01"));
    }

    private int recusar(String valor) {
        BigDecimal numero = valor == null ? null : new BigDecimal(valor);
        return assertThrows(ApiException.class, () -> new SaqueCommand(numero)).getStatus();
    }

    @Test
    void limiteEDuasCasasDecimaisSaoAceitos() {
        assertEquals(new BigDecimal("1000000"), new DepositoCommand(new BigDecimal("1000000")).getValor());
        assertEquals(new BigDecimal("0.01"), new SaqueCommand(new BigDecimal("0.01")).getValor());
    }

    @Test
    void saqueEDepositoAlteramOSaldoEConsultasNao() {
        assertTrue(new SaqueCommand(BigDecimal.TEN).alteraSaldo());
        assertTrue(new DepositoCommand(BigDecimal.TEN).alteraSaldo());
        assertFalse(new SaldoCommand().alteraSaldo());
        assertFalse(new ExtratoCommand().alteraSaldo());
    }

    @Test
    void saqueAcimaDoSaldoNaoAlteraAConta() {
        Conta conta = new Conta("0001-1", new BigDecimal("50.00"));
        SaqueCommand saque = new SaqueCommand(new BigDecimal("50.01"));
        assertThrows(SaldoInsuficienteException.class, () -> saque.aplicar(conta, saque.getValor()));
        assertEquals(new BigDecimal("50.00"), conta.getSaldo());
    }

    @Test
    void nomesGravadosNoLog() {
        assertEquals("SAQUE", new SaqueCommand(BigDecimal.ONE).getNome());
        assertEquals("DEPOSITO", new DepositoCommand(BigDecimal.ONE).getNome());
        assertEquals("SALDO", new SaldoCommand().getNome());
        assertEquals("EXTRATO", new ExtratoCommand().getNome());
    }
}
