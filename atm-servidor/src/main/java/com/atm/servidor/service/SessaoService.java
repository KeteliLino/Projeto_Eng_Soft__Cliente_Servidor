package com.atm.servidor.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton: guarda em memória os tokens de quem está autenticado no terminal. Precisa ser uma
 * instância única, senão o token criado no login não seria encontrado nas operações seguintes.
 */
public final class SessaoService {

    private static final Duration VALIDADE = Duration.ofMinutes(10);

    private static SessaoService instancia;

    private record Sessao(String cartao, Instant expiraEm) {
    }

    private final Map<String, Sessao> sessoes = new ConcurrentHashMap<>();

    private SessaoService() {
    }

    public static synchronized SessaoService getInstancia() {
        if (instancia == null) {
            instancia = new SessaoService();
        }
        return instancia;
    }

    public String criar(String cartao) {
        Instant agora = Instant.now();
        sessoes.values().removeIf(s -> s.expiraEm().isBefore(agora));
        String token = UUID.randomUUID().toString();
        sessoes.put(token, new Sessao(cartao, agora.plus(VALIDADE)));
        return token;
    }

    /**
     * Devolve o cartão dono do token e renova a validade da sessão. A renovação é atômica, para um
     * logout simultâneo não ser desfeito; sessão vencida é removida.
     */
    public String validar(String token) {
        Instant agora = Instant.now();
        Sessao renovada = token == null ? null : sessoes.computeIfPresent(token, (chave, sessao) ->
                sessao.expiraEm().isBefore(agora) ? null : new Sessao(sessao.cartao(), agora.plus(VALIDADE)));
        if (renovada == null) {
            throw new ApiException(401, "Sessão expirada. Insira o cartão novamente.");
        }
        return renovada.cartao();
    }

    public void encerrar(String token) {
        if (token != null) {
            sessoes.remove(token);
        }
    }
}
