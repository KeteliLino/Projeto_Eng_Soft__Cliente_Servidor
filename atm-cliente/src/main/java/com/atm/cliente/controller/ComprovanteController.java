package com.atm.cliente.controller;

import com.atm.cliente.model.Comprovante;
import com.atm.cliente.model.TipoTransacao;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ComprovanteController {

    @FXML
    private Label aviso;
    @FXML
    private Label operacao;
    @FXML
    private Label conta;
    @FXML
    private Label valor;
    @FXML
    private Label saldo;
    @FXML
    private Label dataHora;
    @FXML
    private Label codigo;

    void exibir(Comprovante comprovante) {
        aviso.setText(comprovante.tipo() == TipoTransacao.SAQUE
                ? "Retire suas cédulas e o comprovante."
                : "Depósito realizado com sucesso.");
        operacao.setText(comprovante.tipo().getRotulo());
        conta.setText(comprovante.conta());
        valor.setText(Formatos.moeda(comprovante.valor()));
        saldo.setText(Formatos.moeda(comprovante.saldo()));
        dataHora.setText(Formatos.dataHora(comprovante.dataHora()));
        codigo.setText(String.valueOf(comprovante.transacaoId()));
    }

    @FXML
    private void voltar() {
        Navegador.ir("Menu.fxml");
    }
}
