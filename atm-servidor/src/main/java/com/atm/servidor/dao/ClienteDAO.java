package com.atm.servidor.dao;

import com.atm.servidor.model.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ClienteDAO {

    public Optional<Cliente> buscaCliente(Connection conexao, String cartao) throws SQLException {
        String sql = "SELECT nome, cartao, pin FROM cliente WHERE cartao = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, cartao);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Cliente(rs.getString("nome"), rs.getString("cartao"), rs.getString("pin")));
            }
        }
    }
}
