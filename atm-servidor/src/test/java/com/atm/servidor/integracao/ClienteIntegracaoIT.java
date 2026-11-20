package com.atm.servidor.integracao;

import static com.atm.servidor.apoio.BancoDeTeste.CARTAO_MARIA;
import static com.atm.servidor.apoio.BancoDeTeste.CONTA_MARIA;
import static com.atm.servidor.apoio.BancoDeTeste.CONTA_MARIA_2;
import static com.atm.servidor.apoio.BancoDeTeste.PIN_MARIA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.atm.servidor.apoio.BancoDeTeste;
import com.atm.servidor.model.LoginResposta;
import com.atm.servidor.service.ApiException;
import com.atm.servidor.service.ContaService;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Teste de integração da classe Cliente (caixa cinza): Cliente + ContaService + ClienteDAO + MySQL.
 * O contador de PINs errados, testado na unidade (ClienteTest), precisa ficar gravado no banco
 * para o bloqueio valer entre um login e outro.
 */
@DisplayName("Integração - Cliente (RF-01, RN-06, RN-07)")
class ClienteIntegracaoIT {

    private static final String PIN_ERRADO = "9999";

    private ContaService contaService;

    @BeforeAll
    static void apontarParaOBancoDeTeste() {
        BancoDeTeste.configurar();
    }

    @BeforeEach
    void prepararBanco() {
        BancoDeTeste.recriar();
        contaService = new ContaService();
    }

    @Test
    @DisplayName("Cartão e PIN corretos devolvem token, nome e as contas do cliente")
    void loginCorretoDevolveTokenNomeEContas() throws Exception {
        LoginResposta login = contaService.autenticarCartao(CARTAO_MARIA, PIN_MARIA);

        assertNotNull(login.token());
        assertEquals("Maria Silva", login.nome());
        assertEquals(List.of(CONTA_MARIA, CONTA_MARIA_2), login.contas());
    }

    @Test
    @DisplayName("Terceiro PIN errado bloqueia o cartão no banco (403) e nem o PIN correto entra depois")
    void terceiroPinErradoBloqueiaOCartao() {
        assertEquals(401, statusDoLogin(PIN_ERRADO));
        assertEquals(401, statusDoLogin(PIN_ERRADO));
        assertEquals(403, statusDoLogin(PIN_ERRADO));

        assertEquals(3, BancoDeTeste.tentativasPin(CARTAO_MARIA));
        assertEquals(403, statusDoLogin(PIN_MARIA));
    }

    private int statusDoLogin(String pin) {
        return assertThrows(ApiException.class, () -> contaService.autenticarCartao(CARTAO_MARIA, pin)).getStatus();
    }
}
