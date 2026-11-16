package co.edu.unillanos.campusfest.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Evento del campus con su aforo y sus reservas.
 *
 * Experto en la disponibilidad: calcula los cupos preguntando a cada
 * Reserva si ocupa cupo. Crea las reservas (patrón Creador).
 */
public class Evento {

    private final String id;
    private String nombre;
    private LocalDateTime fecha;
    private String lugar;
    private int aforo;
    private final int bloqueados;
    private final List<Reserva> reservas;

    public Evento(String id, String nombre, LocalDateTime fecha, String lugar,
                  int aforo, int bloqueados) {
        this(id, nombre, fecha, lugar, aforo, bloqueados, new ArrayList<>());
    }

    /** Para el adaptador de persistencia: arma el evento con sus reservas guardadas. */
    public Evento(String id, String nombre, LocalDateTime fecha, String lugar,
                  int aforo, int bloqueados, List<Reserva> reservas) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El identificador del evento es obligatorio.");
        }
        validarDatos(nombre, fecha, lugar);
        if (bloqueados < 0) {
            throw new IllegalArgumentException("Los cupos bloqueados no pueden ser negativos.");
        }
        if (aforo < bloqueados) {
            throw new IllegalArgumentException("El aforo (" + aforo
                    + ") no puede ser menor que los cupos bloqueados (" + bloqueados + ").");
        }
        this.id = id;
        this.nombre = nombre.trim();
        this.fecha = fecha;
        this.lugar = lugar.trim();
        this.aforo = aforo;
        this.bloqueados = bloqueados;
        this.reservas = new ArrayList<>();
        if (reservas != null) {
            this.reservas.addAll(reservas);
        }
    }

    /** CU1. Cupos disponibles = aforo − bloqueados − cupos ocupados por reservas. */
    public int cuposDisponibles(LocalDateTime ahora) {
        return aforo - bloqueados - cuposOcupadosPorReservas(ahora);
    }

    /** CU2. Crea una reserva pendiente si queda al menos un cupo disponible. */
    public Reserva reservar(String estudianteId, LocalDateTime ahora) {
        if (cuposDisponibles(ahora) <= 0) {
            throw new IllegalStateException("No hay cupos disponibles para el evento "
                    + nombre + ".");
        }
        Reserva reserva = Reserva.pendiente(estudianteId, ahora);
        reservas.add(reserva);
        return reserva;
    }

    /** CU2 y CU4. Devuelve la reserva de este evento que tiene ese código. */
    public Reserva buscarReservaPorCodigo(String codigo) {
        for (Reserva reserva : reservas) {
            if (reserva.getCodigo().equals(codigo)) {
                return reserva;
            }
        }
        throw new IllegalArgumentException("El código " + codigo
                + " no corresponde a una reserva del evento " + nombre + ".");
    }

    /** CU3. Cambia nombre, fecha y lugar. */
    public void cambiarDatos(String nombre, LocalDateTime fecha, String lugar) {
        validarDatos(nombre, fecha, lugar);
        this.nombre = nombre.trim();
        this.fecha = fecha;
        this.lugar = lugar.trim();
    }

    /** CU3. El aforo nunca puede quedar por debajo de bloqueados + cupos ocupados por reservas. */
    public void cambiarAforo(int nuevoAforo, LocalDateTime ahora) {
        int ocupados = cuposOcupadosPorReservas(ahora);
        int aforoMinimo = bloqueados + ocupados;
        if (nuevoAforo < aforoMinimo) {
            throw new IllegalStateException("Hay " + ocupados + " cupos ocupados por reservas y "
                    + bloqueados + " bloqueados: el aforo no puede ser menor que "
                    + aforoMinimo + ".");
        }
        this.aforo = nuevoAforo;
    }

    private int cuposOcupadosPorReservas(LocalDateTime ahora) {
        int ocupados = 0;
        for (Reserva reserva : reservas) {
            if (reserva.ocupaCupo(ahora)) {
                ocupados++;
            }
        }
        return ocupados;
    }

        public boolean actualizarReservasVencidas(
        LocalDateTime ahora) {

    boolean huboCambios = false;

    for (Reserva reserva : reservas) {
        if (reserva.actualizarVencimiento(ahora)) {
            huboCambios = true;
        }
    }

    return huboCambios;
}

    private static void validarDatos(String nombre, LocalDateTime fecha, String lugar) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del evento es obligatorio.");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha del evento es obligatoria.");
        }
        if (lugar == null || lugar.isBlank()) {
            throw new IllegalArgumentException("El lugar del evento es obligatorio.");
        }
    }


    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getLugar() {
        return lugar;
    }

    public int getAforo() {
        return aforo;
    }

    public int getBloqueados() {
        return bloqueados;
    }

    /** Lista de solo lectura: las reservas solo se agregan con reservar(...). */
    public List<Reserva> getReservas() {
        return Collections.unmodifiableList(reservas);
    }
}