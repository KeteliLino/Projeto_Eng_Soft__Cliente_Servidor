package com.atm.servidor.integracao;

import static com.atm.servidor.apoio.BancoDeTeste.CARTAO_MARIA;
import static com.atm.servidor.apoio.BancoDeTeste.CONTA_MARIA;
import static com.atm.servidor.apoio.BancoDeTeste.PIN_MARIA;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.atm.servidor.apoio.BancoDeTeste;
import com.atm.servidor.controller.ApiController;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Teste de integração da classe SessaoService (caixa cinza): SessaoService + ApiController + ContaService
 * + MySQL, com requisições HTTP de verdade. Confere que o token criado no login é o que libera as rotas.
 */
@DisplayName("Integração - SessaoService (RN-03, RNF-07)")
class SessaoIntegracaoIT {

    private static HttpServer servidor;
    private static String endereco;
    private final HttpClient http = HttpClient.newHttpClient();

    @BeforeAll
    static void subirServidor() throws IOException {
        BancoDeTeste.configurar();
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);   // porta livre qualquer
        servidor.createContext("/api/", new ApiController());
        servidor.start();
        endereco = "http://127.0.0.1:" + servidor.getAddress().getPort();
    }

    @AfterAll
    static void desligarServidor() {
        servidor.stop(0);
    }

    @BeforeEach
    void prepararBanco() {
        BancoDeTeste.recriar();
    }

    @Test
    @DisplayName("Sem token, a operação é recusada (401)")
    void semTokenRecusa() throws Exception {
        assertEquals(401, consultarSaldo(null, CONTA_MARIA).statusCode());
    }

    @Test
    @DisplayName("O token do login libera as operações até o cliente sair; depois vale 401")
    void tokenDoLoginValeAteSair() throws Exception {
        String token = entrar();
        assertEquals(200, consultarSaldo(token, CONTA_MARIA).statusCode());

        HttpResponse<String> sair = enviar(HttpRequest.newBuilder(URI.create(endereco + "/api/sair"))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody()));

        assertEquals(200, sair.statusCode());
        assertEquals(401, consultarSaldo(token, CONTA_MARIA).statusCode());
    }

    private String entrar() throws Exception {
        String corpo = "{\"cartao\":\"" + CARTAO_MARIA + "\",\"pin\":\"" + PIN_MARIA + "\"}";
        HttpResponse<String> resposta = enviar(HttpRequest.newBuilder(URI.create(endereco + "/api/autenticar"))
                .POST(HttpRequest.BodyPublishers.ofString(corpo)));
        assertEquals(200, resposta.statusCode());
        return JsonParser.parseString(resposta.body()).getAsJsonObject().get("token").getAsString();
    }

    private HttpResponse<String> consultarSaldo(String token, String conta) throws Exception {
        HttpRequest.Builder pedido = HttpRequest.newBuilder(URI.create(endereco + "/api/contas/" + conta + "/saldo")).GET();
        if (token != null) {
            pedido.header("Authorization", "Bearer " + token);
        }
        return enviar(pedido);
    }

    private HttpResponse<String> enviar(HttpRequest.Builder pedido) throws Exception {
        return http.send(pedido.build(), HttpResponse.BodyHandlers.ofString());
    }
}
