package com.atm.cliente.controller;

import com.atm.cliente.model.Extrato;
import com.atm.cliente.model.TipoTransacao;
import com.atm.cliente.model.Transacao;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ExtratoController {

    @FXML
    private Label contaInfo;
    @FXML
    private Label valorSaldo;
    @FXML
    private TableView<Transacao> tabela;
    @FXML
    private TableColumn<Transacao, String> colunaData;
    @FXML
    private TableColumn<Transacao, String> colunaTipo;
    @FXML
    private TableColumn<Transacao, String> colunaValor;
    @FXML
    private TableColumn<Transacao, String> colunaSaldo;

    /** Saldo da conta logo após cada transação, pelo id da transação. */
    private final Map<Integer, BigDecimal> saldoApos = new HashMap<>();

    @FXML
    private void initialize() {
        colunaData.setCellValueFactory(c -> new SimpleStringProperty(Formatos.dataHora(c.getValue().dataHora())));
        colunaTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().tipo().getRotulo()));
        colunaValor.setCellValueFactory(c -> new SimpleStringProperty(valorComSinal(c.getValue())));
        colunaSaldo.setCellValueFactory(c -> new SimpleStringProperty(Formatos.moeda(saldoApos.get(c.getValue().id()))));

        colunaValor.setCellFactory(coluna -> celulaColorida(
                transacao -> transacao.tipo() == TipoTransacao.SAQUE ? "debito" : "credito"));
        colunaSaldo.setCellFactory(coluna -> celulaColorida(transacao -> "saldo-extrato"));
    }

    /** Célula que recebe uma classe CSS conforme a transação: créditos em azul, débitos em vermelho. */
    private TableCell<Transacao, String> celulaColorida(Function<Transacao, String> classeCss) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String texto, boolean vazia) {
                super.updateItem(texto, vazia);
                getStyleClass().removeAll("credito", "debito", "saldo-extrato");
                if (vazia || texto == null) {
                    setText(null);
                    return;
                }
                setText(texto);
                getStyleClass().add(classeCss.apply(getTableView().getItems().get(getIndex())));
            }
        };
    }

    private String valorComSinal(Transacao transacao) {
        String sinal = transacao.tipo() == TipoTransacao.SAQUE ? "- " : "+ ";
        return sinal + Formatos.moeda(transacao.valor());
    }

    void exibir(Extrato extrato) {
        contaInfo.setText("Conta " + extrato.conta());
        valorSaldo.setText(Formatos.moeda(extrato.saldo()));

        // As transações vêm da mais recente para a mais antiga. O saldo após a mais recente é o
        // saldo atual; para chegar ao saldo após a anterior, desfaz-se o efeito da transação.
        BigDecimal saldo = extrato.saldo();
        for (Transacao transacao : extrato.transacoes()) {
            saldoApos.put(transacao.id(), saldo);
            saldo = transacao.tipo() == TipoTransacao.SAQUE
                    ? saldo.add(transacao.valor())
                    : saldo.subtract(transacao.valor());
        }
        tabela.getItems().setAll(extrato.transacoes());
    }

    @FXML
    private void voltar() {
        Navegador.ir("Menu.fxml");
    }
}
