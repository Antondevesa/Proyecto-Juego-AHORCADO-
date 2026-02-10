package com.liceolapaz.ahorcado.servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class HiloServidor implements Runnable {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public HiloServidor(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            System.out.println("Hilo iniciado para cliente: " + socket.getInetAddress());

            out.writeUTF("Bienvenido al Servidor del Ahorcado.");


        } catch (IOException e) {
            System.err.println("Error con un cliente: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}