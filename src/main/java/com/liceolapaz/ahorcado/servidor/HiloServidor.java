package com.liceolapaz.ahorcado.servidor;

import com.liceolapaz.ahorcado.modelo.Jugador;
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

    static {
        try {
            factory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        } catch (Exception e) {
            System.err.println("Error al iniciar Hibernate en el servidor: " + e.getMessage());
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
            System.out.println("➡️ Petición de conexión recibida de: " + nombreJugador);

            Jugador jugador = gestionarJugador(nombreJugador);
            out.writeUTF("Conexión establecida!\nBienvenido " + jugador.getNombre() + ".\nBuscando palabra...");

        } catch (IOException e) {
            System.err.println("Error con un cliente: " + e.getMessage());
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
                System.out.println("Nuevo jugador registrado en BD: " + nombre);
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
}