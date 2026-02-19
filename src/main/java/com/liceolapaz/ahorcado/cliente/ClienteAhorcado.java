package com.liceolapaz.ahorcado.cliente;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class ClienteAhorcado {
    private static final String HOST = "localhost";
    private static final int PUERTO = 65000;

    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.trustStore", "src/main/resources/servidor_keystore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");

        try {
            SSLSocketFactory sslFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) sslFactory.createSocket(HOST, PUERTO);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            String mensajeServidor = in.readUTF();
            System.out.println("Servidor dice: " + mensajeServidor);


            socket.close();

        } catch (IOException e) {
            System.err.println("Error al conectar: " + e.getMessage());
        }
    }
}