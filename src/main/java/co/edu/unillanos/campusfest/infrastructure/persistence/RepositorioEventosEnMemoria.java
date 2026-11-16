package co.edu.unillanos.campusfest.infrastructure.persistence;

import java.util.HashMap;
import java.util.Map;

import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Evento;

/** Adaptador en memoria del puerto RepositorioEventos. */
public class RepositorioEventosEnMemoria implements RepositorioEventos {

    private final Map<String, Evento> eventos = new HashMap<>();

    @Override
    public Evento buscar(String eventoId) {
        return eventos.get(eventoId);
    }

    @Override
    public void guardar(Evento evento) {
        eventos.put(evento.getId(), evento);
    }
}