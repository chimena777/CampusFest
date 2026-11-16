package co.edu.unillanos.campusfest.infrastructure.ui;

import java.time.format.DateTimeFormatter;

import co.edu.unillanos.campusfest.application.usecase.RegistrarIngreso;
import co.edu.unillanos.campusfest.domain.Asistencia;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * CU4 · Pantalla del encargado de la entrada. El lector de QR escribe el código
 * en campoCodigo como si fuera un teclado y envía Enter, que dispara alRegistrar.
 */
public class PantallaIngresoController {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final RegistrarIngreso registrarIngreso;

    @FXML
    private TextField campoEventoId;

    @FXML
    private TextField campoCodigo;

    @FXML
    private Label etiquetaMensaje;

    public PantallaIngresoController(RegistrarIngreso registrarIngreso) {
        this.registrarIngreso = registrarIngreso;
    }

    @FXML
    private void alRegistrar() {
        try {
            Asistencia asistencia = registrarIngreso.registrar(
                    campoEventoId.getText().trim(), campoCodigo.getText().trim());
            etiquetaMensaje.setText("Ingreso registrado a las "
                    + asistencia.getHora().format(FORMATO_HORA) + ".");
            campoCodigo.clear();
        } catch (IllegalArgumentException | IllegalStateException e) {
            etiquetaMensaje.setText(e.getMessage());
        }
    }
}