package com.atm.cliente.model;

import java.util.List;

/** Dados de quem está usando o terminal agora. */
public final class Sessao {

    private static String token;
    private static String nome;
    private static List<String> contas = List.of();
    private static String contaAtual;

    private Sessao() {
    }

    public static void iniciar(LoginResposta login) {
        token = login.token();
        nome = login.nome();
        contas = login.contas();
        contaAtual = contas.isEmpty() ? null : contas.getFirst();
    }

    public static void encerrar() {
        token = null;
        nome = null;
        contas = List.of();
        contaAtual = null;
    }

    public static String getToken() {
        return token;
    }

    public static String getNome() {
        return nome;
    }

    public static List<String> getContas() {
        return contas;
    }

    public static String getContaAtual() {
        return contaAtual;
    }

    public static void setContaAtual(String conta) {
        contaAtual = conta;
    }
}
