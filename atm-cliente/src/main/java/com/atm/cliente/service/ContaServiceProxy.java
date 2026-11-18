package com.atm.cliente.service;

import com.atm.cliente.model.Comprovante;
import com.atm.cliente.model.Extrato;
import com.atm.cliente.model.LoginResposta;
import com.atm.cliente.model.Sessao;
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

/**
 * Proxy: representa no terminal o ContaService que roda no servidor. As telas chamam os métodos
 * como se o serviço fosse local; por baixo, o proxy:
 * <ul>
 *   <li>(proxy remoto) monta a requisição HTTP, converte o JSON e trata a falta de conexão;</li>
 *   <li>(proxy de proteção) só envia operações de conta com sessão ativa, anexa o token e
 *       encerra a sessão local quando o servidor responde que ela expirou.</li>
 * </ul>
 */
public class ContaServiceProxy implements ContaBancariaService {

    private static final Duration TEMPO_LIMITE = Duration.ofSeconds(10);
    private static final String SESSAO_EXPIRADA = "Sessão expirada. Insira o cartão novamente.";

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(TEMPO_LIMITE).build();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, tipo, ctx) -> LocalDateTime.parse(json.getAsString()))
            .create();
    private final String urlBase = lerUrlServidor();
    private final Sessao sessao = Sessao.getInstancia();

    @Override
    public LoginResposta autenticar(String cartao, String pin) throws ApiException {
        String corpo = gson.toJson(Map.of("cartao", cartao, "pin", pin));
        return gson.fromJson(enviar("POST", "/api/autenticar", corpo), LoginResposta.class);
    }

    /** Avisa o servidor e encerra a sessão local mesmo se o servidor não responder. */
    @Override
    public void sair() throws ApiException {
        try {
            if (sessao.isAtiva()) {
                enviar("POST", "/api/sair", "");
            }
        } finally {
            sessao.encerrar();
        }
    }

    @Override
    public Comprovante saque(String conta, BigDecimal valor) throws ApiException {
        return movimentar(conta, "saque", valor);
    }

    @Override
    public Comprovante deposito(String conta, BigDecimal valor) throws ApiException {
        return movimentar(conta, "deposito", valor);
    }

    private Comprovante movimentar(String conta, String operacao, BigDecimal valor) throws ApiException {
        exigirSessao();
        String corpo = gson.toJson(Map.of("valor", valor));
        return gson.fromJson(enviar("POST", caminhoConta(conta, operacao), corpo), Comprovante.class);
    }

    @Override
    public BigDecimal saldo(String conta) throws ApiException {
        exigirSessao();
        JsonObject resposta = gson.fromJson(enviar("GET", caminhoConta(conta, "saldo"), null), JsonObject.class);
        return resposta.get("saldo").getAsBigDecimal();
    }

    @Override
    public Extrato extrato(String conta) throws ApiException {
        exigirSessao();
        return gson.fromJson(enviar("GET", caminhoConta(conta, "extrato"), null), Extrato.class);
    }

    /** Controle de acesso: sem sessão, a operação nem chega a ir para a rede. */
    private void exigirSessao() throws ApiException {
        if (!sessao.isAtiva()) {
            throw new ApiException(401, SESSAO_EXPIRADA);
        }
    }

    private String caminhoConta(String conta, String operacao) {
        return "/api/contas/" + URLEncoder.encode(conta, StandardCharsets.UTF_8) + "/" + operacao;
    }

    private String enviar(String metodo, String caminho, String corpo) throws ApiException {
        HttpRequest.Builder pedido = HttpRequest.newBuilder(URI.create(urlBase + caminho)).timeout(TEMPO_LIMITE);
        if (sessao.isAtiva()) {
            pedido.header("Authorization", "Bearer " + sessao.getToken());
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

        if (resposta.statusCode() == 401 && sessao.isAtiva()) {
            sessao.encerrar();
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
        try (InputStream entrada = ContaServiceProxy.class.getResourceAsStream("/cliente.properties")) {
            config.load(entrada);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return config.getProperty("servidor.url");
    }
}
