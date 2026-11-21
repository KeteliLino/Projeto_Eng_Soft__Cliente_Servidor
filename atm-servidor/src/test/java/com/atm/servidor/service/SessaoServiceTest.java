package com.atm.servidor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Teste de unidade da classe SessaoService (caixa branca): Singleton e tokens, sem banco nem rede. */
@DisplayName("Unidade - SessaoService (RN-03)")
class SessaoServiceTest {

    private final SessaoService sessoes = SessaoService.getInstancia();

    @Test
    @DisplayName("O Singleton devolve sempre a mesma instância")
    void singletonDevolveSempreAMesmaInstancia() {
        assertSame(SessaoService.getInstancia(), SessaoService.getInstancia());
    }

    @Test
    @DisplayName("O token identifica o cartão até a sessão ser encerrada")
    void tokenValeAteASessaoSerEncerrada() {
        String token = sessoes.criar("1111222233334444");
        assertEquals("1111222233334444", sessoes.validar(token));

        sessoes.encerrar(token);
        ApiException erro = assertThrows(ApiException.class, () -> sessoes.validar(token));
        assertEquals(401, erro.getStatus());
    }
}
