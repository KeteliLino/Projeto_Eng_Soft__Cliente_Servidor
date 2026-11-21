package com.atm.servidor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Teste de unidade da classe Cliente (caixa branca): o contador de PINs errados, sem banco. */
@DisplayName("Unidade - Cliente (RN-07)")
class ClienteTest {

    private final Cliente cliente = new Cliente("Maria", "1111222233334444", "hash", 0);

    @Test
    @DisplayName("O cartão bloqueia na terceira senha errada")
    void cartaoBloqueiaNaTerceiraSenhaErrada() {
        assertFalse(cliente.registrarPinErrado());
        assertFalse(cliente.registrarPinErrado());
        assertTrue(cliente.registrarPinErrado());
        assertTrue(cliente.isBloqueado());
    }

    @Test
    @DisplayName("Acertar a senha zera as tentativas")
    void acessoCorretoZeraAsTentativas() {
        cliente.registrarPinErrado();
        cliente.registrarPinErrado();
        cliente.zerarTentativas();
        assertEquals(0, cliente.getTentativasPin());
        assertFalse(cliente.isBloqueado());
    }
}
