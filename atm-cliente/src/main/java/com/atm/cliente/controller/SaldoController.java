package com.atm.cliente.controller;

import com.atm.cliente.model.Sessao;
import java.math.BigDecimal;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SaldoController {

    @FXML
    private Label contaInfo;
    @FXML
    private Label valorSaldo;

    void exibir(BigDecimal saldo) {
        contaInfo.setText("Conta " + Sessao.getInstancia().getContaAtual());
        valorSaldo.setText(Formatos.moeda(saldo));
    }

    @FXML
    private void voltar() {
        Navegador.ir("Menu.fxml");
    }
}
