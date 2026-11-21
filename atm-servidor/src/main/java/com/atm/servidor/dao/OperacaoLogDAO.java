package com.atm.servidor.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** Grava o registro de cada operação executada, inclusive as recusadas (auditoria). */
public class OperacaoLogDAO {

    public void registraOperacao(Connection conexao, String numeroConta, String operacao, BigDecimal valor,
                                 String resultado, String motivo) throws SQLException {
        String sql = "INSERT INTO operacao_log (conta_numero, operacao, valor, resultado, motivo, data_hora)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, numeroConta);
            ps.setString(2, operacao);
            ps.setBigDecimal(3, valor);
            ps.setString(4, resultado);
            ps.setString(5, motivo);
            ps.setObject(6, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            ps.executeUpdate();
        }
    }
}
