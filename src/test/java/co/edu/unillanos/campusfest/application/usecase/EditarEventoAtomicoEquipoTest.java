package co.edu.unillanos.campusfest.application.usecase;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.unillanos.campusfest.domain.Evento;

class EditarEventoAtomicoEquipoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 20, 18, 0);
    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 11, 28, 19, 0);

    private RepositorioEventosFalso repositorio;
    private Evento evento;
    private EditarEvento caso;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioEventosFalso();
        evento = new Evento("ev-1", "Concierto", FECHA, "Auditorio", 10, 2);
        evento.reservar("est-1", AHORA).confirmar(AHORA);
        repositorio.agregar(evento);
        caso = new EditarEvento(repositorio, new RelojFijo(AHORA));
    }

    private void comprobarRechazo() {
        assertEquals("Concierto", evento.getNombre());
        assertEquals(FECHA, evento.getFecha());
        assertEquals("Auditorio", evento.getLugar());
        assertEquals(10, evento.getAforo());
        assertEquals(0, repositorio.getVecesGuardado());
    }

    @Test
    void nombreInvalidoNoCambiaNiGuarda() {
        assertThrows(IllegalArgumentException.class,
                () -> caso.editar("ev-1", "", FECHA.plusDays(1), "Coliseo", 8));
        comprobarRechazo();
    }

    @Test
    void fechaNulaNoCambiaNiGuarda() {
        assertThrows(IllegalArgumentException.class,
                () -> caso.editar("ev-1", "Nuevo", null, "Coliseo", 8));
        comprobarRechazo();
    }

    @Test
    void lugarInvalidoNoCambiaNiGuarda() {
        assertThrows(IllegalArgumentException.class,
                () -> caso.editar("ev-1", "Nuevo", FECHA.plusDays(1), "", 8));
        comprobarRechazo();
    }

    @Test
    void aforoInvalidoNoCambiaNiGuarda() {
        assertThrows(IllegalStateException.class,
                () -> caso.editar("ev-1", "Nuevo", FECHA.plusDays(1), "Coliseo", 2));
        comprobarRechazo();
    }

    @Test
    void edicionValidaGuardaUnaVez() {
        caso.editar("ev-1", "Nuevo", FECHA.plusDays(1), "Coliseo", 3);
        assertEquals("Nuevo", evento.getNombre());
        assertEquals(FECHA.plusDays(1), evento.getFecha());
        assertEquals("Coliseo", evento.getLugar());
        assertEquals(3, evento.getAforo());
        assertEquals(1, repositorio.getVecesGuardado());
    }
}