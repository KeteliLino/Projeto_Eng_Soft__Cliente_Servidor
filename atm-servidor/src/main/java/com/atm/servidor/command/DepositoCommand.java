package com.atm.servidor.command;

import com.atm.servidor.model.Conta;
import com.atm.servidor.model.TipoTransacao;
import java.math.BigDecimal;

public class DepositoCommand extends MovimentacaoCommand {

    public DepositoCommand(BigDecimal valor) {
        super(TipoTransacao.DEPOSITO, valor);
    }

    @Override
    protected void aplicar(Conta conta, BigDecimal valor) {
        conta.depositar(valor);
    }
}
