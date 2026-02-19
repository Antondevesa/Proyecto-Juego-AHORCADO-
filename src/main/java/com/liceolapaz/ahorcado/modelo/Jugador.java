package com.liceolapaz.ahorcado.modelo;

import com.liceolapaz.ahorcado.seguridad.CifradoAESConverter;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "jugadores")
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = CifradoAESConverter.class)
    @Column(name = "nombre_cifrado", nullable = false, unique = true)
    private String nombre;

    @OneToMany(mappedBy = "jugador", cascade = CascadeType.ALL)
    private List<Puntuacion> puntuaciones;

    public Jugador() {}

    public Jugador(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}