package com.liceolapaz.ahorcado.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "palabras")
public class Palabra {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "palabra", length = 15, nullable = false)
    private String palabra;
    public Palabra() {}

    public Palabra(Long id, String palabra) {
        this.id = id;
        this.palabra = palabra;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPalabra() { return palabra; }
    public void setPalabra(String palabra) { this.palabra = palabra; }
}