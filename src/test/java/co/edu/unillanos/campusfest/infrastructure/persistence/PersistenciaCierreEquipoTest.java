
package co.edu.unillanos.campusfest.infrastructure.persistence;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import co.edu.unillanos.campusfest.application.port.Reloj;
import co.edu.unillanos.campusfest.application.usecase.EditarEvento;
import co.edu.unillanos.campusfest.application.usecase.RegistrarIngreso;
import co.edu.unillanos.campusfest.application.usecase.ReservarEntrada;
import co.edu.unillanos.campusfest.domain.EstadoReserva;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.domain.Reserva;

class PersistenciaCierreEquipoTest {

    @TempDir
    Path carpeta;

    private static final LocalDateTime AHORA =
            LocalDateTime.of(2026, 11, 20, 18, 0);

    private static final LocalDateTime FECHA =
            LocalDateTime.of(2026, 11, 28, 19, 0);

    private String url() {
        return "jdbc:h2:file:"
                + carpeta.resolve("cierre").toAbsolutePath().toString()
                        .replace('\\', '/');
    }

    private Connection abrir() throws Exception {
        return DriverManager.getConnection(url(), "sa", "");
    }

    private Evento evento(String id) {
        return new Evento(id, "Concierto", FECHA, "Auditorio", 10, 2);
    }

    @Test
    void conservaEdicionReservasYAsistenciaTrasReabrir() throws Exception {
        String codigo;
        LocalDateTime venceEn;
        Reloj reloj = () -> AHORA;

        try (Connection conexion = abrir()) {
            RepositorioEventosH2 repo =
                    new RepositorioEventosH2(conexion);

            repo.guardar(evento("ev-1"));

            ReservarEntrada reservar =
                    new ReservarEntrada(repo, reloj);

            Reserva reserva =
                    reservar.solicitarReserva("ev-1", "est-1");

            codigo = reserva.getCodigo();
            venceEn = reserva.getVenceEn();

            reservar.confirmarReserva("ev-1", codigo);

            new RegistrarIngreso(repo, reloj)
                    .registrar("ev-1", codigo);

            new EditarEvento(repo, reloj).editar(
                    "ev-1",
                    "Nuevo",
                    FECHA.plusDays(1),
                    "Coliseo",
                    11
            );
        }

        // Se cierra la primera conexión y se abre otra al mismo archivo.
        try (Connection conexion = abrir()) {
            RepositorioEventosH2 repo =
                    new RepositorioEventosH2(conexion);

            Evento recuperado = repo.buscar("ev-1");

            assertNotNull(recuperado);
            assertEquals("Nuevo", recuperado.getNombre());
            assertEquals(FECHA.plusDays(1), recuperado.getFecha());
            assertEquals("Coliseo", recuperado.getLugar());
            assertEquals(11, recuperado.getAforo());
            assertEquals(2, recuperado.getBloqueados());

            assertEquals(1, recuperado.getReservas().size());

            Reserva reserva =
                    recuperado.buscarReservaPorCodigo(codigo);

            assertEquals("est-1", reserva.getEstudianteId());
            assertEquals(venceEn, reserva.getVenceEn());
            assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
            assertNotNull(reserva.getAsistencia());
            assertEquals(AHORA, reserva.getAsistencia().getHora());

            assertEquals(8, recuperado.cuposDisponibles(AHORA));

            assertThrows(
                    IllegalStateException.class,
                    () -> new RegistrarIngreso(repo, reloj)
                            .registrar("ev-1", codigo)
            );

            assertEquals(
                    AHORA,
                    repo.buscar("ev-1")
                            .buscarReservaPorCodigo(codigo)
                            .getAsistencia()
                            .getHora()
            );
        }
    }

    @Test
    void guardarDosVecesNoDuplicaNiPierdeAsistencia() throws Exception {
        try (Connection conexion = abrir()) {
            RepositorioEventosH2 repo =
                    new RepositorioEventosH2(conexion);

            Evento original = evento("ev-1");

            Reserva reserva = original.reservar("est-1", AHORA);
            reserva.confirmar(AHORA);
            reserva.registrarIngreso(AHORA);

            repo.guardar(original);

            original.cambiarDatos(
                    "Actualizado",
                    FECHA,
                    "Coliseo"
            );

            repo.guardar(original);

            Evento leido = repo.buscar("ev-1");

            assertNotSame(original, leido);
            assertNotSame(leido, repo.buscar("ev-1"));

            assertEquals("Actualizado", leido.getNombre());
            assertEquals(1, leido.getReservas().size());

            assertEquals(
                    AHORA,
                    leido.buscarReservaPorCodigo(reserva.getCodigo())
                            .getAsistencia()
                            .getHora()
            );
        }
    }

    @Test
    void falloSqlRevierteEventoYConservaCausa() throws Exception {
        try (Connection conexion = abrir()) {
            RepositorioEventosH2 repo =
                    new RepositorioEventosH2(conexion);

            Evento primero = evento("ev-1");

            Reserva reserva = primero.reservar("est-1", AHORA);

            repo.guardar(primero);

            // Mismo código en otro evento: viola la clave primaria de RESERVA.
            Reserva duplicada = Reserva.reconstruir(
                    reserva.getCodigo(),
                    "est-2",
                    reserva.getVenceEn(),
                    EstadoReserva.PENDIENTE,
                    null
            );

            Evento segundo = new Evento(
                    "ev-2",
                    "Otro",
                    FECHA,
                    "Sala",
                    10,
                    0,
                    List.of(duplicada)
            );

            IllegalStateException error = assertThrows(
                    IllegalStateException.class,
                    () -> repo.guardar(segundo)
            );

            assertInstanceOf(
                    java.sql.SQLException.class,
                    error.getCause()
            );

            assertNull(repo.buscar("ev-2"));

            Evento intacto = repo.buscar("ev-1");

            assertEquals(1, intacto.getReservas().size());

            assertEquals(
                    "est-1",
                    intacto.buscarReservaPorCodigo(reserva.getCodigo())
                            .getEstudianteId()
            );

            assertTrue(conexion.getAutoCommit());
        }
    }
}
