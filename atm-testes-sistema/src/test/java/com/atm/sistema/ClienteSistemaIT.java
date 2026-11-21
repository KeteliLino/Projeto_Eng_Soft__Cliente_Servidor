package com.atm.sistema;

import static com.atm.servidor.apoio.BancoDeTeste.CARTAO_MARIA;
import static com.atm.servidor.apoio.BancoDeTeste.PIN_MARIA;
import static com.atm.sistema.TerminalDeTeste.entrar;
import static com.atm.sistema.TerminalDeTeste.esperarTela;
import static com.atm.sistema.TerminalDeTeste.esperarTexto;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Teste de sistema da classe Cliente (caixa preta): entrada com cartão e senha e bloqueio do cartão,
 * pela tela inicial do terminal.
 */
@DisplayName("Sistema - Cliente: cartão, senha e bloqueio pelo terminal")
class ClienteSistemaIT {

    private static final String SENHA_ERRADA = "9999";
    private static final String CARTAO_OU_SENHA_INVALIDOS = "Cartão ou senha inválidos";
    private static final String CARTAO_BLOQUEADO = "Cartão bloqueado após 3 senhas incorretas. Procure sua agência.";

    @BeforeAll
    static void ligarSistema() {
        TerminalDeTeste.ligar();
    }

    @BeforeEach
    void terminalNaTelaInicial() {
        TerminalDeTeste.voltarAoInicio();
    }

    @Test
    @DisplayName("CT-01: cartão e senha corretos abrem o menu com o nome do cliente")
    void cartaoESenhaCorretosAbremOMenu() {
        entrar(CARTAO_MARIA, PIN_MARIA);

        esperarTexto("#saudacao", "Olá, Maria Silva");
    }

    @Test
    @DisplayName("CT-11: três senhas erradas bloqueiam o cartão, e a senha certa não entra mais")
    void tresSenhasErradasBloqueiamOCartao() {
        entrar(CARTAO_MARIA, SENHA_ERRADA);
        esperarTexto("#mensagem", CARTAO_OU_SENHA_INVALIDOS);
        entrar(CARTAO_MARIA, SENHA_ERRADA);
        esperarTexto("#mensagem", CARTAO_OU_SENHA_INVALIDOS);
        entrar(CARTAO_MARIA, SENHA_ERRADA);
        esperarTexto("#mensagem", CARTAO_BLOQUEADO);

        entrar(CARTAO_MARIA, PIN_MARIA);                // o clique limpa a mensagem até a resposta chegar
        esperarTexto("#mensagem", CARTAO_BLOQUEADO);
        esperarTela("#campoCartao");
    }
}
