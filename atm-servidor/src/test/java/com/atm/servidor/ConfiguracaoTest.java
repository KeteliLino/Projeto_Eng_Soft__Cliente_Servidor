package com.atm.servidor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Teste escrito antes da implementação (TDD): os testes de integração e de sistema precisam apontar
 * o servidor para um banco de teste sem mexer nos arquivos .properties.
 */
class ConfiguracaoTest {

    @AfterEach
    void limpar() {
        System.clearProperty("servidor.porta");
    }

    @Test
    void semPropriedadeDoSistemaUsaOValorDoArquivo() {
        assertEquals("8080", Configuracao.getInstancia().get("servidor.porta"));
    }

    @Test
    void propriedadeDoSistemaTemPrioridadeSobreOArquivo() {
        System.setProperty("servidor.porta", "9999");
        assertEquals("9999", Configuracao.getInstancia().get("servidor.porta"));
    }
}
