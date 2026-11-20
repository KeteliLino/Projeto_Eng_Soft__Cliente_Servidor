package com.atm.servidor.integracao;

import static com.atm.servidor.apoio.BancoDeTeste.CARTAO_MARIA;
import static com.atm.servidor.apoio.BancoDeTeste.CONTA_MARIA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.atm.servidor.apoio.BancoDeTeste;
import com.atm.servidor.command.DepositoCommand;
import com.atm.servidor.command.SaqueCommand;
import com.atm.servidor.model.Comprovante;
import com.atm.servidor.model.SaldoInsuficienteException;
import com.atm.servidor.service.ContaService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Teste de integração da classe Conta (caixa cinza): Conta + comandos + ContaService + DAOs + MySQL.
 * Confere que as regras testadas na unidade (ContaTest) continuam valendo quando as partes trabalham
 * juntas e o resultado fica gravado no banco.
 */
@DisplayName("Integração - Conta (RF-02, RF-03, RF-06, RN-01, RNF-03, RNF-09)")
class ContaIntegracaoIT {

    private ContaService contaService;

    @BeforeAll
    static void apontarParaOBancoDeTeste() {
        BancoDeTeste.configurar();
    }

    @BeforeEach
    void prepararBanco() {
        BancoDeTeste.recriar();                       // conta 0001-1 da Maria começa com R$ 1.000,00
        contaService = new ContaService();
    }

    @Test
    @DisplayName("Depósito grava o novo saldo, uma transação e o log de sucesso")
    void depositoGravaSaldoTransacaoELog() throws Exception {
        Comprovante comprovante = contaService.executar(CARTAO_MARIA, CONTA_MARIA,
                new DepositoCommand(new BigDecimal("50.00")));

        assertEquals(new BigDecimal("1050.00"), comprovante.saldo());
        assertEquals(new BigDecimal("1050.00"), BancoDeTeste.saldo(CONTA_MARIA));
        assertEquals(1, BancoDeTeste.quantidadeDeTransacoes(CONTA_MARIA));
        assertEquals(List.of("DEPOSITO SUCESSO"), BancoDeTeste.resultadosDoLog(CONTA_MARIA));
    }

    @Test
    @DisplayName("Saque acima do saldo é recusado: banco sem alteração e log RECUSADA")
    void saqueAcimaDoSaldoNaoAlteraOBanco() {
        assertThrows(SaldoInsuficienteException.class, () -> contaService.executar(CARTAO_MARIA, CONTA_MARIA,
                new SaqueCommand(new BigDecimal("1000.01"))));

        assertEquals(new BigDecimal("1000.00"), BancoDeTeste.saldo(CONTA_MARIA));
        assertEquals(0, BancoDeTeste.quantidadeDeTransacoes(CONTA_MARIA));
        assertEquals(List.of("SAQUE RECUSADA"), BancoDeTeste.resultadosDoLog(CONTA_MARIA));
    }
}
