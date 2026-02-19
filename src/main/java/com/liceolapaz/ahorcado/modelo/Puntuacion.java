package com.liceolapaz.ahorcado.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "puntuaciones")
public class Puntuacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "acierto", nullable = false)
    private boolean acierto;

    @Column(name = "puntos_ganados")
    private int puntosGanados;

    public Puntuacion() {}

    public Puntuacion(Jugador jugador, LocalDateTime fechaHora, boolean acierto, int puntosGanados) {
        this.jugador = jugador;
        this.fechaHora = fechaHora;
        this.acierto = acierto;
        this.puntosGanados = puntosGanados;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Jugador getJugador() { return jugador; }
    public void setJugador(Jugador jugador) { this.jugador = jugador; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public boolean isAcierto() { return acierto; }
    public void setAcierto(boolean acierto) { this.acierto = acierto; }
    public int getPuntosGanados() { return puntosGanados; }
    public void setPuntosGanados(int puntosGanados) { this.puntosGanados = puntosGanados; }
}