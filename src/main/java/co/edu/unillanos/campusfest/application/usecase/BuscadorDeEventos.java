package co.edu.unillanos.campusfest.application.usecase;

import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Evento;

/**
 * Busca un evento que debe existir. Los cuatro casos de uso lo usan para no
 * repetir la misma verificación (cada regla en un solo lugar).
 */
final class BuscadorDeEventos {

    private BuscadorDeEventos() {
    }

    static Evento buscarExistente(RepositorioEventos repositorio, String eventoId) {
        Evento evento = repositorio.buscar(eventoId);
        if (evento == null) {
            throw new IllegalArgumentException("No existe el evento " + eventoId + ".");
        }
        return evento;
    }
}
