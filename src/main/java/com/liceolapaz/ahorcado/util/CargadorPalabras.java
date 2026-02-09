package com.liceolapaz.ahorcado.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liceolapaz.ahorcado.modelo.Palabra;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public class CargadorPalabras {
    public static void main(String[] args) {

        SessionFactory factory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = factory.openSession();
        Transaction tx = null;

        try {
            Reader reader = new InputStreamReader(Objects.requireNonNull(
                    CargadorPalabras.class.getClassLoader().getResourceAsStream("palabras-ahorcado.json")
            ));

            Gson gson = new Gson();
            Type listaTipo = new TypeToken<List<Palabra>>(){}.getType();
            List<Palabra> palabras = gson.fromJson(reader, listaTipo);

            tx = session.beginTransaction();
            System.out.println("Iniciando importación...");

            for (Palabra p : palabras) {
                session.merge(p);
            }

            tx.commit();
            System.out.println(" Se han guardado " + palabras.size() + " palabras en la BD.");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println(" ERROR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            session.close();
            factory.close();
        }
    }
}