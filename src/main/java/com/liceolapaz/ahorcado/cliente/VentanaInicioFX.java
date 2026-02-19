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
            if (!txtNombre.getText().trim().isEmpty()) {
                ventanaPrincipal.setScene(escenaJuego);
            } else {
                mostrarAlerta("Error", "Por favor, introduce tu nombre.");
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
        panelLetra.getChildren().addAll(txtLetra, btnEnviar);

        HBox panelBotonesExtra = new HBox(15);
        panelBotonesExtra.setAlignment(Pos.CENTER);
        Button btnCancelar = new Button("Cancelar Partida");
        Button btnPuntuacion = new Button("Mostrar Puntuación");
        panelBotonesExtra.getChildren().addAll(btnCancelar, btnPuntuacion);

        panelControles.getChildren().addAll(panelLetra, panelBotonesExtra);
        layoutJuego.setBottom(panelControles);

        btnCancelar.setOnAction(e -> ventanaPrincipal.setScene(escenaInicio));

        escenaJuego = new Scene(layoutJuego, 450, 400);
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
