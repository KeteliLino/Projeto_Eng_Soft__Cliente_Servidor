package com.atm.cliente.service;

import com.atm.cliente.model.Comprovante;
import com.atm.cliente.model.Extrato;
import com.atm.cliente.model.LoginResposta;
import java.math.BigDecimal;

/**
 * Proxy: a interface comum entre as telas do terminal e o serviço de contas. O serviço real
 * (ContaService) roda no servidor; no terminal, quem a implementa é o {@link ContaServiceProxy}.
 */
public interface ContaBancariaService {

    LoginResposta autenticar(String cartao, String pin) throws ApiException;

    void sair() throws ApiException;

    Comprovante saque(String conta, BigDecimal valor) throws ApiException;

    Comprovante deposito(String conta, BigDecimal valor) throws ApiException;

    BigDecimal saldo(String conta) throws ApiException;

    Extrato extrato(String conta) throws ApiException;
}
