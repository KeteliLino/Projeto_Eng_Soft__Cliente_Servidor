package com.atm.sistema;

import static org.junit.jupiter.api.Assertions.fail;

import com.atm.cliente.AtmApp;
import com.atm.cliente.controller.Navegador;
import com.atm.cliente.model.Sessao;
import com.atm.servidor.apoio.BancoDeTeste;
import com.atm.servidor.controller.ApiController;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;

/**
 * Sistema completo para os testes de sistema: servidor (ApiController) numa porta livre, banco
 * caixa_eletronico_teste e a janela do terminal JavaFX. Os testes usam o terminal como o usuário:
 * digitam nos campos, clicam nos botões e leem o que aparece na tela.
 */
final class TerminalDeTeste {

    private static final long ESPERA_MAXIMA_MS = 15_000;

    private static Stage janela;
    private static HttpServer servidor;

    private TerminalDeTeste() {
    }

    /** Liga servidor e terminal uma única vez para todas as classes de teste de sistema. */
    static synchronized void ligar() {
        if (janela != null) {
            return;
        }
        BancoDeTeste.configurar();
        try {
            servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        servidor.createContext("/api/", new ApiController());
        servidor.start();
        // O terminal passa a falar com este servidor de teste, não com o da porta 8080.
        System.setProperty("servidor.url", "http://127.0.0.1:" + servidor.getAddress().getPort());

        CountDownLatch pronto = new CountDownLatch(1);
        Platform.setImplicitExit(false);
        Platform.startup(pronto::countDown);
        aguardar(pronto);
        naTela(() -> {
            janela = new Stage();
            new AtmApp().start(janela);
            return null;
        });
    }

    /** Banco com os dados iniciais e terminal na tela de cartão e senha, como um terminal recém-ligado. */
    static void voltarAoInicio() {
        BancoDeTeste.recriar();
        naTela(() -> {
            Sessao.getInstancia().encerrar();
            Navegador.iniciar(janela);
            return null;
        });
    }

    static void entrar(String cartao, String pin) {
        digitar("#campoCartao", cartao);
        digitar("#campoSenha", pin);
        clicar("Entrar");
    }

    static void digitar(String id, String texto) {
        naTela(() -> {
            ((TextInputControl) exigir(id)).setText(texto);
            return null;
        });
    }

    /** Clica no botão que mostra o texto informado na tela atual. */
    static void clicar(String textoDoBotao) {
        naTela(() -> {
            Button botao = raiz().lookupAll(".button").stream()
                    .filter(Button.class::isInstance).map(Button.class::cast)
                    .filter(b -> textoDoBotao.equals(b.getText()) && b.isVisible())
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Botão \"" + textoDoBotao + "\" não está na tela"));
            if (botao.isDisabled()) {
                throw new AssertionError("Botão \"" + textoDoBotao + "\" está desabilitado");
            }
            botao.fire();
            return null;
        });
    }

    /** Texto de um rótulo ou campo da tela atual, com espaços comuns no lugar dos espaços especiais de R$. */
    static String texto(String id) {
        return naTela(() -> {
            Node no = exigir(id);
            String texto = no instanceof Labeled rotulo ? rotulo.getText() : ((TextInputControl) no).getText();
            return normalizar(texto);
        });
    }

    static boolean estaNaTela(String id) {
        return naTela(() -> raiz().lookup(id) != null);
    }

    /** Espera a resposta do servidor: as chamadas são feitas em segundo plano, como no uso real. */
    static void esperarTela(String id) {
        esperar(() -> raiz().lookup(id) != null, "a tela com " + id);
    }

    static void esperarTexto(String id, String esperado) {
        esperar(() -> {
            Node no = raiz().lookup(id);
            return no instanceof Labeled rotulo && esperado.equals(normalizar(rotulo.getText()));
        }, "\"" + esperado + "\" em " + id);
    }

    /** Células visíveis de uma coluna do extrato, de cima para baixo: texto e classe de cor. */
    static List<String[]> celulasDaColuna(String idColuna) {
        esperar(() -> !celulas(idColuna).isEmpty(), "linhas na coluna " + idColuna);
        return naTela(() -> celulas(idColuna).stream()
                .map(celula -> new String[] {normalizar(celula.getText()), corDa(celula)})
                .toList());
    }

    private static List<TableCell<?, ?>> celulas(String idColuna) {
        return raiz().lookupAll(".table-cell").stream()
                .filter(TableCell.class::isInstance).<TableCell<?, ?>>map(no -> (TableCell<?, ?>) no)
                .filter(c -> c.getTableColumn() != null && idColuna.equals("#" + c.getTableColumn().getId()))
                .filter(c -> !c.isEmpty() && c.getText() != null)
                .sorted(Comparator.comparingInt(TableCell::getIndex))
                .toList();
    }

    private static String corDa(TableCell<?, ?> celula) {
        for (String classe : List.of("credito", "debito", "saldo-extrato")) {
            if (celula.getStyleClass().contains(classe)) {
                return classe;
            }
        }
        return "";
    }

    private static void esperar(BooleanSupplier condicao, String descricao) {
        long limite = System.currentTimeMillis() + ESPERA_MAXIMA_MS;
        while (System.currentTimeMillis() < limite) {
            if (naTela(condicao::getAsBoolean)) {
                return;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        fail("Esperava " + descricao + ", mas a tela mostra: " + naTela(TerminalDeTeste::textosDaTela));
    }

    private static String textosDaTela() {
        return raiz().lookupAll(".label").stream()
                .map(no -> normalizar(((Labeled) no).getText()))
                .filter(t -> t != null && !t.isBlank())
                .toList()
                .toString();
    }

    private static Node exigir(String id) {
        Node no = raiz().lookup(id);
        if (no == null) {
            throw new AssertionError(id + " não está na tela atual: " + textosDaTela());
        }
        return no;
    }

    private static Parent raiz() {
        return janela.getScene().getRoot();
    }

    private static String normalizar(String texto) {
        return texto == null ? null : texto.replace(' ', ' ').replace(' ', ' ');
    }

    /** Toda leitura e ação na interface acontece na thread do JavaFX, como num clique de verdade. */
    private static <T> T naTela(Callable<T> acao) {
        FutureTask<T> tarefa = new FutureTask<>(acao);
        Platform.runLater(tarefa);
        try {
            return tarefa.get(ESPERA_MAXIMA_MS, TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.ExecutionException e) {
            if (e.getCause() instanceof AssertionError erro) {
                throw erro;
            }
            throw new IllegalStateException(e.getCause());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static void aguardar(CountDownLatch sinal) {
        try {
            if (!sinal.await(ESPERA_MAXIMA_MS, TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException("O JavaFX não iniciou");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
