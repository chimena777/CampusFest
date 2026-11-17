package co.edu.unillanos.campusfest.application.usecase;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.domain.Reserva;

class ConsultasSinEscrituraEquipoTest {

    @Test
    void consultarLiberaCupoAlMinutoDiezSinGuardar() {
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 20, 18, 0);
        RelojFijo reloj = new RelojFijo(ahora);
        RepositorioEventosFalso repo = new RepositorioEventosFalso();
        Evento evento = new Evento("ev-1", "Concierto", ahora.plusDays(1),
                "Auditorio", 10, 2);
        Reserva reserva = evento.reservar("est-1", ahora);
        repo.agregar(evento);

        ConsultarCupos consultar = new ConsultarCupos(repo, reloj);
        assertEquals(7, consultar.consultar("ev-1"));

        reloj.avanzarMinutos(10);
        assertEquals(8, consultar.consultar("ev-1"));
        assertEquals(0, repo.getVecesGuardado());

        assertThrows(IllegalStateException.class,
                () -> new ReservarEntrada(repo, reloj)
                        .confirmarReserva("ev-1", reserva.getCodigo()));
        assertEquals(0, repo.getVecesGuardado());
    }

    @Test
    void obtenerEventoNoGuarda() {
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 20, 18, 0);
        RepositorioEventosFalso repo = new RepositorioEventosFalso();
        Evento evento = new Evento("ev-1", "Concierto", ahora.plusDays(1),
                "Auditorio", 10, 2);
        repo.agregar(evento);

        EditarEvento editar = new EditarEvento(repo, new RelojFijo(ahora));
        assertSame(evento, editar.obtener("ev-1"));
        assertEquals(0, repo.getVecesGuardado());
    }
}