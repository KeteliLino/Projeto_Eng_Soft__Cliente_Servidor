package com.atm.servidor;

import com.atm.servidor.controller.ApiController;
import com.atm.servidor.dao.ConexaoBD;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;

public class ServidorMain {

    public static void main(String[] args) throws IOException {
        Configuracao config = Configuracao.getInstancia();

        try (Connection conexao = ConexaoBD.getInstancia().abrir()) {
            System.out.println("Conectado ao MySQL: " + conexao.getCatalog());
            // Bancos criados com uma versão antiga do schema.sql: avisa em vez de falhar no meio do uso.
            if (!existeColuna(conexao, "operacao_log", "id") || !existeColuna(conexao, "cliente", "tentativas_pin")) {
                System.err.println("Banco desatualizado: execute banco/schema.sql de novo no MySQL Workbench.");
                System.exit(1);
            }
        } catch (SQLException e) {
            System.err.println("Falha ao conectar ao MySQL: " + e.getMessage());
            System.err.println("Confira se banco/schema.sql foi executado e a senha em " + config.getArquivoLocal());
            System.exit(1);
        }

        String host = config.get("servidor.host");
        int porta = Integer.parseInt(config.get("servidor.porta"));
        HttpServer servidor = HttpServer.create(new InetSocketAddress(host, porta), 0);
        servidor.createContext("/api/", new ApiController());
        servidor.setExecutor(Executors.newFixedThreadPool(10));
        servidor.start();
        System.out.println("Servidor ATM em http://" + host + ":" + porta);
    }

    private static boolean existeColuna(Connection conexao, String tabela, String coluna) throws SQLException {
        try (ResultSet colunas = conexao.getMetaData().getColumns(conexao.getCatalog(), null, tabela, coluna)) {
            return colunas.next();
        }
    }
}
