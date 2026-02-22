package com.liceolapaz.ahorcado.cliente;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class VentanaInicioFX extends Application {

    private Stage ventanaPrincipal;
    private Scene escenaInicio;
    private Scene escenaJuego;
    private TextField txtNombre;
    private Label lblPalabraOculta;
    private TextField txtLetra;
    private TextArea areaMensajes;
    private javax.net.ssl.SSLSocket socket;
    private java.io.DataInputStream in;
    private java.io.DataOutputStream out;

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

        Button btnIniciar = new Button("Iniciar Partida");

        btnIniciar.setOnAction(e -> {
            String nombre = txtNombre.getText().trim();
            if (!nombre.isEmpty()) {
                conectarAlServidor(nombre);
            } else {
                mostrarAlerta("Error", "Introduce tu nombre");
            }
        });

        layoutInicio.getChildren().addAll(lblTitulo, cajaNombre, btnIniciar);
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
        Button btnEnviar = new Button("Enviar");

        btnEnviar.setOnAction(e -> {
            String letra = txtLetra.getText().trim().toUpperCase();
            if (letra.length() == 1) {
                try {
                    out.writeUTF(letra);
                    txtLetra.setText("");
                    areaMensajes.appendText("Has enviado la letra: " + letra + "\n");
                } catch (Exception ex) {
                    areaMensajes.appendText("Error al enviar la letra.\n");
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
        panelBotonesExtra.getChildren().addAll(btnCancelar, btnPuntuacion);

        panelControles.getChildren().addAll(panelLetra, panelBotonesExtra);
        layoutJuego.setBottom(panelControles);

        btnCancelar.setOnAction(e -> {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ventanaPrincipal.setScene(escenaInicio);
        });

        escenaJuego = new Scene(layoutJuego, 450, 400);
    }

    private void conectarAlServidor(String nombre) {
        try {
            System.setProperty("javax.net.ssl.trustStore", "src/main/resources/servidor_keystore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "123456");

            javax.net.ssl.SSLSocketFactory sslFactory = (javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory.getDefault();
            socket = (javax.net.ssl.SSLSocket) sslFactory.createSocket("localhost", 65000);

            out = new java.io.DataOutputStream(socket.getOutputStream());
            in = new java.io.DataInputStream(socket.getInputStream());

            out.writeUTF(nombre);

            String mensajeBienvenida = in.readUTF();
            String palabraOcultaInicial = in.readUTF();

            areaMensajes.setText(mensajeBienvenida + "\n");
            lblPalabraOculta.setText(palabraOcultaInicial);

            ventanaPrincipal.setScene(escenaJuego);

            Thread hiloEscucha = new Thread(() -> {
                try {
                    while (true) {
                        String mensaje = in.readUTF();
                        String tablero = in.readUTF();

                        javafx.application.Platform.runLater(() -> {
                            areaMensajes.appendText(mensaje + "\n");
                            lblPalabraOculta.setText(tablero);

                            if (mensaje.contains("¡HAS GANADO!") || mensaje.contains("¡HAS PERDIDO!")) {
                                txtLetra.setDisable(true);
                            }
                        });
                    }
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() ->
                            areaMensajes.appendText("Desconectado del servidor\n")
                    );
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