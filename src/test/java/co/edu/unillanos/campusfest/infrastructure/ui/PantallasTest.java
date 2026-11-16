package co.edu.unillanos.campusfest.infrastructure.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 * Pruebas del equipo sobre las reglas de las pantallas del contrato.
 * Leen los FXML, el CSS y el código fuente: no abren ventanas.
 */
class PantallasEquipoTest {

    private static final String VISTAS = "src/main/resources/co/edu/unillanos/campusfest/vistas/";
    private static final String UI = "src/main/java/co/edu/unillanos/campusfest/infrastructure/ui/";
    private static final String RAIZ = "src/main/java/co/edu/unillanos/campusfest/";

    private static final String[][] PANTALLAS = {
        {"pantalla-cupos.fxml", "PantallaCuposController", "ConsultarCupos", "consultar("},
        {"pantalla-reserva.fxml", "PantallaReservaController", "ReservarEntrada",
            "solicitarReserva(", "confirmarReserva("},
        {"pantalla-evento.fxml", "PantallaEventoController", "EditarEvento", "obtener(", "editar("},
        {"pantalla-ingreso.fxml", "PantallaIngresoController", "RegistrarIngreso", "registrar("}
    };

    private static final String[] PROHIBIDOS_EN_CONTROLADOR = {
        "cuposDisponibles(", "ocupaCupo(", "getEstado()", ".reservar(", ".confirmar(",
        ".cancelar(", ".registrarIngreso(", "cambiarAforo(", "cambiarDatos(",
        "import co.edu.unillanos.campusfest.application.port.",
        "import co.edu.unillanos.campusfest.infrastructure.persistence.",
        "import co.edu.unillanos.campusfest.infrastructure.reloj.",
        "import java.sql.", "new ConsultarCupos(", "new ReservarEntrada(",
        "new EditarEvento(", "new RegistrarIngreso("
    };

    @Test
    void existenLasCuatroVistasYSusControladores() {
        for (String[] pantalla : PANTALLAS) {
            assertTrue(new File(VISTAS + pantalla[0]).isFile(), "Falta " + pantalla[0]);
            assertTrue(new File(UI + pantalla[1] + ".java").isFile(), "Falta " + pantalla[1]);
        }
    }

    @Test
    void ningunFxmlDeclaraElControlador() throws IOException {
        for (String[] pantalla : PANTALLAS) {
            assertFalse(leer(VISTAS + pantalla[0]).contains("fx:controller"), pantalla[0]);
        }
    }

    @Test
    void cadaFxIdEsUnCampoPrivadoConFxml() throws IOException {
        for (String[] pantalla : PANTALLAS) {
            String codigo = leer(UI + pantalla[1] + ".java");
            for (String id : buscarTodos(leer(VISTAS + pantalla[0]), "fx:id=\"(\\w+)\"")) {
                Pattern campo = Pattern.compile("@FXML\\s+private\\s+\\w+\\s+" + id + "\\s*;");
                assertTrue(campo.matcher(codigo).find(), pantalla[1] + " no declara " + id);
            }
        }
    }

    @Test
    void cadaAccionEsUnMetodoConFxmlYHayAlMenosUna() throws IOException {
        for (String[] pantalla : PANTALLAS) {
            String codigo = leer(UI + pantalla[1] + ".java");
            List<String> acciones = buscarTodos(leer(VISTAS + pantalla[0]), "onAction=\"#(\\w+)\"");
            assertFalse(acciones.isEmpty(), pantalla[0] + " no tiene acciones");
            for (String accion : acciones) {
                Pattern metodo = Pattern.compile("@FXML\\s+(private|public|protected)?\\s*void\\s+"
                        + accion + "\\s*\\(");
                assertTrue(metodo.matcher(codigo).find(), pantalla[1] + " no tiene " + accion);
            }
        }
    }

    @Test
    void cadaPantallaTieneEtiquetaMensaje() throws IOException {
        for (String[] pantalla : PANTALLAS) {
            assertTrue(leer(VISTAS + pantalla[0]).contains("<Label fx:id=\"etiquetaMensaje\""),
                    pantalla[0]);
        }
    }

    @Test
    void cadaControladorRecibeSuCasoDeUsoYLlamaASusOperaciones() throws IOException {
        for (String[] pantalla : PANTALLAS) {
            String codigo = leer(UI + pantalla[1] + ".java");
            assertTrue(codigo.contains("public " + pantalla[1] + "(" + pantalla[2] + " "),
                    pantalla[1] + " debe recibir " + pantalla[2]);
            for (int i = 3; i < pantalla.length; i++) {
                assertTrue(codigo.contains(pantalla[i]), pantalla[1] + " no llama " + pantalla[i]);
            }
        }
    }

    @Test
    void cadaControladorAtrapaElErrorYMuestraElMensaje() throws IOException {
        for (String[] pantalla : PANTALLAS) {
            String codigo = leer(UI + pantalla[1] + ".java");
            assertTrue(codigo.contains("catch ("), pantalla[1]);
            assertTrue(codigo.contains("etiquetaMensaje.setText(e.getMessage())"), pantalla[1]);
        }
    }

    @Test
    void losControladoresNoTienenReglasNiConocenElRepositorioNiElReloj() throws IOException {
        for (String[] pantalla : PANTALLAS) {
            String codigo = leer(UI + pantalla[1] + ".java");
            for (String prohibido : PROHIBIDOS_EN_CONTROLADOR) {
                assertFalse(codigo.contains(prohibido), pantalla[1] + " contiene " + prohibido);
            }
        }
    }

    @Test
    void elCssEstaEnVistasYSusPropiedadesUsanElPrefijoFx() throws IOException {
        String css = leer(VISTAS + "campusfest.css").replaceAll("(?s)/\\*.*?\\*/", "");
        List<String> propiedades = buscarTodos(css, "([\\w-]+)\\s*:[^;{}]*;");
        assertFalse(propiedades.isEmpty());
        for (String propiedad : propiedades) {
            assertTrue(propiedad.startsWith("-fx-"), "Propiedad sin -fx-: " + propiedad);
        }
    }

    @Test
    void appEnsamblaTodoConH2() throws IOException {
        String app = leer(RAIZ + "App.java");
        String[] esperados = {"new RepositorioEventosH2(", "new RelojDelSistema()",
            "new ConsultarCupos(", "new ReservarEntrada(", "new EditarEvento(",
            "new RegistrarIngreso(", "setController(", "DriverManager.getConnection("};
        for (String esperado : esperados) {
            assertTrue(app.contains(esperado), "App no contiene " + esperado);
        }
        for (String[] pantalla : PANTALLAS) {
            assertTrue(app.contains("new " + pantalla[1] + "("), "App no crea " + pantalla[1]);
            assertTrue(app.contains(pantalla[0]), "App no carga " + pantalla[0]);
        }
    }

    @Test
    void soloAppCreaLasImplementacionesDeLosPuertos() throws IOException {
        String[] implementaciones = {"new RepositorioEventosH2(", "new RepositorioEventosEnMemoria(",
            "new RelojDelSistema("};
        List<File> archivos = new ArrayList<>();
        recolectarJava(new File(RAIZ), archivos);
        for (File archivo : archivos) {
            if (archivo.getName().equals("App.java")) {
                continue;
            }
            String codigo = leer(archivo.getPath());
            for (String implementacion : implementaciones) {
                assertFalse(codigo.contains(implementacion),
                        archivo.getName() + " crea " + implementacion);
            }
        }
    }

    // ---------- Utilidades ----------

    private String leer(String ruta) throws IOException {
        return Files.readString(new File(ruta).toPath());
    }

    private List<String> buscarTodos(String texto, String expresion) {
        List<String> encontrados = new ArrayList<>();
        Matcher buscador = Pattern.compile(expresion).matcher(texto);
        while (buscador.find()) {
            encontrados.add(buscador.group(1));
        }
        return encontrados;
    }

    private void recolectarJava(File directorio, List<File> archivos) {
        File[] hijos = directorio.listFiles();
        if (hijos == null) {
            return;
        }
        for (File hijo : hijos) {
            if (hijo.isDirectory()) {
                recolectarJava(hijo, archivos);
            } else if (hijo.getName().endsWith(".java")) {
                archivos.add(hijo);
            }
        }
    }
}