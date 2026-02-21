package com.liceolapaz.ahorcado.servidor;

import com.liceolapaz.ahorcado.modelo.Jugador;
import com.liceolapaz.ahorcado.modelo.Palabra;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class HiloServidor implements Runnable {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private static SessionFactory factory;

    // Bloque estático para arrancar Hibernate una sola vez
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

            String nombreJugador = in.readUTF();
            Jugador jugador = gestionarJugador(nombreJugador);

            long idAleatorio = (long) (Math.random() * 50) + 1;
            String palabraReal = obtenerPalabraAleatoria(idAleatorio);

            // 3. Preparar la partida
            int intentosMaximos = palabraReal.length() / 2;
            int intentosRestantes = intentosMaximos;

            char[] letrasDescubiertas = new char[palabraReal.length()];
            for (int i = 0; i < letrasDescubiertas.length; i++) {
                letrasDescubiertas[i] = '_';
            }

            System.out.println("🎯 Partida: " + jugador.getNombre() + " | Palabra: " + palabraReal + " | Intentos: " + intentosMaximos);

            out.writeUTF("¡Bienvenido " + jugador.getNombre() + "!\nTienes " + intentosMaximos + " intentos para adivinar.");
            out.writeUTF(formatearPalabra(letrasDescubiertas));

            boolean juegoTerminado = false;

            while (!juegoTerminado && intentosRestantes > 0) {

                String letraRecibida = in.readUTF();
                char letra = letraRecibida.charAt(0);

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
                    mensaje = "🏆 ¡HAS GANADO! La palabra era: " + palabraReal;
                    juegoTerminado = true;
                } else {
                    if (acierto) {
                        mensaje = "✅ ¡Acierto! La letra '" + letra + "' está en la palabra.";
                    } else {
                        intentosRestantes--;
                        if (intentosRestantes == 0) {
                            mensaje = "HAS PERDIDO Te has quedado sin intentos. La palabra era: " + palabraReal;
                            // Revelamos la palabra completa si pierde
                            tableroActualizado = formatearPalabra(palabraReal.toCharArray());
                            juegoTerminado = true;
                        } else {
                            mensaje = "Fallo. La letra '" + letra + "' no está. Te quedan " + intentosRestantes + " intentos.";
                        }
                    }
                }

                out.writeUTF(mensaje);
                out.writeUTF(tableroActualizado);
            }

        } catch (IOException e) {
            System.err.println("Cliente desconectado abruptamente: " + e.getMessage());
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
                System.out.println(" Nuevo jugador registrado: " + nombre);
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
            if (p != null) {
                textoPalabra = p.getPalabra();
            }
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
}