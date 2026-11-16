package co.edu.unillanos.campusfest;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;

import co.edu.unillanos.campusfest.application.port.Reloj;
import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.application.usecase.ConsultarCupos;
import co.edu.unillanos.campusfest.application.usecase.EditarEvento;
import co.edu.unillanos.campusfest.application.usecase.RegistrarIngreso;
import co.edu.unillanos.campusfest.application.usecase.ReservarEntrada;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.infrastructure.persistence.RepositorioEventosH2;
import co.edu.unillanos.campusfest.infrastructure.reloj.RelojDelSistema;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaCuposController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaEventoController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaIngresoController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaReservaController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * Ensamblaje de la aplicación (anillo de frameworks y drivers).
 * Es la única clase que crea las implementaciones de los puertos, los casos
 * de uso y los controladores, y la única que conoce la URL de H2.
 */
public class App extends Application {

    private static final String URL_BASE_DE_DATOS = "jdbc:h2:./campusfest-db";
    private static final String CARPETA_VISTAS = "vistas/";

    private Connection conexion;

    @Override
    public void start(Stage ventana) throws IOException, SQLException {
        conexion = DriverManager.getConnection(URL_BASE_DE_DATOS, "sa", "");
        RepositorioEventos repositorio = new RepositorioEventosH2(conexion);
        Reloj reloj = new RelojDelSistema();
        cargarEventosDeEjemplo(repositorio);

        ConsultarCupos consultarCupos = new ConsultarCupos(repositorio, reloj);
        ReservarEntrada reservarEntrada = new ReservarEntrada(repositorio, reloj);
        EditarEvento editarEvento = new EditarEvento(repositorio, reloj);
        RegistrarIngreso registrarIngreso = new RegistrarIngreso(repositorio, reloj);

        PantallaCuposController pantallaCupos = new PantallaCuposController(consultarCupos);
        PantallaReservaController pantallaReserva = new PantallaReservaController(reservarEntrada);
        PantallaEventoController pantallaEvento = new PantallaEventoController(editarEvento);
        PantallaIngresoController pantallaIngreso = new PantallaIngresoController(registrarIngreso);

        TabPane pestanas = new TabPane();
        pestanas.getTabs().add(crearPestana("CU1 · Consultar cupos",
                cargarVista("pantalla-cupos.fxml", pantallaCupos)));
        pestanas.getTabs().add(crearPestana("CU2 · Reservar entrada",
                cargarVista("pantalla-reserva.fxml", pantallaReserva)));
        pestanas.getTabs().add(crearPestana("CU3 · Editar evento",
                cargarVista("pantalla-evento.fxml", pantallaEvento)));
        pestanas.getTabs().add(crearPestana("CU4 · Registrar ingreso",
                cargarVista("pantalla-ingreso.fxml", pantallaIngreso)));

        Scene escena = new Scene(pestanas, 720, 480);
        escena.getStylesheets().add(
                App.class.getResource(CARPETA_VISTAS + "campusfest.css").toExternalForm());
        ventana.setTitle("CampusFest");
        ventana.setScene(escena);
        ventana.show();
    }

    @Override
    public void stop() throws SQLException {
        if (conexion != null) {
            conexion.close();
        }
    }

    private Parent cargarVista(String archivo, Object controlador) throws IOException {
        FXMLLoader cargador = new FXMLLoader(App.class.getResource(CARPETA_VISTAS + archivo));
        cargador.setController(controlador);
        return cargador.load();
    }

    private Tab crearPestana(String titulo, Parent contenido) {
        Tab pestana = new Tab(titulo, contenido);
        pestana.setClosable(false);
        return pestana;
    }

    /** Deja dos eventos de prueba la primera vez que se abre la base de datos. */
    private void cargarEventosDeEjemplo(RepositorioEventos repositorio) {
        if (repositorio.buscar("ev-1") == null) {
            repositorio.guardar(new Evento("ev-1", "Concierto de bienvenida",
                    LocalDateTime.of(2026, 11, 28, 19, 0), "Auditorio Barcelona", 120, 5));
        }
        if (repositorio.buscar("ev-2") == null) {
            repositorio.guardar(new Evento("ev-2", "Feria de emprendimiento",
                    LocalDateTime.of(2026, 12, 3, 9, 0), "Coliseo", 50, 0));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}