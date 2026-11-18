package com.atm.servidor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ClienteTest {

    private final Cliente cliente = new Cliente("Maria", "1111222233334444", "hash", 0);

    @Test
    void cartaoBloqueiaNaTerceiraSenhaErrada() {
        assertFalse(cliente.registrarPinErrado());
        assertFalse(cliente.registrarPinErrado());
        assertTrue(cliente.registrarPinErrado());
        assertTrue(cliente.isBloqueado());
    }

    @Test
    void acessoCorretoZeraAsTentativas() {
        cliente.registrarPinErrado();
        cliente.registrarPinErrado();
        cliente.zerarTentativas();
        assertEquals(0, cliente.getTentativasPin());
        assertFalse(cliente.registrarPinErrado());
    }

    @Test
    void clienteJaBloqueadoNoBancoContinuaBloqueado() {
        assertTrue(new Cliente("Maria", "1111222233334444", "hash", Cliente.MAX_TENTATIVAS_PIN).isBloqueado());
    }
}
