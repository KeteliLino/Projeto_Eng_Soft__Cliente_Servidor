package com.atm.servidor.dao;

import com.atm.servidor.model.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ClienteDAO {

    /**
     * Busca o cliente travando a linha até o fim da transação, para que tentativas de PIN
     * simultâneas no mesmo cartão sejam contadas uma de cada vez.
     */
    public Optional<Cliente> buscaCliente(Connection conexao, String cartao) throws SQLException {
        String sql = "SELECT nome, cartao, pin, tentativas_pin FROM cliente WHERE cartao = ? FOR UPDATE";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, cartao);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Cliente(rs.getString("nome"), rs.getString("cartao"), rs.getString("pin"),
                        rs.getInt("tentativas_pin")));
            }
        }
    }

    public void atualizaTentativas(Connection conexao, Cliente cliente) throws SQLException {
        String sql = "UPDATE cliente SET tentativas_pin = ? WHERE cartao = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, cliente.getTentativasPin());
            ps.setString(2, cliente.getCartao());
            ps.executeUpdate();
        }
    }
}
