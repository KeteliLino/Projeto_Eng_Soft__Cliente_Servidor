package com.atm.servidor.command;

import com.atm.servidor.dao.ContaDAO;
import com.atm.servidor.dao.TransacaoDAO;
import com.atm.servidor.model.Comprovante;
import com.atm.servidor.model.Conta;
import com.atm.servidor.model.TipoTransacao;
import com.atm.servidor.model.Transacao;
import com.atm.servidor.service.ApiException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** Passos comuns a saque e depósito: alterar o saldo, gravá-lo, registrar a transação e gerar o comprovante. */
public abstract class MovimentacaoCommand implements OperacaoCommand<Comprovante> {

    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("1000000");

    private final TipoTransacao tipo;
    private final BigDecimal valor;
    private final ContaDAO contaDAO = new ContaDAO();
    private final TransacaoDAO transacaoDAO = new TransacaoDAO();

    protected MovimentacaoCommand(TipoTransacao tipo, BigDecimal valor) {
        validarValor(valor);
        this.tipo = tipo;
        this.valor = valor;
    }

    /** O que muda no saldo: sacar ou depositar. */
    protected abstract void aplicar(Conta conta, BigDecimal valor);

    @Override
    public Comprovante executar(Conta conta, Connection conexao) throws SQLException {
        aplicar(conta, valor);
        contaDAO.atualizaSaldo(conexao, conta);

        LocalDateTime agora = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Transacao transacao = new Transacao(tipo, valor, agora);
        transacaoDAO.registraTransacao(conexao, conta.getNumero(), transacao);

        return new Comprovante(transacao.getId(), conta.getNumero(), tipo, valor, conta.getSaldo(), agora);
    }

    @Override
    public String getNome() {
        return tipo.name();
    }

    @Override
    public BigDecimal getValor() {
        return valor;
    }

    @Override
    public boolean alteraSaldo() {
        return true;
    }

    /** Valor inválido é recusado antes de virar comando, ainda sem tocar no banco. */
    private static void validarValor(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new ApiException(400, "O valor deve ser maior que zero");
        }
        if (valor.compareTo(VALOR_MAXIMO) > 0) {
            throw new ApiException(400, "O valor excede o limite por operação");
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new ApiException(400, "O valor deve ter no máximo duas casas decimais");
        }
    }
}
