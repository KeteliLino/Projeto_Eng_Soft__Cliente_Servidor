package com.atm.servidor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SessaoServiceTest {

    private final SessaoService sessoes = SessaoService.getInstancia();

    @Test
    void singletonDevolveSempreAMesmaInstancia() {
        assertSame(SessaoService.getInstancia(), SessaoService.getInstancia());
    }

    @Test
    void tokenCriadoIdentificaOCartao() {
        String token = sessoes.criar("1111222233334444");
        assertEquals("1111222233334444", sessoes.validar(token));
    }

    @Test
    void cadaLoginGeraUmTokenDiferente() {
        assertNotEquals(sessoes.criar("1111222233334444"), sessoes.criar("1111222233334444"));
    }

    @Test
    void tokenEncerradoDeixaDeValer() {
        String token = sessoes.criar("1111222233334444");
        sessoes.encerrar(token);
        ApiException erro = assertThrows(ApiException.class, () -> sessoes.validar(token));
        assertEquals(401, erro.getStatus());
    }

    @Test
    void tokenAusenteOuInventadoEhRecusado() {
        assertThrows(ApiException.class, () -> sessoes.validar(null));
        assertThrows(ApiException.class, () -> sessoes.validar("token-inventado"));
    }
}
