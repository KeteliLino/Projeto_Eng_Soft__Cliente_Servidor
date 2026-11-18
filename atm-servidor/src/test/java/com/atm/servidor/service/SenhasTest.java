package com.atm.servidor.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SenhasTest {

    @Test
    void pinCorretoConfereComOHash() {
        assertTrue(Senhas.confere("1234", Senhas.gerarHash("1234")));
    }

    @Test
    void pinErradoNaoConfere() {
        assertFalse(Senhas.confere("4321", Senhas.gerarHash("1234")));
    }

    @Test
    void mesmoPinGeraHashesDiferentesPorCausaDoSalt() {
        assertNotEquals(Senhas.gerarHash("1234"), Senhas.gerarHash("1234"));
    }

    @Test
    void hashNaoContemOPin() {
        assertFalse(Senhas.gerarHash("1234").contains("1234"));
    }
}
