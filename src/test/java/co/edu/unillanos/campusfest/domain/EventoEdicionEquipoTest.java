package co.edu.unillanos.campusfest.domain;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventoEdicionEquipoTest {
    private static final LocalDateTime AHORA =
            LocalDateTime.of(2026, 11, 20, 18, 0);
    private static final LocalDateTime FECHA =
            LocalDateTime.of(2026, 11, 28, 19, 0);
    private Evento evento;

    @BeforeEach
    void preparar() {
        evento = new Evento("ev-1", "Concierto", FECHA, "Auditorio", 10, 2);
        evento.reservar("est-1", AHORA).confirmar(AHORA);
    }

    private void comprobarOriginal() {
        assertEquals("Concierto", evento.getNombre());
        assertEquals(FECHA, evento.getFecha());
        assertEquals("Auditorio", evento.getLugar());
        assertEquals(10, evento.getAforo());
    }

    @Test
    void nombreInvalidoNoCambiaNada() {
        assertThrows(IllegalArgumentException.class,
                () -> evento.editarDatosYAforo(" ", FECHA.plusDays(1),
                        "Coliseo", 8, AHORA));
        comprobarOriginal();
    }

    @Test
    void fechaNulaNoCambiaNada() {
        assertThrows(IllegalArgumentException.class,
                () -> evento.editarDatosYAforo("Nuevo", null,
                        "Coliseo", 8, AHORA));
        comprobarOriginal();
    }

    @Test
    void lugarInvalidoNoCambiaNada() {
        assertThrows(IllegalArgumentException.class,
                () -> evento.editarDatosYAforo("Nuevo", FECHA.plusDays(1),
                        " ", 8, AHORA));
        comprobarOriginal();
    }

    @Test
    void aforoInvalidoNoCambiaNada() {
        assertThrows(IllegalStateException.class,
                () -> evento.editarDatosYAforo("Nuevo", FECHA.plusDays(1),
                        "Coliseo", 2, AHORA));
        comprobarOriginal();
    }

    @Test
    void edicionValidaCambiaTodo() {
        evento.editarDatosYAforo(" Nuevo ", FECHA.plusDays(1),
                " Coliseo ", 3, AHORA);
        assertEquals("Nuevo", evento.getNombre());
        assertEquals(FECHA.plusDays(1), evento.getFecha());
        assertEquals("Coliseo", evento.getLugar());
        assertEquals(3, evento.getAforo());
    }
}