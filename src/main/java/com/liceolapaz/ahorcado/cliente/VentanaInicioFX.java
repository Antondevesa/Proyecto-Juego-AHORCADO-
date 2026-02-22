package com.liceolapaz.ahorcado.cliente;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class VentanaInicioFX extends Application {

    private Stage ventanaPrincipal;
    private Scene escenaInicio;
    private Scene escenaJuego;
    private TextField txtNombre;
    private RadioButton rb1Jugador;
    private RadioButton rb2Jugadores;
    private Label lblPalabraOculta;
    private TextField txtLetra;
    private Button btnEnviar;
    private TextArea areaMensajes;
    private SSLSocket socket;
    private DataInputStream in;
    private DataOutputStream out;

    @Override
    public void start(Stage primaryStage) {
        this.ventanaPrincipal = primaryStage;
        ventanaPrincipal.setTitle("Juego del Ahorcado - Cliente");
        crearEscenaInicio();
        crearEscenaJuego();
        ventanaPrincipal.setScene(escenaInicio);
        ventanaPrincipal.show();
    }

    private void crearEscenaInicio() {
        VBox layoutInicio = new VBox(20);
        layoutInicio.setAlignment(Pos.CENTER);
        layoutInicio.setPadding(new Insets(40));

        Label lblTitulo = new Label("Bienvenido al Ahorcado");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        HBox cajaNombre = new HBox(10);
        cajaNombre.setAlignment(Pos.CENTER);
        cajaNombre.getChildren().add(new Label("Tu Nombre:"));
        txtNombre = new TextField();
        cajaNombre.getChildren().add(txtNombre);

        // SELECTOR DE MODO DE JUEGO
        HBox cajaModo = new HBox(15);
        cajaModo.setAlignment(Pos.CENTER);
        ToggleGroup tgModo = new ToggleGroup();
        rb1Jugador = new RadioButton("1 Jugador");
        rb2Jugadores = new RadioButton("2 Jugadores");
        rb1Jugador.setToggleGroup(tgModo);
        rb2Jugadores.setToggleGroup(tgModo);
        rb1Jugador.setSelected(true); // Por defecto 1 Jugador
        cajaModo.getChildren().addAll(rb1Jugador, rb2Jugadores);

        Button btnIniciar = new Button("Iniciar Partida");

        btnIniciar.setOnAction(e -> {
            String nombre = txtNombre.getText().trim();
            String modo = rb1Jugador.isSelected() ? "1" : "2";

            if (!nombre.isEmpty()) {
                conectarAlServidor(nombre + "|" + modo);
            } else {
                mostrarAlerta("Error", "Introduce tu nombre");
            }
        });

        layoutInicio.getChildren().addAll(lblTitulo, cajaNombre, cajaModo, btnIniciar);
        escenaInicio = new Scene(layoutInicio, 450, 400);
    }

    private void crearEscenaJuego() {
        BorderPane layoutJuego = new BorderPane();
        layoutJuego.setPadding(new Insets(20));

        lblPalabraOculta = new Label("_ _ _ _ _ _");
        lblPalabraOculta.setFont(Font.font("Monospaced", FontWeight.BOLD, 30));
        BorderPane.setAlignment(lblPalabraOculta, Pos.CENTER);
        layoutJuego.setTop(lblPalabraOculta);

        areaMensajes = new TextArea("Esperando turno...\n");
        areaMensajes.setEditable(false);
        layoutJuego.setCenter(areaMensajes);
        BorderPane.setMargin(areaMensajes, new Insets(20, 0, 20, 0));

        VBox panelControles = new VBox(15);
        panelControles.setAlignment(Pos.CENTER);

        HBox panelLetra = new HBox(10);
        panelLetra.setAlignment(Pos.CENTER);
        panelLetra.getChildren().add(new Label("Letra:"));
        txtLetra = new TextField();
        txtLetra.setPrefWidth(50);

        btnEnviar = new Button("Enviar");

        btnEnviar.setOnAction(e -> {
            String letra = txtLetra.getText().trim().toUpperCase();
            if (letra.length() == 1) {
                try {
                    out.writeUTF(letra);
                    txtLetra.setText("");
                } catch (Exception ex) {
                    areaMensajes.appendText("Error al enviar la letra\n");
                }
            } else {
                mostrarAlerta("Aviso", "Introduce solo 1 letra");
            }
        });

        panelLetra.getChildren().addAll(txtLetra, btnEnviar);

        HBox panelBotonesExtra = new HBox(15);
        panelBotonesExtra.setAlignment(Pos.CENTER);
        Button btnCancelar = new Button("Cancelar Partida");
        Button btnPuntuacion = new Button("Mostrar Puntuación");

        btnPuntuacion.setOnAction(e -> {
            try {
                if (out != null) out.writeUTF("-PUNTUACION-");
            } catch (Exception ex) {}
        });

        btnCancelar.setOnAction(e -> {
            try {
                if (out != null) out.writeUTF("-CANCELAR-");
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (Exception ex) {}

            txtNombre.setText("");
            txtLetra.setDisable(false);
            btnEnviar.setDisable(false);
            areaMensajes.setText("");
            lblPalabraOculta.setText("_ _ _ _ _ _");
            ventanaPrincipal.setScene(escenaInicio);
        });

        panelBotonesExtra.getChildren().addAll(btnCancelar, btnPuntuacion);
        panelControles.getChildren().addAll(panelLetra, panelBotonesExtra);
        layoutJuego.setBottom(panelControles);

        escenaJuego = new Scene(layoutJuego, 450, 450);
    }

    private void conectarAlServidor(String datosConexion) {
        try {
            System.setProperty("javax.net.ssl.trustStore", "src/main/resources/servidor_keystore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "123456");

            SSLSocketFactory sslFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = (SSLSocket) sslFactory.createSocket("localhost", 65000);

            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            out.writeUTF(datosConexion);

            String mensajeBienvenida = in.readUTF();
            String palabraOcultaInicial = in.readUTF();

            txtLetra.setDisable(false);
            btnEnviar.setDisable(false);

            mensajeBienvenida = mensajeBienvenida.replace("[TURNO]", "").trim();

            areaMensajes.setText(mensajeBienvenida + "\n");
            lblPalabraOculta.setText(palabraOcultaInicial);

            ventanaPrincipal.setScene(escenaJuego);

            Thread hiloEscucha = new Thread(() -> {
                try {
                    while (true) {
                        String mensajeRecibido = in.readUTF();
                        String tablero = in.readUTF();

                        Platform.runLater(() -> {
                            String mensajeProcesado = mensajeRecibido;

                            if (mensajeProcesado.startsWith("[TURNO]")) {
                                txtLetra.setDisable(false);
                                btnEnviar.setDisable(false);
                                mensajeProcesado = mensajeProcesado.replace("[TURNO]", "").trim();
                            } else if (mensajeProcesado.startsWith("[ESPERA]")) {
                                txtLetra.setDisable(true);
                                btnEnviar.setDisable(true);
                                mensajeProcesado = mensajeProcesado.replace("[ESPERA]", "").trim();
                            }

                            areaMensajes.appendText(mensajeProcesado + "\n");
                            lblPalabraOculta.setText(tablero);

                            if (mensajeProcesado.contains("¡HAS GANADO!") || mensajeProcesado.contains("¡HAS PERDIDO!")) {
                                txtLetra.setDisable(true);
                                btnEnviar.setDisable(true);
                            }
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> areaMensajes.appendText("Desconectado del servidor\n"));
                }
            });
            hiloEscucha.setDaemon(true);
            hiloEscucha.start();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error de Conexión", "No se pudo conectar al servidor");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}