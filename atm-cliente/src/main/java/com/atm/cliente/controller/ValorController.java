package com.atm.cliente.controller;

import com.atm.cliente.model.Sessao;
import com.atm.cliente.model.TipoTransacao;
import com.atm.cliente.service.ContaBancariaService;
import java.math.BigDecimal;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** Tela de digitar o valor, usada tanto pelo saque quanto pelo depósito. */
public class ValorController {

    @FXML
    private Label titulo;
    @FXML
    private Label contaInfo;
    @FXML
    private TextField campoValor;
    @FXML
    private Button botaoConfirmar;
    @FXML
    private Label mensagem;

    private TipoTransacao tipo;

    void preparar(TipoTransacao tipo) {
        this.tipo = tipo;
        titulo.setText(tipo.getRotulo());
        contaInfo.setText("Conta " + Sessao.getInstancia().getContaAtual());
    }

    @FXML
    private void valorRapido(ActionEvent evento) {
        campoValor.setText(((Button) evento.getSource()).getUserData().toString());
    }

    @FXML
    private void confirmar() {
        Optional<BigDecimal> valor = Formatos.lerValor(campoValor.getText());
        if (valor.isEmpty() || valor.get().signum() <= 0) {
            mensagem.setText("Digite um valor válido, por exemplo 50,00.");
            return;
        }

        botaoConfirmar.setDisable(true);
        mensagem.setText("");
        String conta = Sessao.getInstancia().getContaAtual();
        ContaBancariaService servico = Navegador.servico();
        Navegador.executar(() -> tipo == TipoTransacao.SAQUE
                        ? servico.saque(conta, valor.get())
                        : servico.deposito(conta, valor.get()),
                comprovante -> Navegador.ir("Comprovante.fxml",
                        (ComprovanteController tela) -> tela.exibir(comprovante)),
                erro -> {
                    botaoConfirmar.setDisable(false);
                    Navegador.mostrarErro(erro, mensagem);
                });
    }

    @FXML
    private void voltar() {
        Navegador.ir("Menu.fxml");
    }
}
