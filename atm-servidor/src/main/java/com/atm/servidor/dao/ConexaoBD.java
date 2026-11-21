package com.atm.servidor.dao;

import com.atm.servidor.Configuracao;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Singleton: um único ponto de acesso ao MySQL, com a mesma configuração para todos os DAOs. */
public final class ConexaoBD {

    private static ConexaoBD instancia;

    private final String url;
    private final String usuario;
    private final String senha;

    private ConexaoBD() {
        Configuracao config = Configuracao.getInstancia();
        this.url = config.get("db.url");
        this.usuario = config.get("db.usuario");
        this.senha = config.get("db.senha");
        try {
            // Necessário no exec:java, em que o DriverManager não enxerga o driver sozinho.
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver do MySQL não encontrado", e);
        }
    }

    public static synchronized ConexaoBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexaoBD();
        }
        return instancia;
    }

    /** Cada chamada abre uma conexão nova, que quem chamou deve fechar. */
    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, usuario, senha);
    }
}
