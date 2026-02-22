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

public class HiloServidor implements Runnable {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private static SessionFactory factory;

    static {
        try {
            factory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        } catch (Exception e) {
            System.err.println("Error al iniciar Hibernate: " + e.getMessage());
        }
    }

    public HiloServidor(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            String datosRecibidos = in.readUTF();
            String[] partes = datosRecibidos.split("\\|");
            String nombreJugador = partes[0];
            String modoJuego = partes.length > 1 ? partes[1] : "1";

            Jugador jugador = gestionarJugador(nombreJugador);

            long idAleatorio = (long) (Math.random() * 50) + 1;
            String palabraReal = obtenerPalabraAleatoria(idAleatorio);

            int intentosMaximos = palabraReal.length() / 2;
            int intentosRestantes = intentosMaximos;

            char[] letrasDescubiertas = new char[palabraReal.length()];
            for (int i = 0; i < letrasDescubiertas.length; i++) {
                letrasDescubiertas[i] = '_';
            }
            if (modoJuego.equals("2")) {
                out.writeUTF("[TURNO] ¡Bienvenido " + jugador.getNombre() + "!\nEl modo 2P está en camino. Tienes " + intentosMaximos + " intentos.");
            } else {
                out.writeUTF("[TURNO] ¡Bienvenido " + jugador.getNombre() + "!\nTienes " + intentosMaximos + " intentos para adivinar.");
            }
            out.writeUTF(formatearPalabra(letrasDescubiertas));

            boolean juegoTerminado = false;

            while (!juegoTerminado && intentosRestantes > 0) {
                String inputRecibido = in.readUTF();

                if (inputRecibido.equals("-CANCELAR-")) {
                    System.out.println("⚠️ Partida cancelada por: " + jugador.getNombre());
                    break;
                } else if (inputRecibido.equals("-PUNTUACION-")) {
                    int puntosTotales = obtenerPuntuacionTotal(jugador.getId());
                    out.writeUTF("[TURNO] PUNTUACIÓN GLOBAL: Tienes un total de " + puntosTotales + " puntos.");
                    out.writeUTF(formatearPalabra(letrasDescubiertas));
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

                String tableroActualizado = formatearPalabra(letrasDescubiertas);
                String mensaje = "";

                if (String.valueOf(letrasDescubiertas).equals(palabraReal)) {
                    int puntosGanados = (palabraReal.length() < 10) ? 1 : 2;
                    guardarPartida(jugador, true, puntosGanados);

                    mensaje = "[TURNO] ¡HAS GANADO! (+" + puntosGanados + " pts). La palabra era: " + palabraReal;
                    juegoTerminado = true;
                } else {
                    if (acierto) {
                        mensaje = "[TURNO] ¡Acierto! La letra '" + letra + "' está en la palabra.";
                    } else {
                        intentosRestantes--;
                        if (intentosRestantes == 0) {
                            guardarPartida(jugador, false, 0);
                            mensaje = "[TURNO] ¡HAS PERDIDO! Sin intentos. La palabra era: " + palabraReal;
                            tableroActualizado = formatearPalabra(palabraReal.toCharArray());
                            juegoTerminado = true;
                        } else {
                            mensaje = "[TURNO] Fallo. La letra '" + letra + "' no está. Te quedan " + intentosRestantes + " intentos.";
                        }
                    }
                }

                out.writeUTF(mensaje);
                out.writeUTF(tableroActualizado);
            }

        } catch (IOException e) {
            System.err.println("Cliente desconectado: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private Jugador gestionarJugador(String nombre) {
        Session session = factory.openSession();
        Transaction tx = null;
        Jugador j = null;
        try {
            tx = session.beginTransaction();
            Query<Jugador> query = session.createQuery("FROM Jugador WHERE nombre = :nombre", Jugador.class);
            query.setParameter("nombre", nombre);
            j = query.uniqueResult();
            if (j == null) {
                j = new Jugador(nombre);
                session.persist(j);
                System.out.println("Nuevo jugador registrado: " + nombre);
            } else {
                System.out.println("Jugador habitual conectado: " + nombre);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return j;
    }

    private String obtenerPalabraAleatoria(Long id) {
        Session session = factory.openSession();
        String textoPalabra = "ERROR";
        try {
            Palabra p = session.get(Palabra.class, id);
            if (p != null) textoPalabra = p.getPalabra();
        } finally {
            session.close();
        }
        return textoPalabra.toUpperCase();
    }

    private String formatearPalabra(char[] array) {
        StringBuilder sb = new StringBuilder();
        for (char c : array) {
            sb.append(c).append(" ");
        }
        return sb.toString().trim();
    }

    private void guardarPartida(Jugador jugador, boolean acierto, int puntos) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Puntuacion p = new Puntuacion(jugador, LocalDateTime.now(), acierto, puntos);
            session.persist(p);
            tx.commit();
            System.out.println("Partida guardada en BD para " + jugador.getNombre());
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    private int obtenerPuntuacionTotal(Long idJugador) {
        Session session = factory.openSession();
        int total = 0;
        try {
            Query<Long> query = session.createQuery("SELECT SUM(p.puntosGanados) FROM Puntuacion p WHERE p.jugador.id = :id", Long.class);
            query.setParameter("id", idJugador);
            Long resultado = query.uniqueResult();
            if (resultado != null) {
                total = resultado.intValue();
            }
        } finally {
            session.close();
        }
        return total;
    }
}