package com.atm.servidor.service;

import com.atm.servidor.command.OperacaoCommand;
import com.atm.servidor.dao.ClienteDAO;
import com.atm.servidor.dao.ConexaoBD;
import com.atm.servidor.dao.ContaDAO;
import com.atm.servidor.dao.OperacaoLogDAO;
import com.atm.servidor.model.Cliente;
import com.atm.servidor.model.Conta;
import com.atm.servidor.model.LoginResposta;
import com.atm.servidor.model.SaldoInsuficienteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Serviço de contas do servidor. No padrão Command, é o invocador que executa as operações. */
public class ContaService {

    private static final String CARTAO_OU_SENHA_INVALIDOS = "Cartão ou senha inválidos";
    private static final String CARTAO_BLOQUEADO =
            "Cartão bloqueado após " + Cliente.MAX_TENTATIVAS_PIN + " senhas incorretas. Procure sua agência.";
    /** Hash de um PIN qualquer, conferido quando o cartão não existe. */
    private static final String HASH_FALSO = Senhas.gerarHash("0000");

    private final ConexaoBD banco = ConexaoBD.getInstancia();
    private final SessaoService sessoes = SessaoService.getInstancia();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ContaDAO contaDAO = new ContaDAO();
    private final OperacaoLogDAO operacaoLogDAO = new OperacaoLogDAO();

    public LoginResposta autenticarCartao(String cartao, String pin) throws SQLException {
        if (cartao == null || pin == null) {
            throw new ApiException(400, "Informe o cartão e a senha");
        }
        try (Connection conexao = banco.abrir()) {
            conexao.setAutoCommit(false);
            try {
                Optional<Cliente> encontrado = clienteDAO.buscaCliente(conexao, cartao);
                if (encontrado.isEmpty()) {
                    // Gasta o mesmo tempo de um PIN errado, para o tempo de resposta não revelar quais cartões existem.
                    Senhas.confere(pin, HASH_FALSO);
                    throw new ApiException(401, CARTAO_OU_SENHA_INVALIDOS);
                }
                Cliente cliente = encontrado.get();
                // Bloqueado: nem confere o PIN, para não continuar servindo de teste de senhas.
                if (cliente.isBloqueado()) {
                    throw new ApiException(403, CARTAO_BLOQUEADO);
                }
                if (!validaPin(cliente, pin)) {
                    boolean bloqueou = cliente.registrarPinErrado();
                    clienteDAO.atualizaTentativas(conexao, cliente);
                    conexao.commit();
                    throw bloqueou
                            ? new ApiException(403, CARTAO_BLOQUEADO)
                            : new ApiException(401, CARTAO_OU_SENHA_INVALIDOS);
                }
                if (cliente.getTentativasPin() > 0) {
                    cliente.zerarTentativas();
                    clienteDAO.atualizaTentativas(conexao, cliente);
                }
                List<String> contas = contaDAO.listarPorCartao(conexao, cartao).stream().map(Conta::getNumero).toList();
                conexao.commit();
                return new LoginResposta(sessoes.criar(cartao), cliente.getNome(), contas);
            } catch (SQLException | RuntimeException e) {
                conexao.rollback();
                throw e;
            }
        }
    }

    private boolean validaPin(Cliente cliente, String pin) {
        return Senhas.confere(pin, cliente.getPin());
    }

    /**
     * Executa qualquer operação sobre uma conta do cliente, dentro de uma transação do banco:
     * ou tudo o que o comando fez é gravado, ou nada. O resultado vai para o log de operações.
     */
    public <R> R executar(String cartao, String numeroConta, OperacaoCommand<R> comando) throws SQLException {
        try (Connection conexao = banco.abrir()) {
            conexao.setAutoCommit(false);
            Conta conta = null;
            try {
                conta = exigirConta(comando.alteraSaldo()
                        ? contaDAO.buscarParaAtualizar(conexao, numeroConta, cartao)
                        : contaDAO.buscar(conexao, numeroConta, cartao));
                R resultado = comando.executar(conta, conexao);
                operacaoLogDAO.registraOperacao(conexao, numeroConta, comando.getNome(), comando.getValor(),
                        "SUCESSO", null);
                conexao.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                conexao.rollback();
                // Conta inexistente ou de outro cliente não entra no log.
                if (conta != null) {
                    registrarFalha(conexao, numeroConta, comando, e);
                }
                throw e;
            }
        }
    }

    /** Registra a operação que não deu certo, sem esconder o erro original se o próprio log falhar. */
    private void registrarFalha(Connection conexao, String numeroConta, OperacaoCommand<?> comando, Exception erro) {
        boolean recusada = erro instanceof SaldoInsuficienteException || erro instanceof ApiException;
        try {
            operacaoLogDAO.registraOperacao(conexao, numeroConta, comando.getNome(), comando.getValor(),
                    recusada ? "RECUSADA" : "ERRO", recusada ? erro.getMessage() : null);
            conexao.commit();
        } catch (SQLException falhaNoLog) {
            erro.addSuppressed(falhaNoLog);
        }
    }

    private Conta exigirConta(Optional<Conta> conta) {
        // Conta de outro cliente responde igual a conta inexistente.
        return conta.orElseThrow(() -> new ApiException(404, "Conta não encontrada"));
    }
}
