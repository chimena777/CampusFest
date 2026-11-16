package co.edu.unillanos.campusfest.infrastructure.ui;

import java.time.format.DateTimeFormatter;

import co.edu.unillanos.campusfest.application.usecase.ReservarEntrada;
import co.edu.unillanos.campusfest.domain.Reserva;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** CU2 · Pantalla para solicitar una reserva y confirmarla antes de 10 minutos. */
public class PantallaReservaController {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ReservarEntrada reservarEntrada;

    @FXML
    private TextField campoEventoId;

    @FXML
    private TextField campoEstudianteId;

    @FXML
    private TextField campoCodigo;

    @FXML
    private Label etiquetaMensaje;

    public PantallaReservaController(ReservarEntrada reservarEntrada) {
        this.reservarEntrada = reservarEntrada;
    }

    @FXML
    private void alSolicitar() {
        try {
            Reserva reserva = reservarEntrada.solicitarReserva(
                    campoEventoId.getText().trim(), campoEstudianteId.getText().trim());
            campoCodigo.setText(reserva.getCodigo());
            etiquetaMensaje.setText("Reserva pendiente creada. Confírmela antes de las "
                    + reserva.getVenceEn().format(FORMATO_HORA) + ".");
        } catch (IllegalArgumentException | IllegalStateException e) {
            etiquetaMensaje.setText(e.getMessage());
        }
    }

    @FXML
    private void alConfirmar() {
        try {
            Reserva reserva = reservarEntrada.confirmarReserva(
                    campoEventoId.getText().trim(), campoCodigo.getText().trim());
            etiquetaMensaje.setText("Reserva confirmada. Su código QR es: " + reserva.getCodigo());
        } catch (IllegalArgumentException | IllegalStateException e) {
            etiquetaMensaje.setText(e.getMessage());
        }
    }
}