package com.liceolapaz.ahorcado.servidor;

import com.liceolapaz.ahorcado.modelo.Jugador;
import com.liceolapaz.ahorcado.modelo.Palabra;
import com.liceolapaz.ahorcado.modelo.Puntuacion;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;

public class HiloPartidaMultijugador implements Runnable {
    private Socket s1, s2;
    private DataInputStream in1, in2;
    private DataOutputStream out1, out2;
    private Jugador j1, j2;
    private static SessionFactory factory;

    static {
        try {
            factory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        } catch (Exception e) {}
    }

    public HiloPartidaMultijugador(Socket s1, DataInputStream in1, DataOutputStream out1, Jugador j1,
                                   Socket s2, DataInputStream in2, DataOutputStream out2, Jugador j2) {
        this.s1 = s1; this.in1 = in1; this.out1 = out1; this.j1 = j1;
        this.s2 = s2; this.in2 = in2; this.out2 = out2; this.j2 = j2;
    }

    @Override
    public void run() {
        try {
            long idAleatorio = (long) (Math.random() * 50) + 1;
            String palabraReal = obtenerPalabraAleatoria(idAleatorio);
            int intentosMaximos = palabraReal.length() / 2;
            int intentosRestantes = intentosMaximos;

            char[] letrasDescubiertas = new char[palabraReal.length()];
            for (int i = 0; i < letrasDescubiertas.length; i++) letrasDescubiertas[i] = '_';

            boolean turnoJ1 = true;
            boolean juegoTerminado = false;

            String tableroInicial = formatearPalabra(letrasDescubiertas);
            out1.writeUTF("[TURNO] ¡Empieza la partida!\nTienes el primer turno. Intentos de equipo: " + intentosRestantes);
            out1.writeUTF(tableroInicial);

            out2.writeUTF("[ESPERA] ¡Empieza la partida!\nEmpieza jugando " + j1.getNombre() + ". Intentos de equipo: " + intentosRestantes);
            out2.writeUTF(tableroInicial);

            while (!juegoTerminado && intentosRestantes > 0) {
                DataInputStream inActual = turnoJ1 ? in1 : in2;
                DataOutputStream outActual = turnoJ1 ? out1 : out2;
                DataOutputStream outEspera = turnoJ1 ? out2 : out1;
                Jugador jugActual = turnoJ1 ? j1 : j2;
                Jugador jugEspera = turnoJ1 ? j2 : j1;

                String inputRecibido = inActual.readUTF();

                if (inputRecibido.equals("-CANCELAR-")) {
                    outActual.writeUTF("[ESPERA] Te has rendido.");
                    outActual.writeUTF(formatearPalabra(letrasDescubiertas));
                    outEspera.writeUTF("[ESPERA] El rival se ha rendido. Partida terminada.");
                    outEspera.writeUTF(formatearPalabra(letrasDescubiertas));
                    break;
                } else if (inputRecibido.equals("-PUNTUACION-")) {
                    int puntos = obtenerPuntuacionTotal(jugActual.getId());
                    outActual.writeUTF("[TURNO] PUNTUACIÓN GLOBAL: Tienes " + puntos + " puntos.");
                    outActual.writeUTF(formatearPalabra(letrasDescubiertas));
                    continue;
                }

                char letra = inputRecibido.charAt(0);
                boolean acierto = false;
                for (int i = 0; i < palabraReal.length(); i++) {
                    if (palabraReal.charAt(i) == letra) {
                        letrasDescubiertas[i] = letra;
                        acierto = true;
                    }
                }

                String tablero = formatearPalabra(letrasDescubiertas);

                if (String.valueOf(letrasDescubiertas).equals(palabraReal)) {
                    int puntosGanados = (palabraReal.length() < 10) ? 1 : 2;
                    guardarPartida(jugActual, true, puntosGanados);
                    guardarPartida(jugEspera, false, 0);

                    outActual.writeUTF("[ESPERA]HAS GANADO. Diste el golpe de gracia. (+" + puntosGanados + " pts)");
                    outEspera.writeUTF("[ESPERA] " + jugActual.getNombre() + " acertó la última letra. ¡Perdiste!");

                    outActual.writeUTF(tablero);
                    outEspera.writeUTF(tablero);
                    juegoTerminado = true;
                } else {
                    if (acierto) {
                        outActual.writeUTF("[TURNO] ¡Bien! La letra '" + letra + "' está. Sigues tirando.");
                        outEspera.writeUTF("[ESPERA] " + jugActual.getNombre() + " acertó la '" + letra + "'. Le sigue tocando.");
                    } else {
                        intentosRestantes--;
                        if (intentosRestantes == 0) {
                            guardarPartida(jugActual, false, 0);
                            guardarPartida(jugEspera, false, 0);
                            String msjDerrota = "[ESPERA] 💀 ¡SIN INTENTOS DE EQUIPO! La palabra era: " + palabraReal;
                            String tableroDerrota = formatearPalabra(palabraReal.toCharArray());

                            outActual.writeUTF(msjDerrota);
                            outActual.writeUTF(tableroDerrota);
                            outEspera.writeUTF(msjDerrota);
                            outEspera.writeUTF(tableroDerrota);
                            juegoTerminado = true;
                        } else {
                            turnoJ1 = !turnoJ1;
                            outActual.writeUTF("[ESPERA] Fallaste. Quedan " + intentosRestantes + " intentos al equipo. Turno de " + jugEspera.getNombre());
                            outEspera.writeUTF("[TURNO] " + jugActual.getNombre() + " falló. ¡Te toca! Intentos: " + intentosRestantes);
                        }
                    }
                    if (!juegoTerminado) {
                        outActual.writeUTF(tablero);
                        outEspera.writeUTF(tablero);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Un jugador se desconectó de la partida multijugador.");
        } finally {
            try { s1.close(); s2.close(); } catch (IOException e) {}
        }
    }
    private String obtenerPalabraAleatoria(Long id) {
        Session session = factory.openSession();
        String textoPalabra = "ERROR";
        try { Palabra p = session.get(Palabra.class, id); if (p != null) textoPalabra = p.getPalabra(); } finally { session.close(); }
        return textoPalabra.toUpperCase();
    }
    private String formatearPalabra(char[] array) {
        StringBuilder sb = new StringBuilder();
        for (char c : array) sb.append(c).append(" ");
        return sb.toString().trim();
    }
    private void guardarPartida(Jugador jugador, boolean acierto, int puntos) {
        Session session = factory.openSession();
        Transaction tx = null;
        try { tx = session.beginTransaction(); Puntuacion p = new Puntuacion(jugador, LocalDateTime.now(), acierto, puntos); session.persist(p); tx.commit(); } catch (Exception e) { if (tx != null) tx.rollback(); } finally { session.close(); }
    }
    private int obtenerPuntuacionTotal(Long idJugador) {
        Session session = factory.openSession();
        int total = 0;
        try { Query<Long> query = session.createQuery("SELECT SUM(p.puntosGanados) FROM Puntuacion p WHERE p.jugador.id = :id", Long.class); query.setParameter("id", idJugador); Long resultado = query.uniqueResult(); if (resultado != null) total = resultado.intValue(); } finally { session.close(); }
        return total;
    }
}