package com.atm.servidor.service;

/** Erro de negócio que vira uma resposta HTTP com o status e a mensagem informados. */
public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(int status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
