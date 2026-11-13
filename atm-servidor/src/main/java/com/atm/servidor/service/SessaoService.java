package com.atm.servidor.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Guarda em memória os tokens de quem está autenticado no terminal. */
public class SessaoService {

    private static final Duration VALIDADE = Duration.ofMinutes(10);

    private record Sessao(String cartao, Instant expiraEm) {
    }

    private final Map<String, Sessao> sessoes = new ConcurrentHashMap<>();

    public String criar(String cartao) {
        Instant agora = Instant.now();
        sessoes.values().removeIf(s -> s.expiraEm().isBefore(agora));
        String token = UUID.randomUUID().toString();
        sessoes.put(token, new Sessao(cartao, agora.plus(VALIDADE)));
        return token;
    }

    /** Devolve o cartão dono do token e renova a validade da sessão. */
    public String validar(String token) {
        Instant agora = Instant.now();
        Sessao sessao = token == null ? null : sessoes.get(token);
        if (sessao == null || sessao.expiraEm().isBefore(agora)) {
            throw new ApiException(401, "Sessão expirada. Insira o cartão novamente.");
        }
        sessoes.put(token, new Sessao(sessao.cartao(), agora.plus(VALIDADE)));
        return sessao.cartao();
    }

    public void encerrar(String token) {
        if (token != null) {
            sessoes.remove(token);
        }
    }
}
