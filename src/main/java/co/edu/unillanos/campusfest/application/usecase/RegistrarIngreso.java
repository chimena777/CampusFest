package co.edu.unillanos.campusfest.application.usecase;

import co.edu.unillanos.campusfest.application.port.Reloj;
import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Asistencia;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.domain.Reserva;

/**
 * CU4 · Registrar el ingreso con QR.
 * El lector de QR es un detalle: el código llega como String.
 */
public class RegistrarIngreso {

    private final RepositorioEventos repositorio;
    private final Reloj reloj;

    public RegistrarIngreso(RepositorioEventos repositorio, Reloj reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    public Asistencia registrar(String eventoId, String codigo) {
        Evento evento = BuscadorDeEventos.buscarExistente(repositorio, eventoId);
        Reserva reserva = evento.buscarReservaPorCodigo(codigo);
        Asistencia asistencia = reserva.registrarIngreso(reloj.ahora());
        repositorio.guardar(evento);
        return asistencia;
    }
}
