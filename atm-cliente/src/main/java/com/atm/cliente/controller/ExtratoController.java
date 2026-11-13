package com.atm.cliente.controller;

import com.atm.cliente.model.Extrato;
import com.atm.cliente.model.TipoTransacao;
import com.atm.cliente.model.Transacao;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
    private void initialize() {
        colunaData.setCellValueFactory(c -> new SimpleStringProperty(Formatos.dataHora(c.getValue().dataHora())));
        colunaTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().tipo().getRotulo()));
        colunaValor.setCellValueFactory(c -> new SimpleStringProperty(valorComSinal(c.getValue())));
    }

    private String valorComSinal(Transacao transacao) {
        String sinal = transacao.tipo() == TipoTransacao.SAQUE ? "- " : "+ ";
        return sinal + Formatos.moeda(transacao.valor());
    }

    void exibir(Extrato extrato) {
        contaInfo.setText("Conta " + extrato.conta());
        valorSaldo.setText(Formatos.moeda(extrato.saldo()));
        tabela.getItems().setAll(extrato.transacoes());
    }

    @FXML
    private void voltar() {
        Navegador.ir("Menu.fxml");
    }
}
