package com.liceolapaz.ahorcado.servidor;

import com.liceolapaz.ahorcado.modelo.Jugador;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class GestorEmparejamiento {
    private static Socket socketEspera = null;
    private static DataInputStream inEspera = null;
    private static DataOutputStream outEspera = null;
    private static Jugador jugadorEspera = null;

    public static synchronized void conectarJugador(Socket socket, DataInputStream in, DataOutputStream out, Jugador jugador) {
        try {
            if (socketEspera == null) {
                socketEspera = socket;
                inEspera = in;
                outEspera = out;
                jugadorEspera = jugador;

                out.writeUTF("[ESPERA] Buscando un oponente...\nEspera a que alguien más se conecte en modo 2 Jugadores");
                out.writeUTF("_ _ _ _ _ _");
            } else {
                out.writeUTF("[ESPERA] ¡Oponente encontrado! Preparando partida");
                outEspera.writeUTF("[ESPERA] ¡Oponente encontrado! Preparando partida");

                new Thread(new HiloPartidaMultijugador(
                        socketEspera, inEspera, outEspera, jugadorEspera,
                        socket, in, out, jugador
                )).start();

                socketEspera = null;
                inEspera = null;
                outEspera = null;
                jugadorEspera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}