package com.atm.cliente.model;

import java.util.List;

/** Singleton: dados de quem está usando o terminal agora, os mesmos para todas as telas. */
public final class Sessao {

    private static Sessao instancia;

    private String token;
    private String nome;
    private List<String> contas = List.of();
    private String contaAtual;

    private Sessao() {
    }

    public static synchronized Sessao getInstancia() {
        if (instancia == null) {
            instancia = new Sessao();
        }
        return instancia;
    }

    public void iniciar(LoginResposta login) {
        token = login.token();
        nome = login.nome();
        contas = login.contas();
        contaAtual = contas.isEmpty() ? null : contas.getFirst();
    }

    public void encerrar() {
        token = null;
        nome = null;
        contas = List.of();
        contaAtual = null;
    }

    public boolean isAtiva() {
        return token != null;
    }

    public String getToken() {
        return token;
    }

    public String getNome() {
        return nome;
    }

    public List<String> getContas() {
        return contas;
    }

    public String getContaAtual() {
        return contaAtual;
    }

    public void setContaAtual(String conta) {
        contaAtual = conta;
    }
}
