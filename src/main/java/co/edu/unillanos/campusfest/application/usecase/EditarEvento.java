package co.edu.unillanos.campusfest.application.usecase;

import java.time.LocalDateTime;

import co.edu.unillanos.campusfest.application.port.Reloj;
import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Evento;

/**
 * CU3 · Editar evento. La regla del aforo vive en Evento.cambiarAforo;
 * este caso de uso solo coordina y guarda.
 */
public class EditarEvento {

    private final RepositorioEventos repositorio;
    private final Reloj reloj;

    public EditarEvento(RepositorioEventos repositorio, Reloj reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    /** Devuelve el evento para llenar el formulario de edición. */
    public Evento obtener(String eventoId) {
        return BuscadorDeEventos.buscarExistente(repositorio, eventoId);
    }

    public void editar(String eventoId, String nombre, LocalDateTime fecha,
                       String lugar, int nuevoAforo) {
        Evento evento = BuscadorDeEventos.buscarExistente(repositorio, eventoId);
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("-El nombre del evento es obligatorio.");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("-La fecha del evento es obligatoria.");
        }
        if (lugar == null || lugar.isBlank()) {
            throw new IllegalArgumentException("-El lugar del evento es obligatorio.");
        }
        evento.cambiarAforo(nuevoAforo, reloj.ahora());
        evento.cambiarDatos(nombre, fecha, lugar);
        repositorio.guardar(evento);
    }
}
