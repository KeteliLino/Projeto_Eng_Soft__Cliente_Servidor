package com.atm.servidor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Singleton: a configuração do servidor é lida uma única vez e compartilhada por todo o sistema.
 * Valores de servidor.properties, sobrescritos pelos de servidor.local.properties, se existir,
 * e por propriedades do sistema (-Dchave=valor).
 */
public final class Configuracao {

    private static final Path CONFIG_LOCAL = Path.of("servidor.local.properties");

    private static Configuracao instancia;

    private final Properties propriedades = new Properties();

    private Configuracao() {
        try (InputStream padrao = Configuracao.class.getResourceAsStream("/servidor.properties")) {
            propriedades.load(padrao);
            if (Files.exists(CONFIG_LOCAL)) {
                try (Reader local = Files.newBufferedReader(CONFIG_LOCAL)) {
                    propriedades.load(local);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler a configuração do servidor", e);
        }
    }

    /** synchronized: o servidor atende várias requisições ao mesmo tempo, e só pode existir uma instância. */
    public static synchronized Configuracao getInstancia() {
        if (instancia == null) {
            instancia = new Configuracao();
        }
        return instancia;
    }

    /** Uma propriedade do sistema (-Dchave=valor) tem prioridade; os testes usam isso para apontar o banco de teste. */
    public String get(String chave) {
        return System.getProperty(chave, propriedades.getProperty(chave));
    }

    public Path getArquivoLocal() {
        return CONFIG_LOCAL.toAbsolutePath();
    }
}
