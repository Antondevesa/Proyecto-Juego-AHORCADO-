package com.liceolapaz.ahorcado.servidor;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.Socket;

public class ServidorAhorcado {
    private static final int PUERTO = 65000;

    public static void main(String[] args) {
        String rutaKeystore = "src/main/resources/servidor_keystore.jks";

        System.setProperty("javax.net.ssl.keyStore", rutaKeystore);
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");

        try {
            SSLServerSocketFactory sslFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket serverSocket = (SSLServerSocket) sslFactory.createServerSocket(PUERTO);

            System.out.println("Servidor seguro en el puerto " + PUERTO);

            while (true) {
                Socket socketCliente = serverSocket.accept();
                System.out.println("Nuevo cliente conectado: " + socketCliente.getInetAddress());

            }
        } catch (IOException e) {
            System.err.println("Error al iniciar servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}