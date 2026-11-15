package co.edu.unillanos.campusfest.domain;

import java.time.LocalDateTime;

/**
 * Registro del ingreso de un estudiante al evento (CU4).
 * La crea Reserva.registrarIngreso (patrón Creador). Es inmutable.
 */
public class Asistencia {

    private final LocalDateTime hora;

    public Asistencia(LocalDateTime hora) {
        if (hora == null) {
            throw new IllegalArgumentException("La hora del ingreso es obligatoria.");
        }
        this.hora = hora;
    }

    public LocalDateTime getHora() {
        return hora;
    }
}