package com.atm.servidor.service;

import com.atm.servidor.dao.ClienteDAO;
import com.atm.servidor.dao.ConexaoBD;
import com.atm.servidor.dao.ContaDAO;
import com.atm.servidor.dao.TransacaoDAO;
import com.atm.servidor.model.Cliente;
import com.atm.servidor.model.Comprovante;
import com.atm.servidor.model.Conta;
import com.atm.servidor.model.Extrato;
import com.atm.servidor.model.LoginResposta;
import com.atm.servidor.model.SaldoInsuficienteException;
import com.atm.servidor.model.TipoTransacao;
import com.atm.servidor.model.Transacao;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class ContaService {

    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("1000000");

    private final ConexaoBD banco;
    private final SessaoService sessoes;
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ContaDAO contaDAO = new ContaDAO();
    private final TransacaoDAO transacaoDAO = new TransacaoDAO();

    public ContaService(ConexaoBD banco, SessaoService sessoes) {
        this.banco = banco;
        this.sessoes = sessoes;
    }

    public LoginResposta autenticarCartao(String cartao, String pin) throws SQLException {
        if (cartao == null || pin == null) {
            throw new ApiException(400, "Informe o cartão e a senha");
        }
        try (Connection conexao = banco.abrir()) {
            Optional<Cliente> cliente = clienteDAO.buscaCliente(conexao, cartao);
            if (cliente.isEmpty() || !validaPin(cliente.get(), pin)) {
                throw new ApiException(401, "Cartão ou senha inválidos");
            }
            List<String> contas = contaDAO.listarPorCartao(conexao, cartao).stream().map(Conta::getNumero).toList();
            return new LoginResposta(sessoes.criar(cartao), cliente.get().getNome(), contas);
        }
    }

    private boolean validaPin(Cliente cliente, String pin) {
        return Senhas.confere(pin, cliente.getPin());
    }

    public Comprovante saque(String cartao, String numeroConta, BigDecimal valor) throws SQLException {
        return movimentar(cartao, numeroConta, TipoTransacao.SAQUE, valor);
    }

    public Comprovante deposito(String cartao, String numeroConta, BigDecimal valor) throws SQLException {
        return movimentar(cartao, numeroConta, TipoTransacao.DEPOSITO, valor);
    }

    public BigDecimal saldo(String cartao, String numeroConta) throws SQLException {
        try (Connection conexao = banco.abrir()) {
            return exigirConta(contaDAO.buscar(conexao, numeroConta, cartao)).getSaldo();
        }
    }

    public Extrato extrato(String cartao, String numeroConta) throws SQLException {
        try (Connection conexao = banco.abrir()) {
            Conta conta = exigirConta(contaDAO.buscar(conexao, numeroConta, cartao));
            BigDecimal saldo = conta.getSaldo();
            List<Transacao> transacoes = transacaoDAO.buscaTransacoes(conexao, numeroConta);
            return gerarExtrato(conta, saldo, transacoes);
        }
    }

    private Extrato gerarExtrato(Conta conta, BigDecimal saldo, List<Transacao> transacoes) {
        conta.getTransacoes().addAll(transacoes);
        return new Extrato(conta.getNumero(), saldo, conta.getTransacoes());
    }

    /** Atualiza o saldo e registra a transação juntos: ou as duas coisas acontecem, ou nenhuma. */
    private Comprovante movimentar(String cartao, String numeroConta, TipoTransacao tipo, BigDecimal valor)
            throws SQLException {
        validarValor(valor);
        try (Connection conexao = banco.abrir()) {
            conexao.setAutoCommit(false);
            try {
                Conta conta = exigirConta(contaDAO.buscarParaAtualizar(conexao, numeroConta, cartao));
                if (tipo == TipoTransacao.SAQUE) {
                    if (conta.getSaldo().compareTo(valor) < 0) {
                        throw new SaldoInsuficienteException();
                    }
                    conta.sacar(valor);
                } else {
                    conta.depositar(valor);
                }
                contaDAO.atualizaSaldo(conexao, conta);

                LocalDateTime agora = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
                Transacao transacao = new Transacao(tipo, valor, agora);
                transacaoDAO.registraTransacao(conexao, numeroConta, transacao);
                conexao.commit();

                return new Comprovante(transacao.getId(), numeroConta, tipo, valor, conta.getSaldo(), agora);
            } catch (SQLException | RuntimeException e) {
                conexao.rollback();
                throw e;
            }
        }
    }

    private Conta exigirConta(Optional<Conta> conta) {
        // Conta de outro cliente responde igual a conta inexistente.
        return conta.orElseThrow(() -> new ApiException(404, "Conta não encontrada"));
    }

    private void validarValor(BigDecimal valor) {
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
