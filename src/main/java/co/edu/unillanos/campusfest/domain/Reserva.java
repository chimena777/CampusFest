package co.edu.unillanos.campusfest.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Reserva de un estudiante para un evento.
 *
 * Experta en su propio estado: sabe si ocupa cupo, si venció, si se puede
 * confirmar, cancelar o registrar el ingreso. El estado solo cambia con
 * métodos de negocio (no hay setters públicos).
 */
public class Reserva {

    /** Plazo para confirmar una reserva pendiente. Regla del plazo: vive solo aquí. */
    private static final int PLAZO_MINUTOS = 10;

    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String codigo;
    private final String estudianteId;
    private final LocalDateTime venceEn;
    private EstadoReserva estado;
    private Asistencia asistencia;

    private Reserva(String codigo, String estudianteId, LocalDateTime venceEn,
                    EstadoReserva estado, Asistencia asistencia) {
        this.codigo = codigo;
        this.estudianteId = estudianteId;
        this.venceEn = venceEn;
        this.estado = estado;
        this.asistencia = asistencia;
    }

    /** Crea una reserva PENDIENTE que vence en ahora + 10 minutos. */
    public static Reserva pendiente(String estudianteId, LocalDateTime ahora) {
        if (estudianteId == null || estudianteId.isBlank()) {
            throw new IllegalArgumentException("El identificador del estudiante es obligatorio.");
        }
        if (ahora == null) {
            throw new IllegalArgumentException("La hora de la solicitud es obligatoria.");
        }
        String codigo = UUID.randomUUID().toString();
        return new Reserva(codigo, estudianteId.trim(), ahora.plusMinutes(PLAZO_MINUTOS),
                EstadoReserva.PENDIENTE, null);
    }

    /**
     * Vuelve a armar una reserva leída de la base de datos.
     * La usa el adaptador de persistencia; no aplica reglas.
     */
    public static Reserva reconstruir(String codigo, String estudianteId, LocalDateTime venceEn,
                                      EstadoReserva estado, Asistencia asistencia) {
        return new Reserva(codigo, estudianteId, venceEn, estado, asistencia);
    }

    /** Una reserva ocupa cupo si está confirmada o si está pendiente y no ha vencido. */
    public boolean ocupaCupo(LocalDateTime ahora) {
        if (estado == EstadoReserva.CONFIRMADA) {
            return true;
        }
        return estado == EstadoReserva.PENDIENTE && !vencio(ahora);
    }

    public boolean actualizarVencimiento(LocalDateTime ahora) {

    if (ahora == null) {
        throw new IllegalArgumentException(
            "La fecha actual no puede ser nula."
        );
    }

    if (estado == EstadoReserva.PENDIENTE && vencio(ahora)) {
        estado = EstadoReserva.VENCIDA;
        return true;
    }

    return false;
}

    /** Confirma la reserva antes del plazo. A los 10:00 exactos ya está vencida. */
    public void confirmar(LocalDateTime ahora) {
        if (estado != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException("La reserva no se puede confirmar porque está "
                    + estado + ".");
        }
        if (vencio(ahora)) {
            estado = EstadoReserva.VENCIDA;
            throw new IllegalStateException("La reserva venció: debía confirmarse antes de "
                    + venceEn.format(FORMATO_HORA) + ".");
        }
        estado = EstadoReserva.CONFIRMADA;
    }

    /** Cancela una reserva pendiente o confirmada. */
    public void cancelar() {
        if (estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("La reserva no se puede cancelar porque está "
                    + estado + ".");
        }
        estado = EstadoReserva.CANCELADA;
    }

    /** Registra el único ingreso permitido de una reserva confirmada (patrón Creador). */
    public Asistencia registrarIngreso(LocalDateTime ahora) {
        if (estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("La reserva no está confirmada (estado "
                    + estado + "): no puede ingresar.");
        }
        if (asistencia != null) {
            throw new IllegalStateException("Esta reserva ya ingresó a las "
                    + asistencia.getHora().format(FORMATO_HORA) + ".");
        }
        asistencia = new Asistencia(ahora);
        return asistencia;
    }

    private boolean vencio(LocalDateTime ahora) {
        return !ahora.isBefore(venceEn);
    }

    public String getCodigo() {
        return codigo;
    }

    public String getEstudianteId() {
        return estudianteId;
    }

    public LocalDateTime getVenceEn() {
        return venceEn;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    /** Null si la reserva todavía no ha ingresado. */
    public Asistencia getAsistencia() {
        return asistencia;
    }
}