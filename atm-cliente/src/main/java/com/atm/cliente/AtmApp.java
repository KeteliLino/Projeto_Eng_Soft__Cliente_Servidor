package com.atm.cliente;

import com.atm.cliente.controller.Navegador;
import javafx.application.Application;
import javafx.stage.Stage;

public class AtmApp extends Application {

    @Override
    public void start(Stage palco) {
        palco.setTitle("Caixa Eletrônico");
        palco.setResizable(false);
        Navegador.iniciar(palco);
        palco.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
