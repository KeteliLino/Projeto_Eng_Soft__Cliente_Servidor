package com.atm.servidor.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBD {

    private final String url;
    private final String usuario;
    private final String senha;

    public ConexaoBD(String url, String usuario, String senha) {
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
        try {
            // Necessário no exec:java, em que o DriverManager não enxerga o driver sozinho.
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver do MySQL não encontrado", e);
        }
    }

    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, usuario, senha);
    }
}
