package co.edu.unillanos.campusfest.infrastructure.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Asistencia;
import co.edu.unillanos.campusfest.domain.EstadoReserva;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.domain.Reserva;

/**
 * Adaptador H2 del puerto RepositorioEventos.
 *
 * Recibe una conexión abierta y crea las tablas con db/schema.sql.
 * No aplica reglas del negocio: solo guarda y reconstruye.
 */
public class RepositorioEventosH2 implements RepositorioEventos {

    private static final String RUTA_ESQUEMA = "/db/schema.sql";

    private final Connection conexion;

    public RepositorioEventosH2(Connection conexion) {
        if (conexion == null) {
            throw new IllegalArgumentException("La conexión es obligatoria.");
        }
        this.conexion = conexion;
        crearTablas();
    }

    @Override
    public Evento buscar(String eventoId) {
        String nombre;
        Timestamp fecha;
        String lugar;
        int aforo;
        int bloqueados;
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT NOMBRE, FECHA, LUGAR, AFORO, BLOQUEADOS FROM EVENTO WHERE ID = ?")) {
            ps.setString(1, eventoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                nombre = rs.getString("NOMBRE");
                fecha = rs.getTimestamp("FECHA");
                lugar = rs.getString("LUGAR");
                aforo = rs.getInt("AFORO");
                bloqueados = rs.getInt("BLOQUEADOS");
            }
            List<Reserva> reservas = leerReservas(eventoId);
            return new Evento(eventoId, nombre, fecha.toLocalDateTime(), lugar,
                    aforo, bloqueados, reservas);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo leer el evento " + eventoId + ".", e);
        }
    }

    @Override
    public void guardar(Evento evento) {
        if (evento == null) {
            throw new IllegalArgumentException("El evento es obligatorio.");
        }
        boolean autoCommit = true;
        try {
            autoCommit = conexion.getAutoCommit();
            conexion.setAutoCommit(false);
            guardarEvento(evento);
            borrarReservas(evento.getId());
            insertarReservas(evento);
            conexion.commit();
        } catch (SQLException e) {
            revertir();
            throw new IllegalStateException(
                    "No se pudo guardar el evento " + evento.getId() + ".", e);
        } finally {
            restaurarAutoCommit(autoCommit);
        }
    }

    private void crearTablas() {
        try (InputStream in = getClass().getResourceAsStream(RUTA_ESQUEMA)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el esquema " + RUTA_ESQUEMA + ".");
            }
            String script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            try (Statement st = conexion.createStatement()) {
                for (String sentencia : script.split(";")) {
                    if (!sentencia.isBlank()) {
                        st.execute(sentencia);
                    }
                }
            }
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("No se pudo crear el esquema de la base de datos.", e);
        }
    }

    private List<Reserva> leerReservas(String eventoId) throws SQLException {
        String sql = "SELECT R.CODIGO, R.ESTUDIANTE_ID, R.VENCE_EN, R.ESTADO, A.HORA "
                + "FROM RESERVA R LEFT JOIN ASISTENCIA A ON A.CODIGO_RESERVA = R.CODIGO "
                + "WHERE R.EVENTO_ID = ? ORDER BY R.POSICION";
        List<Reserva> reservas = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, eventoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp hora = rs.getTimestamp("HORA");
                    Asistencia asistencia =
                            hora == null ? null : new Asistencia(hora.toLocalDateTime());
                    reservas.add(Reserva.reconstruir(
                            rs.getString("CODIGO"),
                            rs.getString("ESTUDIANTE_ID"),
                            rs.getTimestamp("VENCE_EN").toLocalDateTime(),
                            EstadoReserva.valueOf(rs.getString("ESTADO")),
                            asistencia));
                }
            }
        }
        return reservas;
    }

    private void guardarEvento(Evento evento) throws SQLException {
        String sql = "MERGE INTO EVENTO (ID, NOMBRE, FECHA, LUGAR, AFORO, BLOQUEADOS) "
                + "KEY (ID) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, evento.getId());
            ps.setString(2, evento.getNombre());
            ps.setTimestamp(3, Timestamp.valueOf(evento.getFecha()));
            ps.setString(4, evento.getLugar());
            ps.setInt(5, evento.getAforo());
            ps.setInt(6, evento.getBloqueados());
            ps.executeUpdate();
        }
    }

    private void borrarReservas(String eventoId) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(
                "DELETE FROM ASISTENCIA WHERE CODIGO_RESERVA IN "
                        + "(SELECT CODIGO FROM RESERVA WHERE EVENTO_ID = ?)")) {
            ps.setString(1, eventoId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conexion.prepareStatement(
                "DELETE FROM RESERVA WHERE EVENTO_ID = ?")) {
            ps.setString(1, eventoId);
            ps.executeUpdate();
        }
    }

    private void insertarReservas(Evento evento) throws SQLException {
        String sqlReserva = "INSERT INTO RESERVA "
                + "(CODIGO, EVENTO_ID, ESTUDIANTE_ID, VENCE_EN, ESTADO, POSICION) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlAsistencia = "INSERT INTO ASISTENCIA (CODIGO_RESERVA, HORA) VALUES (?, ?)";
        int posicion = 0;
        for (Reserva reserva : evento.getReservas()) {
            try (PreparedStatement ps = conexion.prepareStatement(sqlReserva)) {
                ps.setString(1, reserva.getCodigo());
                ps.setString(2, evento.getId());
                ps.setString(3, reserva.getEstudianteId());
                ps.setTimestamp(4, Timestamp.valueOf(reserva.getVenceEn()));
                ps.setString(5, reserva.getEstado().name());
                ps.setInt(6, posicion++);
                ps.executeUpdate();
            }
            if (reserva.getAsistencia() != null) {
                try (PreparedStatement ps = conexion.prepareStatement(sqlAsistencia)) {
                    ps.setString(1, reserva.getCodigo());
                    ps.setTimestamp(2, Timestamp.valueOf(reserva.getAsistencia().getHora()));
                    ps.executeUpdate();
                }
            }
        }
    }

    private void revertir() {
        try {
            conexion.rollback();
        } catch (SQLException ignorada) {
            // si la conexión ya falló, no hay nada más que revertir
        }
    }

    private void restaurarAutoCommit(boolean autoCommit) {
        try {
            conexion.setAutoCommit(autoCommit);
        } catch (SQLException ignorada) {
            // la conexión pudo cerrarse; el error real ya se informó
        }
    }
}