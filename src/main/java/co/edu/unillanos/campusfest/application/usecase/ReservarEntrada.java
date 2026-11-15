package co.edu.unillanos.campusfest.application.usecase;

import co.edu.unillanos.campusfest.application.port.Reloj;
import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.domain.Reserva;

/**
 * CU2 · Reservar y confirmar una entrada.
 * Busca el evento, pide la hora, delega en las entidades y guarda si todo sale bien.
 */
public class ReservarEntrada {

    private final RepositorioEventos repositorio;
    private final Reloj reloj;

    public ReservarEntrada(RepositorioEventos repositorio, Reloj reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    public Reserva solicitarReserva(String eventoId, String estudianteId) {
        Evento evento = BuscadorDeEventos.buscarExistente(repositorio, eventoId);
        Reserva reserva = evento.reservar(estudianteId, reloj.ahora());
        repositorio.guardar(evento);
        return reserva;
    }

    public Reserva confirmarReserva(String eventoId, String codigoReserva) {
        Evento evento = BuscadorDeEventos.buscarExistente(repositorio, eventoId);
        Reserva reserva = evento.buscarReservaPorCodigo(codigoReserva);
        reserva.confirmar(reloj.ahora());
        repositorio.guardar(evento);
        return reserva;
    }
}
