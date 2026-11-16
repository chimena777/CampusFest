package co.edu.unillanos.campusfest.infrastructure.ui;

import co.edu.unillanos.campusfest.application.usecase.ConsultarCupos;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** CU1 · Pantalla para que el estudiante consulte los cupos de un evento. */
public class PantallaCuposController {

    private final ConsultarCupos consultarCupos;

    @FXML
    private TextField campoEventoId;

    @FXML
    private Label etiquetaMensaje;

    public PantallaCuposController(ConsultarCupos consultarCupos) {
        this.consultarCupos = consultarCupos;
    }

    @FXML
    private void alConsultar() {
        try {
            int cupos = consultarCupos.consultar(campoEventoId.getText().trim());
            etiquetaMensaje.setText("Cupos disponibles: " + cupos);
        } catch (IllegalArgumentException | IllegalStateException e) {
            etiquetaMensaje.setText(e.getMessage());
        }
    }
}