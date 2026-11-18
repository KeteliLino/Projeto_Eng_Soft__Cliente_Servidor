package com.atm.servidor.command;

import com.atm.servidor.model.Conta;
import com.atm.servidor.model.SaldoInsuficienteException;
import com.atm.servidor.model.TipoTransacao;
import java.math.BigDecimal;

public class SaqueCommand extends MovimentacaoCommand {

    public SaqueCommand(BigDecimal valor) {
        super(TipoTransacao.SAQUE, valor);
    }

    @Override
    protected void aplicar(Conta conta, BigDecimal valor) {
        // alt [saldo >= valor] do diagrama de sequência
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new SaldoInsuficienteException();
        }
        conta.sacar(valor);
    }
}
