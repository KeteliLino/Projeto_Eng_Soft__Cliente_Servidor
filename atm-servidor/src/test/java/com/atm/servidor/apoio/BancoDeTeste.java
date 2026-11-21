package com.atm.servidor.apoio;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Banco usado pelos testes de integração e de sistema: caixa_eletronico_teste, criado a partir do
 * banco/schema.sql. O banco do dia a dia (caixa_eletronico) nunca é tocado pelos testes.
 *
 * Usa o mesmo MySQL e a mesma senha de servidor.local.properties. Para usar outro MySQL:
 * -Ddb.servidor=host:porta -Ddb.usuario=... -Ddb.senha=...
 */
public final class BancoDeTeste {

    public static final String NOME = "caixa_eletronico_teste";

    // Dados criados pelo schema.sql
    public static final String CARTAO_MARIA = "1111222233334444";
    public static final String PIN_MARIA = "1234";
    public static final String CONTA_MARIA = "0001-1";
    public static final String CONTA_MARIA_2 = "0001-2";

    private static String servidor;
    private static String usuario;
    private static String senha;

    private BancoDeTeste() {
    }

    /** Aponta o servidor (Configuracao/ConexaoBD) para o banco de teste. Chamar antes de criar o ContaService. */
    public static synchronized void configurar() {
        if (servidor != null) {
            return;
        }
        Properties local = lerConfiguracaoLocal();
        servidor = System.getProperty("db.servidor", "localhost:3306");
        usuario = System.getProperty("db.usuario", local.getProperty("db.usuario", "root"));
        senha = System.getProperty("db.senha", local.getProperty("db.senha", ""));

        System.setProperty("db.url", "jdbc:mysql://" + servidor + "/" + NOME);
        System.setProperty("db.usuario", usuario);
        System.setProperty("db.senha", senha);
    }

    /** Apaga e cria de novo o banco de teste, com os dados iniciais do schema.sql. */
    public static void recriar() {
        configurar();
        String script = lerSchema().replaceAll("\\bcaixa_eletronico\\b", NOME);
        String url = "jdbc:mysql://" + servidor + "/?allowMultiQueries=true";
        try (Connection conexao = DriverManager.getConnection(url, usuario, senha);
             Statement comando = conexao.createStatement()) {
            comando.execute("DROP DATABASE IF EXISTS " + NOME);
            comando.execute(script);
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível criar o banco de teste em " + servidor
                    + ". O MySQL está ligado? " + e.getMessage(), e);
        }
    }

    public static BigDecimal saldo(String conta) {
        return (BigDecimal) consultar("SELECT saldo FROM conta WHERE numero = ?", conta).getFirst()[0];
    }

    public static int quantidadeDeTransacoes(String conta) {
        return ((Number) consultar("SELECT COUNT(*) FROM transacao WHERE conta_numero = ?", conta)
                .getFirst()[0]).intValue();
    }

    /** Resultado (SUCESSO, RECUSADA ou ERRO) de cada linha do operacao_log da conta, na ordem em que foram gravadas. */
    public static List<String> resultadosDoLog(String conta) {
        List<String> resultados = new ArrayList<>();
        for (Object[] linha : consultar(
                "SELECT CONCAT(operacao, ' ', resultado) FROM operacao_log WHERE conta_numero = ? ORDER BY id", conta)) {
            resultados.add((String) linha[0]);
        }
        return resultados;
    }

    public static int tentativasPin(String cartao) {
        return ((Number) consultar("SELECT tentativas_pin FROM cliente WHERE cartao = ?", cartao)
                .getFirst()[0]).intValue();
    }

    private static List<Object[]> consultar(String sql, String parametro) {
        configurar();
        try (Connection conexao = DriverManager.getConnection(System.getProperty("db.url"), usuario, senha);
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, parametro);
            List<Object[]> linhas = new ArrayList<>();
            try (ResultSet resultado = comando.executeQuery()) {
                int colunas = resultado.getMetaData().getColumnCount();
                while (resultado.next()) {
                    Object[] linha = new Object[colunas];
                    for (int i = 0; i < colunas; i++) {
                        linha[i] = resultado.getObject(i + 1);
                    }
                    linhas.add(linha);
                }
            }
            return linhas;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    /** O Maven roda os testes na pasta do módulo (atm-servidor ou atm-cliente). */
    private static Path acharNaRaiz(String caminho) {
        for (Path base : List.of(Path.of(""), Path.of(".."), Path.of("../atm-servidor"))) {
            Path arquivo = base.resolve(caminho);
            if (Files.exists(arquivo)) {
                return arquivo;
            }
        }
        return null;
    }

    private static Properties lerConfiguracaoLocal() {
        Properties local = new Properties();
        Path arquivo = acharNaRaiz("servidor.local.properties");
        if (arquivo != null) {
            try (Reader leitor = Files.newBufferedReader(arquivo)) {
                local.load(leitor);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return local;
    }

    private static String lerSchema() {
        Path schema = acharNaRaiz("banco/schema.sql");
        if (schema == null) {
            throw new IllegalStateException("banco/schema.sql não encontrado");
        }
        try {
            return Files.readString(schema, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
