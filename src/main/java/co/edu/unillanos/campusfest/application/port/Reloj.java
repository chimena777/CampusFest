package co.edu.unillanos.campusfest.application.port;

import java.time.LocalDateTime;

/**
 * Puerto que entrega la hora actual (Variaciones protegidas e Indirección).
 * Las entidades y los casos de uso nunca llaman a now().
 */
public interface Reloj {

    LocalDateTime ahora();
}
