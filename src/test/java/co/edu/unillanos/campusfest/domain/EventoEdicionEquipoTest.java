package co.edu.unillanos.campusfest.domain; 
import static org.junit.jupiter.api.Assertions.assertEquals; 
import static org.junit.jupiter.api.Assertions.assertThrows; 
import java.time.LocalDateTime; import org.junit.jupiter.api.BeforeEach; 
import org.junit.jupiter.api.Test; 
/** Pruebas del equipo: la edición completa no deja el evento a medias. */ 
class EventoEdicionEquipoTest { 
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 1, 10, 0); 
    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 11, 28, 19, 0); 
    private Evento evento;
    @BeforeEach
    void preparar() {
    evento = new Evento(
            "ev-1",
            "Concierto de bienvenida",
            FECHA,
            "Auditorio Barcelona",
            10,
            2);

            evento.reservar("est-1", AHORA)
            .confirmar(AHORA);
    }

    @Test
    void cambiarAforoYDatosDeEvento() {
        evento.cambiarAforo(15, AHORA);
        evento.cambiarDatos("Concierto de cierre", FECHA.plusDays(1), "Plazoleta central");

        assertEquals(15, evento.getAforo());
        assertEquals("Concierto de cierre", evento.getNombre());
        assertEquals(FECHA.plusDays(1), evento.getFecha());
        assertEquals("Plazoleta central", evento.getLugar());
    }

    @Test
    void cambiarAforoYDatosDeEventoConErrorNoDejaElEventoAMedias() {

        IllegalArgumentException ex =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> evento.editarDatosYAforo(
                            null,
                            FECHA.plusDays(1),
                            "Plazoleta central",
                            5,
                            AHORA));

                assertEquals(
                "El nombre del evento es obligatorio.",
                ex.getMessage());

        assertEquals(10, evento.getAforo());
        assertEquals("Concierto de bienvenida", evento.getNombre());
        assertEquals(FECHA, evento.getFecha());
        assertEquals("Auditorio Barcelona", evento.getLugar());
    }

    @Test 
    void unNombreInvalidoNoCambiaNingunDato() { 

        assertThrows(IllegalArgumentException.class, ()-> evento.editarDatosYAforo(" ", FECHA.plusDays(1), "Coliseo", 8, AHORA)); 
        assertEquals("Concierto de bienvenida", evento.getNombre()); 
        assertEquals(FECHA, evento.getFecha()); 
        assertEquals("Auditorio Barcelona", evento.getLugar()); 
        assertEquals(10, evento.getAforo()); 
    }

    @Test 
    void unAforoInvalidoNoCambiaNingunDato() {

    assertThrows(
            IllegalStateException.class,
            () -> evento.editarDatosYAforo(
                    "Nuevo",
                    FECHA.plusDays(1),
                    "Coliseo",
                    2,
                    AHORA));

        assertEquals("Concierto de bienvenida", evento.getNombre());
        assertEquals(FECHA, evento.getFecha());
        assertEquals("Auditorio Barcelona", evento.getLugar());
        assertEquals(10, evento.getAforo());
    }

    @Test 
    void unaEdicionValidaCambiaTodoJunto() { 
        evento.editarDatosYAforo("Nuevo", FECHA.plusDays(1), "Coliseo", 3, AHORA); 
        assertEquals("Nuevo", evento.getNombre()); 
        assertEquals(FECHA.plusDays(1), evento.getFecha()); 
        assertEquals("Coliseo", evento.getLugar()); 
        assertEquals(3, evento.getAforo()); 
    } 
    
}
