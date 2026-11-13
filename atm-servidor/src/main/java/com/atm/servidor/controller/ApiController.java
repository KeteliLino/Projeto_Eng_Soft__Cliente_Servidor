package com.atm.servidor.controller;

import com.atm.servidor.model.SaldoInsuficienteException;
import com.atm.servidor.service.ApiException;
import com.atm.servidor.service.ContaService;
import com.atm.servidor.service.SessaoService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Recebe as requisições HTTP do terminal, chama o ContaService e devolve JSON. */
public class ApiController implements HttpHandler {

    private static final Pattern ROTA_CONTA = Pattern.compile("^/api/contas/([^/]+)/(saldo|saque|deposito|extrato)$");
    private static final int TAMANHO_MAXIMO_CORPO = 64 * 1024;

    private record LoginPedido(String cartao, String pin) {
    }

    private record ValorPedido(BigDecimal valor) {
    }

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (data, tipo, ctx) -> new JsonPrimitive(data.toString()))
            .create();
    private final ContaService contaService;
    private final SessaoService sessoes;

    public ApiController(ContaService contaService, SessaoService sessoes) {
        this.contaService = contaService;
        this.sessoes = sessoes;
    }

    @Override
    public void handle(HttpExchange troca) throws IOException {
        try (troca) {
            int status = 200;
            Object corpo;
            try {
                corpo = rotear(troca);
            } catch (ApiException e) {
                status = e.getStatus();
                corpo = Map.of("erro", e.getMessage());
            } catch (SaldoInsuficienteException e) {
                status = 409;
                corpo = Map.of("erro", e.getMessage());
            } catch (JsonParseException e) {
                status = 400;
                corpo = Map.of("erro", "Requisição inválida");
            } catch (SQLException | RuntimeException e) {
                e.printStackTrace();
                status = 500;
                corpo = Map.of("erro", "Erro interno no servidor");
            }
            responder(troca, status, corpo);
        }
    }

    private Object rotear(HttpExchange troca) throws IOException, SQLException {
        String metodo = troca.getRequestMethod();
        String caminho = troca.getRequestURI().getPath();

        if (caminho.equals("/api/autenticar")) {
            exigirMetodo(metodo, "POST");
            LoginPedido pedido = lerCorpo(troca, LoginPedido.class);
            return contaService.autenticarCartao(pedido.cartao(), pedido.pin());
        }
        if (caminho.equals("/api/sair")) {
            exigirMetodo(metodo, "POST");
            sessoes.encerrar(lerToken(troca));
            return Map.of("mensagem", "Sessão encerrada");
        }

        Matcher rota = ROTA_CONTA.matcher(caminho);
        if (!rota.matches()) {
            throw new ApiException(404, "Rota não encontrada");
        }
        String numeroConta = rota.group(1);
        String operacao = rota.group(2);
        exigirMetodo(metodo, operacao.equals("saque") || operacao.equals("deposito") ? "POST" : "GET");
        String cartao = sessoes.validar(lerToken(troca));

        return switch (operacao) {
            case "saque" -> contaService.saque(cartao, numeroConta, lerCorpo(troca, ValorPedido.class).valor());
            case "deposito" -> contaService.deposito(cartao, numeroConta, lerCorpo(troca, ValorPedido.class).valor());
            case "saldo" -> Map.of("saldo", contaService.saldo(cartao, numeroConta));
            default -> contaService.extrato(cartao, numeroConta);
        };
    }

    private void exigirMetodo(String metodo, String esperado) {
        if (!metodo.equals(esperado)) {
            throw new ApiException(405, "Método não permitido");
        }
    }

    private String lerToken(HttpExchange troca) {
        String cabecalho = troca.getRequestHeaders().getFirst("Authorization");
        if (cabecalho == null || !cabecalho.startsWith("Bearer ")) {
            return null;
        }
        return cabecalho.substring("Bearer ".length());
    }

    private <T> T lerCorpo(HttpExchange troca, Class<T> tipo) throws IOException {
        try (InputStream entrada = troca.getRequestBody()) {
            String json = new String(entrada.readNBytes(TAMANHO_MAXIMO_CORPO), StandardCharsets.UTF_8);
            T corpo = gson.fromJson(json, tipo);
            if (corpo == null) {
                throw new JsonParseException("Corpo vazio");
            }
            return corpo;
        }
    }

    private void responder(HttpExchange troca, int status, Object corpo) throws IOException {
        byte[] bytes = gson.toJson(corpo).getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        troca.sendResponseHeaders(status, bytes.length);
        troca.getResponseBody().write(bytes);
    }
}
