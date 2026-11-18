package com.atm.servidor.command;

import com.atm.servidor.model.Conta;
import java.math.BigDecimal;
import java.sql.Connection;

public class SaldoCommand implements OperacaoCommand<BigDecimal> {

    @Override
    public String getNome() {
        return "SALDO";
    }

    @Override
    public BigDecimal executar(Conta conta, Connection conexao) {
        return conta.getSaldo();
    }
}
