package com.atm.servidor.dao;

import com.atm.servidor.model.TipoTransacao;
import com.atm.servidor.model.Transacao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransacaoDAO {

    public void registraTransacao(Connection conexao, String numeroConta, Transacao transacao) throws SQLException {
        String sql = "INSERT INTO transacao (conta_numero, tipo, valor, data_hora) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, numeroConta);
            ps.setString(2, transacao.getTipo().name());
            ps.setBigDecimal(3, transacao.getValor());
            ps.setObject(4, transacao.getDataHora());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                transacao.setId(chaves.getInt(1));
            }
        }
    }

    public List<Transacao> buscaTransacoes(Connection conexao, String numeroConta) throws SQLException {
        String sql = "SELECT id, tipo, valor, data_hora FROM transacao WHERE conta_numero = ? ORDER BY id DESC";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, numeroConta);
            try (ResultSet rs = ps.executeQuery()) {
                List<Transacao> transacoes = new ArrayList<>();
                while (rs.next()) {
                    transacoes.add(new Transacao(
                            rs.getInt("id"),
                            TipoTransacao.valueOf(rs.getString("tipo")),
                            rs.getBigDecimal("valor"),
                            rs.getObject("data_hora", LocalDateTime.class)));
                }
                return transacoes;
            }
        }
    }
}
