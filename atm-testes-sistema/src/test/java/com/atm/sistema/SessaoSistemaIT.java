package com.atm.sistema;

import static com.atm.servidor.apoio.BancoDeTeste.CARTAO_MARIA;
import static com.atm.servidor.apoio.BancoDeTeste.PIN_MARIA;
import static com.atm.sistema.TerminalDeTeste.clicar;
import static com.atm.sistema.TerminalDeTeste.entrar;
import static com.atm.sistema.TerminalDeTeste.esperarTela;
import static com.atm.sistema.TerminalDeTeste.esperarTexto;
import static com.atm.sistema.TerminalDeTeste.estaNaTela;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.atm.cliente.model.Sessao;
import com.atm.servidor.service.SessaoService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Teste de sistema da classe SessaoService (caixa preta): o que o cliente vê ao sair e quando a
 * sessão expira.
 */
@DisplayName("Sistema - Sessão: sair e sessão expirada pelo terminal")
class SessaoSistemaIT {

    @BeforeAll
    static void ligarSistema() {
        TerminalDeTeste.ligar();
    }

    @BeforeEach
    void entrarComoMaria() {
        TerminalDeTeste.voltarAoInicio();
        entrar(CARTAO_MARIA, PIN_MARIA);
        esperarTela("#saudacao");
    }

    @Test
    @DisplayName("Sair encerra a sessão e volta para a tela de cartão e senha")
    void sairVoltaParaATelaInicial() {
        clicar("Sair");

        esperarTela("#campoCartao");
        assertFalse(estaNaTela("#saudacao"));
    }

    @Test
    @DisplayName("CT-10: com a sessão expirada, a operação volta para a tela inicial com aviso")
    void sessaoExpiradaVoltaParaATelaInicial() {
        // Simula os 10 minutos sem uso: o servidor deixa de reconhecer o token deste terminal.
        SessaoService.getInstancia().encerrar(Sessao.getInstancia().getToken());

        clicar("Saldo");

        esperarTexto("#mensagem", "Sessão expirada. Insira o cartão novamente.");
        esperarTela("#campoCartao");
    }
}
