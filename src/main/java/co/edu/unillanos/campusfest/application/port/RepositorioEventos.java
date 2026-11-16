package co.edu.unillanos.campusfest.application.port;

import co.edu.unillanos.campusfest.domain.Evento;

/**
 * Puerto de salida para guardar y buscar eventos (Fabricación pura).
 * Se declara en el anillo de casos de uso y lo implementan los adaptadores.
 */
public interface RepositorioEventos {

    /** Devuelve el evento con sus reservas y asistencias, o null si no existe. */
    Evento buscar(String eventoId);

    /** Inserta o actualiza el evento con sus reservas y asistencias. */
    void guardar(Evento evento);
}
