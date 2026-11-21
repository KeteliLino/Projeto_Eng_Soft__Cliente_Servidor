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

    private final Sessao sessao = Sessao.getInstancia();

    @FXML
    private void initialize() {
        saudacao.setText("Olá, " + sessao.getNome());
        comboConta.getItems().setAll(sessao.getContas());
        comboConta.setValue(sessao.getContaAtual());
        comboConta.valueProperty().addListener((obs, antiga, nova) -> sessao.setContaAtual(nova));
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
        Navegador.executar(() -> Navegador.servico().saldo(sessao.getContaAtual()),
                saldo -> Navegador.ir("Saldo.fxml", (SaldoController tela) -> tela.exibir(saldo)),
                erro -> Navegador.mostrarErro(erro, mensagem));
    }

    @FXML
    private void abrirExtrato() {
        Navegador.executar(() -> Navegador.servico().extrato(sessao.getContaAtual()),
                extrato -> Navegador.ir("Extrato.fxml", (ExtratoController tela) -> tela.exibir(extrato)),
                erro -> Navegador.mostrarErro(erro, mensagem));
    }

    /** O proxy encerra a sessão local mesmo se o servidor não responder; aqui só se volta ao login. */
    @FXML
    private void sair() {
        Navegador.executar(() -> {
            Navegador.servico().sair();
            return true;
        }, ok -> Navegador.ir("Login.fxml"), erro -> Navegador.ir("Login.fxml"));
    }
}
