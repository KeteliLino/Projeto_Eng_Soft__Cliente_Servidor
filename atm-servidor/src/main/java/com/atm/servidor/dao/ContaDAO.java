package com.atm.servidor.dao;

import com.atm.servidor.model.Conta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContaDAO {

    public List<Conta> listarPorCartao(Connection conexao, String cartao) throws SQLException {
        String sql = "SELECT numero, saldo FROM conta WHERE cliente_cartao = ? ORDER BY numero";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, cartao);
            try (ResultSet rs = ps.executeQuery()) {
                List<Conta> contas = new ArrayList<>();
                while (rs.next()) {
                    contas.add(new Conta(rs.getString("numero"), rs.getBigDecimal("saldo")));
                }
                return contas;
            }
        }
    }

    public Optional<Conta> buscar(Connection conexao, String numero, String cartao) throws SQLException {
        return buscar(conexao, numero, cartao, "");
    }

    /** Trava a linha até o fim da transação, para dois saques simultâneos não usarem o mesmo saldo. */
    public Optional<Conta> buscarParaAtualizar(Connection conexao, String numero, String cartao) throws SQLException {
        return buscar(conexao, numero, cartao, " FOR UPDATE");
    }

    private Optional<Conta> buscar(Connection conexao, String numero, String cartao, String sufixo) throws SQLException {
        String sql = "SELECT numero, saldo FROM conta WHERE numero = ? AND cliente_cartao = ?" + sufixo;
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, numero);
            ps.setString(2, cartao);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Conta(rs.getString("numero"), rs.getBigDecimal("saldo")));
            }
        }
    }

    public void atualizaSaldo(Connection conexao, Conta conta) throws SQLException {
        String sql = "UPDATE conta SET saldo = ? WHERE numero = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setBigDecimal(1, conta.getSaldo());
            ps.setString(2, conta.getNumero());
            ps.executeUpdate();
        }
    }
}
