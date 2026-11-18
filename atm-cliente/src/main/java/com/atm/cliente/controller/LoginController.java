package com.atm.cliente.controller;

import com.atm.cliente.model.Sessao;
import com.atm.cliente.service.ApiException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField campoCartao;
    @FXML
    private PasswordField campoSenha;
    @FXML
    private Button botaoEntrar;
    @FXML
    private Label mensagem;

    @FXML
    private void entrar() {
        String cartao = campoCartao.getText().trim();
        String senha = campoSenha.getText();
        if (cartao.isEmpty() || senha.isEmpty()) {
            mensagem.setText("Informe o cartão e a senha.");
            return;
        }

        botaoEntrar.setDisable(true);
        mensagem.setText("");
        Navegador.executar(() -> Navegador.servico().autenticar(cartao, senha),
                login -> {
                    Sessao.getInstancia().iniciar(login);
                    Navegador.ir("Menu.fxml");
                },
                erro -> {
                    botaoEntrar.setDisable(false);
                    campoSenha.clear();
                    mensagem.setText(erro instanceof ApiException ? erro.getMessage() : "Erro inesperado. Tente novamente.");
                });
    }

    void exibirMensagem(String texto) {
        mensagem.setText(texto);
    }
}
