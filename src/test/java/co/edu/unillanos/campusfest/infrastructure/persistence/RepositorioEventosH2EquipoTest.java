package co.edu.unillanos.campusfest.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.unillanos.campusfest.domain.EstadoReserva;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.domain.Reserva;

/** Pruebas del equipo para el adaptador H2, sobre una base en memoria. */
class RepositorioEventosH2EquipoTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 11, 28, 19, 0);
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 20, 10, 0);

    private Connection conexion;
    private RepositorioEventosH2 repositorio;

    @BeforeEach
    void abrirBase() throws SQLException {
        conexion = DriverManager.getConnection(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        repositorio = new RepositorioEventosH2(conexion);
    }

    @AfterEach
    void cerrarBase() throws SQLException {
        conexion.close();
    }

    @Test
    void buscarUnEventoQueNoExisteDevuelveNull() {
        assertNull(repositorio.buscar("no-existe"));
    }

    @Test
    void guardaYBuscaUnEventoSinReservas() {
        repositorio.guardar(new Evento("ev-1", "Concierto", FECHA, "Auditorio", 10, 2));

        Evento encontrado = repositorio.buscar("ev-1");

        assertEquals("ev-1", encontrado.getId());
        assertEquals("Concierto", encontrado.getNombre());
        assertEquals(FECHA, encontrado.getFecha());
        assertEquals("Auditorio", encontrado.getLugar());
        assertEquals(10, encontrado.getAforo());
        assertEquals(2, encontrado.getBloqueados());
        assertEquals(0, encontrado.getReservas().size());
    }

    @Test
    void buscarDevuelveUnObjetoNuevoReconstruidoDesdeLaBase() {
        Evento original = new Evento("ev-1", "Concierto", FECHA, "Auditorio", 10, 0);
        repositorio.guardar(original);

        assertNotSame(original, repositorio.buscar("ev-1"));
    }

    @Test
    void conservaLasReservasConSuEstadoYVencimiento() {
        Evento evento = new Evento("ev-1", "Concierto", FECHA, "Auditorio", 10, 0);
        Reserva reserva = evento.reservar("est-1", AHORA);
        reserva.confirmar(AHORA.plusMinutes(1));
        repositorio.guardar(evento);

        Reserva leida = repositorio.buscar("ev-1").getReservas().get(0);

        assertEquals(reserva.getCodigo(), leida.getCodigo());
        assertEquals("est-1", leida.getEstudianteId());
        assertEquals(EstadoReserva.CONFIRMADA, leida.getEstado());
        assertEquals(reserva.getVenceEn(), leida.getVenceEn());
    }

    @Test
    void conservaLaAsistenciaDeUnaReservaQueYaIngreso() {
        Evento evento = new Evento("ev-1", "Concierto", FECHA, "Auditorio", 10, 0);
        Reserva reserva = evento.reservar("est-1", AHORA);
        reserva.confirmar(AHORA.plusMinutes(1));
        reserva.registrarIngreso(AHORA.plusMinutes(2));
        repositorio.guardar(evento);

        Reserva leida = repositorio.buscar("ev-1").getReservas().get(0);

        assertEquals(AHORA.plusMinutes(2), leida.getAsistencia().getHora());
    }

    @Test
    void guardarDosVecesElMismoEventoLoActualizaSinDuplicarReservas() {
        Evento evento = new Evento("ev-1", "Concierto", FECHA, "Auditorio", 10, 0);
        evento.reservar("est-1", AHORA);
        repositorio.guardar(evento);

        evento.cambiarDatos("Concierto de cierre", FECHA, "Plazoleta");
        repositorio.guardar(evento);

        Evento leido = repositorio.buscar("ev-1");
        assertEquals("Concierto de cierre", leido.getNombre());
        assertEquals("Plazoleta", leido.getLugar());
        assertEquals(1, leido.getReservas().size());
    }

    @Test
    void crearElRepositorioDosVecesSobreLaMismaBaseNoFalla() {
        assertDoesNotThrow(() -> new RepositorioEventosH2(conexion));
    }

    @Test
    void unaConexionNulaSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> new RepositorioEventosH2(null));
    }
}
