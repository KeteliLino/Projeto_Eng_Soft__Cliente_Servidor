package com.atm.cliente.controller;

import com.atm.cliente.model.Sessao;
import com.atm.cliente.model.TipoTransacao;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class MenuController {

    @FXML
    private Label saudacao;
    @FXML
    private ComboBox<String> comboConta;
    @FXML
    private Label mensagem;

    @FXML
    private void initialize() {
        saudacao.setText("Olá, " + Sessao.getNome());
        comboConta.getItems().setAll(Sessao.getContas());
        comboConta.setValue(Sessao.getContaAtual());
        comboConta.valueProperty().addListener((obs, antiga, nova) -> Sessao.setContaAtual(nova));
    }

    @FXML
    private void abrirSaque() {
        abrirValor(TipoTransacao.SAQUE);
    }

    @FXML
    private void abrirDeposito() {
        abrirValor(TipoTransacao.DEPOSITO);
    }

    private void abrirValor(TipoTransacao tipo) {
        Navegador.ir("Valor.fxml", (ValorController valor) -> valor.preparar(tipo));
    }

    @FXML
    private void abrirSaldo() {
        Navegador.executar(() -> Navegador.api().saldo(Sessao.getContaAtual()),
                saldo -> Navegador.ir("Saldo.fxml", (SaldoController tela) -> tela.exibir(saldo)),
                erro -> Navegador.mostrarErro(erro, mensagem));
    }

    @FXML
    private void abrirExtrato() {
        Navegador.executar(() -> Navegador.api().extrato(Sessao.getContaAtual()),
                extrato -> Navegador.ir("Extrato.fxml", (ExtratoController tela) -> tela.exibir(extrato)),
                erro -> Navegador.mostrarErro(erro, mensagem));
    }

    @FXML
    private void sair() {
        Navegador.executar(() -> {
            Navegador.api().sair();
            return true;
        }, ok -> voltarAoLogin(), erro -> voltarAoLogin());
    }

    private void voltarAoLogin() {
        Sessao.encerrar();
        Navegador.ir("Login.fxml");
    }
}
