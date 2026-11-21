package com.atm.servidor.command;

import com.atm.servidor.model.Conta;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Command: uma operação bancária transformada em objeto. O ContaService (invocador) executa
 * qualquer comando da mesma forma e registra o resultado no log de operações.
 *
 * @param <R> o que a operação devolve ao terminal
 */
public interface OperacaoCommand<R> {

    /** Nome gravado no log de operações. */
    String getNome();

    /** Valor movimentado, ou null nas consultas. */
    default BigDecimal getValor() {
        return null;
    }

    /** Operações que alteram o saldo travam a conta até o fim, para evitar dois saques simultâneos. */
    default boolean alteraSaldo() {
        return false;
    }

    /** Executa a operação dentro da transação do banco aberta pelo invocador. */
    R executar(Conta conta, Connection conexao) throws SQLException;
}
