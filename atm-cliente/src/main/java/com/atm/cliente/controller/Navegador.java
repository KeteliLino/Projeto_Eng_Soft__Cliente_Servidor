package com.atm.cliente.controller;

import com.atm.cliente.model.Sessao;
import com.atm.cliente.service.ApiException;
import com.atm.cliente.service.AtmApi;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/** Troca as telas da janela e executa as chamadas ao servidor sem travar a interface. */
public final class Navegador {

    private static final String PASTA_TELAS = "/com/atm/cliente/view/";
    private static final AtmApi API = new AtmApi();

    private static Stage palco;

    private Navegador() {
    }

    public static void iniciar(Stage palcoPrincipal) {
        palco = palcoPrincipal;
        Parent login = carregar("Login.fxml", null);
        palco.setScene(new Scene(login));
    }

    static AtmApi api() {
        return API;
    }

    static void ir(String fxml) {
        ir(fxml, null);
    }

    /** Abre a tela e entrega o controller dela para receber os dados que vai exibir. */
    static <C> void ir(String fxml, Consumer<C> preparar) {
        palco.getScene().setRoot(carregar(fxml, preparar));
    }

    private static <C> Parent carregar(String fxml, Consumer<C> preparar) {
        try {
            FXMLLoader loader = new FXMLLoader(Navegador.class.getResource(PASTA_TELAS + fxml));
            Parent raiz = loader.load();
            if (preparar != null) {
                preparar.accept(loader.getController());
            }
            return raiz;
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível abrir a tela " + fxml, e);
        }
    }

    static <T> void executar(Callable<T> trabalho, Consumer<T> sucesso, Consumer<Exception> falha) {
        Task<T> tarefa = new Task<>() {
            @Override
            protected T call() throws Exception {
                return trabalho.call();
            }
        };
        tarefa.setOnSucceeded(e -> sucesso.accept(tarefa.getValue()));
        tarefa.setOnFailed(e -> falha.accept((Exception) tarefa.getException()));

        Thread linha = new Thread(tarefa, "atm-api");
        linha.setDaemon(true);
        linha.start();
    }

    /** Mostra o erro na tela atual, ou volta ao login se a sessão acabou no servidor. */
    static void mostrarErro(Exception erro, Label rotulo) {
        if (erro instanceof ApiException api && api.getStatus() == 401) {
            Sessao.encerrar();
            ir("Login.fxml", (LoginController login) -> login.exibirMensagem(erro.getMessage()));
            return;
        }
        rotulo.setText(erro instanceof ApiException ? erro.getMessage() : "Erro inesperado. Tente novamente.");
    }
}
