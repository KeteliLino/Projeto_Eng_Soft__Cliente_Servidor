package com.atm.sistema;

import static com.atm.servidor.apoio.BancoDeTeste.CARTAO_MARIA;
import static com.atm.servidor.apoio.BancoDeTeste.PIN_MARIA;
import static com.atm.sistema.TerminalDeTeste.celulasDaColuna;
import static com.atm.sistema.TerminalDeTeste.clicar;
import static com.atm.sistema.TerminalDeTeste.digitar;
import static com.atm.sistema.TerminalDeTeste.entrar;
import static com.atm.sistema.TerminalDeTeste.esperarTela;
import static com.atm.sistema.TerminalDeTeste.esperarTexto;
import static com.atm.sistema.TerminalDeTeste.texto;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Teste de sistema da classe Conta (caixa preta): saque, saldo e extrato feitos pelas telas
 * do terminal, do ponto de vista do cliente.
 */
@DisplayName("Sistema - Conta: saque, saldo e extrato pelo terminal")
class ContaSistemaIT {

    @BeforeAll
    static void ligarSistema() {
        TerminalDeTeste.ligar();
    }

    @BeforeEach
    void entrarComoMaria() {
        TerminalDeTeste.voltarAoInicio();          // conta 0001-1 da Maria com R$ 1.000,00
        entrar(CARTAO_MARIA, PIN_MARIA);
        esperarTela("#saudacao");
    }

    @Test
    @DisplayName("CT-05: saque mostra o comprovante com o aviso das cédulas e o novo saldo")
    void saqueMostraComprovanteENovoSaldo() {
        clicar("Saque");
        digitar("#campoValor", "100,00");
        clicar("Confirmar");

        esperarTexto("#aviso", "Retire suas cédulas e o comprovante.");
        assertEquals("R$ 100,00", texto("#valor"));
        assertEquals("R$ 900,00", texto("#saldo"));
    }

    @Test
    @DisplayName("CT-08: a tela de saldo mostra o saldo da conta escolhida")
    void consultaDeSaldoMostraOSaldoDaConta() {
        clicar("Saldo");

        esperarTexto("#contaInfo", "Conta 0001-1");
        esperarTexto("#valorSaldo", "R$ 1.000,00");
    }

    @Test
    @DisplayName("CT-09: extrato mostra o saldo após cada transação, com depósito em azul e saque em vermelho")
    void extratoMostraSaldoAposCadaTransacao() {
        clicar("Depósito");
        digitar("#campoValor", "100");
        clicar("Confirmar");
        esperarTela("#aviso");
        clicar("Voltar ao menu");

        clicar("Saque");
        digitar("#campoValor", "30");
        clicar("Confirmar");
        esperarTela("#aviso");
        clicar("Voltar ao menu");

        clicar("Extrato");
        esperarTexto("#valorSaldo", "R$ 1.070,00");

        // Da mais recente para a mais antiga: primeiro o saque, depois o depósito.
        List<String[]> valores = celulasDaColuna("#colunaValor");
        List<String[]> saldos = celulasDaColuna("#colunaSaldo");
        assertEquals(2, valores.size());
        assertArrayEquals(new String[] {"- R$ 30,00", "debito"}, valores.get(0));
        assertArrayEquals(new String[] {"+ R$ 100,00", "credito"}, valores.get(1));
        assertArrayEquals(new String[] {"R$ 1.070,00", "saldo-extrato"}, saldos.get(0));
        assertArrayEquals(new String[] {"R$ 1.100,00", "saldo-extrato"}, saldos.get(1));
    }
}
