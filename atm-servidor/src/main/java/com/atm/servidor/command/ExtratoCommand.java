package com.atm.servidor.command;

import com.atm.servidor.dao.TransacaoDAO;
import com.atm.servidor.model.Conta;
import com.atm.servidor.model.Extrato;
import com.atm.servidor.model.Transacao;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ExtratoCommand implements OperacaoCommand<Extrato> {

    private final TransacaoDAO transacaoDAO = new TransacaoDAO();

    @Override
    public String getNome() {
        return "EXTRATO";
    }

    /**
     * Saldo e transações são lidos na mesma transação do banco, para retratarem o mesmo momento:
     * o terminal calcula o saldo após cada transação a partir do saldo atual.
     */
    @Override
    public Extrato executar(Conta conta, Connection conexao) throws SQLException {
        BigDecimal saldo = conta.getSaldo();
        List<Transacao> transacoes = transacaoDAO.buscaTransacoes(conexao, conta.getNumero());
        return gerarExtrato(conta, saldo, transacoes);
    }

    private Extrato gerarExtrato(Conta conta, BigDecimal saldo, List<Transacao> transacoes) {
        conta.getTransacoes().addAll(transacoes);
        return new Extrato(conta.getNumero(), saldo, conta.getTransacoes());
    }
}
