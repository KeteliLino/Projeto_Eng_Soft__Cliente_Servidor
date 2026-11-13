package com.atm.cliente.service;

import com.atm.cliente.model.Comprovante;
import com.atm.cliente.model.Extrato;
import com.atm.cliente.model.LoginResposta;
import com.atm.cliente.model.Sessao;
import com.atm.cliente.model.TipoTransacao;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;

/** Faz as chamadas HTTP ao servidor e converte as respostas JSON para o model. */
public class AtmApi {

    private static final Duration TEMPO_LIMITE = Duration.ofSeconds(10);

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(TEMPO_LIMITE).build();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, tipo, ctx) -> LocalDateTime.parse(json.getAsString()))
            .create();
    private final String urlBase = lerUrlServidor();

    public LoginResposta autenticar(String cartao, String pin) throws ApiException {
        String corpo = gson.toJson(Map.of("cartao", cartao, "pin", pin));
        return gson.fromJson(enviar("POST", "/api/autenticar", corpo), LoginResposta.class);
    }

    public void sair() throws ApiException {
        enviar("POST", "/api/sair", "");
    }

    public Comprovante movimentar(TipoTransacao tipo, String conta, BigDecimal valor) throws ApiException {
        String operacao = tipo == TipoTransacao.SAQUE ? "saque" : "deposito";
        String corpo = gson.toJson(Map.of("valor", valor));
        return gson.fromJson(enviar("POST", caminhoConta(conta, operacao), corpo), Comprovante.class);
    }

    public BigDecimal saldo(String conta) throws ApiException {
        JsonObject resposta = gson.fromJson(enviar("GET", caminhoConta(conta, "saldo"), null), JsonObject.class);
        return resposta.get("saldo").getAsBigDecimal();
    }

    public Extrato extrato(String conta) throws ApiException {
        return gson.fromJson(enviar("GET", caminhoConta(conta, "extrato"), null), Extrato.class);
    }

    private String caminhoConta(String conta, String operacao) {
        return "/api/contas/" + URLEncoder.encode(conta, StandardCharsets.UTF_8) + "/" + operacao;
    }

    private String enviar(String metodo, String caminho, String corpo) throws ApiException {
        HttpRequest.Builder pedido = HttpRequest.newBuilder(URI.create(urlBase + caminho)).timeout(TEMPO_LIMITE);
        if (Sessao.getToken() != null) {
            pedido.header("Authorization", "Bearer " + Sessao.getToken());
        }
        pedido.method(metodo, corpo == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(corpo));

        HttpResponse<String> resposta;
        try {
            resposta = http.send(pedido.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new ApiException(0, "Não foi possível conectar ao servidor.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(0, "Operação interrompida.");
        }

        if (resposta.statusCode() != 200) {
            throw new ApiException(resposta.statusCode(), lerMensagemDeErro(resposta.body()));
        }
        return resposta.body();
    }

    private String lerMensagemDeErro(String corpo) {
        try {
            return gson.fromJson(corpo, JsonObject.class).get("erro").getAsString();
        } catch (JsonParseException | NullPointerException | IllegalStateException e) {
            return "O servidor respondeu com um erro inesperado.";
        }
    }

    private static String lerUrlServidor() {
        Properties config = new Properties();
        try (InputStream entrada = AtmApi.class.getResourceAsStream("/cliente.properties")) {
            config.load(entrada);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return config.getProperty("servidor.url");
    }
}
