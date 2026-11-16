package co.edu.unillanos.campusfest.application.usecase;

import co.edu.unillanos.campusfest.application.port.Reloj;
import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Evento;

/** CU1 · Consultar cupos. Solo coordina: el cálculo lo hace Evento. No guarda nada. */
public class ConsultarCupos {

    private final RepositorioEventos repositorio;
    private final Reloj reloj;

    public ConsultarCupos(RepositorioEventos repositorio, Reloj reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    public int consultar(String eventoId) {
        Evento evento = BuscadorDeEventos.buscarExistente(repositorio, eventoId);
        return evento.cuposDisponibles(reloj.ahora());
    }
}
