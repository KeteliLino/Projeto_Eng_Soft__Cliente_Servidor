package com.atm.servidor;

import com.atm.servidor.controller.ApiController;
import com.atm.servidor.dao.ConexaoBD;
import com.atm.servidor.service.ContaService;
import com.atm.servidor.service.SessaoService;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Executors;

public class ServidorMain {

    private static final Path CONFIG_LOCAL = Path.of("servidor.local.properties");

    public static void main(String[] args) throws IOException {
        Properties config = carregarConfiguracao();
        ConexaoBD banco = new ConexaoBD(config.getProperty("db.url"), config.getProperty("db.usuario"),
                config.getProperty("db.senha"));

        try (Connection conexao = banco.abrir()) {
            System.out.println("Conectado ao MySQL: " + conexao.getCatalog());
        } catch (SQLException e) {
            System.err.println("Falha ao conectar ao MySQL: " + e.getMessage());
            System.err.println("Confira se banco/schema.sql foi executado e a senha em " + CONFIG_LOCAL.toAbsolutePath());
            System.exit(1);
        }

        SessaoService sessoes = new SessaoService();
        ContaService contaService = new ContaService(banco, sessoes);

        String host = config.getProperty("servidor.host");
        int porta = Integer.parseInt(config.getProperty("servidor.porta"));
        HttpServer servidor = HttpServer.create(new InetSocketAddress(host, porta), 0);
        servidor.createContext("/api/", new ApiController(contaService, sessoes));
        servidor.setExecutor(Executors.newFixedThreadPool(10));
        servidor.start();
        System.out.println("Servidor ATM em http://" + host + ":" + porta);
    }

    private static Properties carregarConfiguracao() throws IOException {
        Properties config = new Properties();
        try (InputStream padrao = ServidorMain.class.getResourceAsStream("/servidor.properties")) {
            config.load(padrao);
        }
        if (Files.exists(CONFIG_LOCAL)) {
            try (Reader local = Files.newBufferedReader(CONFIG_LOCAL)) {
                config.load(local);
            }
        }
        return config;
    }
}
