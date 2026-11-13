package com.atm.cliente.service;

/** Falha ao falar com o servidor. O status é 0 quando nem houve resposta. */
public class ApiException extends Exception {

    private final int status;

    public ApiException(int status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
