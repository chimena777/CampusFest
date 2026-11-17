# CampusFest

Aplicación JavaFX para consultar cupos, reservar y confirmar entradas, editar eventos y registrar el ingreso con código QR. Proyecto de semestre de Unillanos, implementado con arquitectura limpia, principios de diseño y patrones GRASP.

## Integrantes

| Integrante | Parte desarrollada |
| --- | --- |
| Yurley Ximena Lozano | Dominio y reglas del negocio: `Evento`, `Reserva`, `EstadoReserva`, `Asistencia` |
| Looana Marinella Leon Guevara | Casos de uso y puertos: `RepositorioEventos`, `Reloj`, los cuatro casos de uso y `BuscadorDeEventos` |
| Laura Geraldine Beltran Cediel | Interfaz JavaFX e integración: controladores, FXML, CSS, `App` y `Launcher` |
| Derlys Xiomara Leal Castillo | Persistencia e infraestructura: `RepositorioEventosEnMemoria`, `RepositorioEventosH2`, `RelojDelSistema`, `schema.sql` y `pom.xml` |

## Cómo ejecutar

Requisitos: Java 21 y Maven 3.9 o superior.

```
mvn test          # compila y ejecuta las pruebas (90 de los docentes y las propias del equipo)
mvn javafx:run    # abre la aplicación
```

La base H2 se guarda en el archivo `campusfest-db.mv.db`, en la carpeta desde la que se ejecuta la aplicación, y se crea sola la primera vez con dos eventos de ejemplo: `ev-1` (Concierto de bienvenida, aforo 120, 5 cupos bloqueados) y `ev-2` (Feria de emprendimiento, aforo 50).

Las pruebas de los docentes están sin modificar en `src/test/java`. Las pruebas propias del equipo están en archivos separados, cuyo nombre termina en `EquipoTest`.

Para probar: en la pestaña **CU2 · Reservar entrada** solicite una reserva para `ev-1`, confírmela antes de 10 minutos, copie el código y péguelo en **CU4 · Registrar ingreso**.

## Arquitectura

| Anillo | Paquete | Clases |
| --- | --- | --- |
| Entidades | `co.edu.unillanos.campusfest.domain` | `Evento`, `Reserva`, `EstadoReserva`, `Asistencia` |
| Casos de uso | `...application.usecase` y `...application.port` | `ConsultarCupos`, `ReservarEntrada`, `EditarEvento`, `RegistrarIngreso`; puertos `RepositorioEventos` y `Reloj` |
| Adaptadores | `...infrastructure.persistence`, `.ui`, `.reloj` | `RepositorioEventosEnMemoria`, `RepositorioEventosH2`, `RelojDelSistema`, cuatro controladores |
| Frameworks y drivers | `co.edu.unillanos.campusfest` | `App`, `Launcher`, FXML, CSS y `db/schema.sql` |

`App` es la única clase que crea las implementaciones de los puertos. Pasar de `RepositorioEventosEnMemoria` a `RepositorioEventosH2` solo cambió `App.java` (commits `c5e1cca` y `510ce74`).

## Casos de uso implementados

| Caso de uso | Clase | Operaciones | Pantalla |
| --- | --- | --- | --- |
| CU1 Consultar cupos | `ConsultarCupos` | `consultar` | `pantalla-cupos.fxml` |
| CU2 Reservar y confirmar una entrada | `ReservarEntrada` | `solicitarReserva`, `confirmarReserva` | `pantalla-reserva.fxml` |
| CU3 Editar evento | `EditarEvento` | `obtener`, `editar` | `pantalla-evento.fxml` |
| CU4 Registrar el ingreso con QR | `RegistrarIngreso` | `registrar` | `pantalla-ingreso.fxml` |

Flujos alternativos cubiertos: sin cupos y reserva vencida (CU2); aforo menor que los cupos ocupados (CU3); código de otro evento, reserva no confirmada e ingreso repetido (CU4).

## Declaración de uso de IA

**Herramientas y para qué se usaron.** Se usó Claude (Anthropic) para repartir el trabajo entre los cuatro integrantes, generar el primer borrador de las clases y de las pantallas, y revisar el cumplimiento de las guías. Clases en las que intervino: `Reserva`, `Evento`, `Asistencia`, `EstadoReserva`, los cuatro casos de uso, `BuscadorDeEventos`, `RepositorioEventosEnMemoria`, `RepositorioEventosH2`, `schema.sql`, los cuatro controladores, los FXML, el CSS, `App`, `Launcher` y este README. Cada integrante revisó, entendió y probó su parte antes de integrarla. Las pruebas entregadas por los docentes no se modificaron.

