package co.edu.unillanos.campusfest.infrastructure.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import co.edu.unillanos.campusfest.application.usecase.EditarEvento;
import co.edu.unillanos.campusfest.domain.Evento;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** CU3 · Pantalla para que el organizador edite los datos y el aforo de un evento. */
public class PantallaEventoController {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final EditarEvento editarEvento;

    @FXML
    private TextField campoEventoId;

    @FXML
    private TextField campoNombre;

    @FXML
    private TextField campoFecha;

    @FXML
    private TextField campoLugar;

    @FXML
    private TextField campoAforo;

    @FXML
    private Label etiquetaBloqueados;

    @FXML
    private Label etiquetaMensaje;

    public PantallaEventoController(EditarEvento editarEvento) {
        this.editarEvento = editarEvento;
    }

    @FXML
    private void alCargar() {
        try {
            Evento evento = editarEvento.obtener(campoEventoId.getText().trim());
            campoNombre.setText(evento.getNombre());
            campoFecha.setText(evento.getFecha().format(FORMATO_FECHA));
            campoLugar.setText(evento.getLugar());
            campoAforo.setText(String.valueOf(evento.getAforo()));
            etiquetaBloqueados.setText("Cupos bloqueados: " + evento.getBloqueados());
            etiquetaMensaje.setText("Evento cargado.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            etiquetaMensaje.setText(e.getMessage());
        }
    }

    @FXML
    private void alGuardar() {
        try {
            LocalDateTime fecha = LocalDateTime.parse(campoFecha.getText().trim(), FORMATO_FECHA);
            int aforo = Integer.parseInt(campoAforo.getText().trim());
            editarEvento.editar(campoEventoId.getText().trim(), campoNombre.getText(), fecha,
                    campoLugar.getText(), aforo);
            etiquetaMensaje.setText("Evento actualizado.");
        } catch (DateTimeParseException e) {
            etiquetaMensaje.setText("La fecha debe tener el formato aaaa-mm-dd hh:mm.");
        } catch (NumberFormatException e) {
            etiquetaMensaje.setText("El aforo debe ser un número entero.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            etiquetaMensaje.setText(e.getMessage());
        }
    }
}