# CampusFest

Aplicación JavaFX para consultar cupos, reservar y confirmar entradas, editar eventos y registrar el ingreso con código QR. Proyecto de semestre de Unillanos, implementado con arquitectura limpia, principios de diseño y patrones GRASP.

## Integrantes

| Integrante | Parte desarrollada |
| --- | --- |
| Yurley Ximena Lozano | Dominio y reglas del negocio: `Evento`, `Reserva`, `EstadoReserva`, `Asistencia`, `pom.xml` |
| Looana Marinella Leon Guevara | Casos de uso y puertos: `RepositorioEventos`, `Reloj` y los cuatro casos de uso |
| Laura Geraldine Beltran Cediel | Interfaz JavaFX e integración: controladores, FXML, CSS, `App`, `Launcher` y diagramas UML |
| Derlys Xiomara Leal Castillo | Persistencia e infraestructura: `RepositorioEventosEnMemoria`, `RepositorioEventosH2`, `RelojDelSistema`, `schema.sql`, `ConfiguracionH2`, `DatosIniciales` |

## Cómo ejecutar

Requisitos: Java 21 y Maven 3.9 o superior.

```
mvn test          # compila y ejecuta las pruebas
mvn javafx:run    # abre la aplicación
```

La base H2 se crea sola en `datos/campusfest` la primera vez, con un evento de ejemplo llamado `ev-1`.
Para probar: en la pestaña **Reservar entrada** solicite una reserva para `ev-1`, confírmela, copie el código
y péguelo en **Registrar ingreso**.

## Arquitectura

| Anillo | Paquete | Clases |
| --- | --- | --- |
| Entidades | `co.edu.unillanos.campusfest.domain` | `Evento`, `Reserva`, `EstadoReserva`, `Asistencia` |
| Casos de uso | `...application.usecase` y `...application.port` | `ConsultarCupos`, `ReservarEntrada`, `EditarEvento`, `RegistrarIngreso`; puertos `RepositorioEventos` y `Reloj` |
| Adaptadores | `...infrastructure.persistence`, `.ui`, `.reloj` | `RepositorioEventosEnMemoria`, `RepositorioEventosH2`, `RelojDelSistema`, cuatro controladores |
| Frameworks y drivers | `co.edu.unillanos.campusfest` | `App`, `Launcher`, `ConfiguracionH2`, `DatosIniciales`, FXML, CSS y `db/schema.sql` |

## Casos de uso implementados

| Caso de uso | Clase | Operaciones | Pantalla |
| --- | --- | --- | --- |
| CU1 Consultar cupos | `ConsultarCupos` | `consultar` | `pantalla-cupos.fxml` |
| CU2 Reservar y confirmar una entrada | `ReservarEntrada` | `solicitarReserva`, `confirmarReserva` | `pantalla-reserva.fxml` |
| CU3 Editar evento | `EditarEvento` | `obtener`, `editar` | `pantalla-evento.fxml` |
| CU4 Registrar el ingreso con QR | `RegistrarIngreso` | `registrar` | `pantalla-ingreso.fxml` |

Flujos alternativos cubiertos: sin cupos y reserva vencida (CU2); aforo menor que los cupos ocupados (CU3);
código de otro evento, reserva no confirmada e ingreso repetido (CU4).

## Declaración de uso de IA

**Herramientas y para qué se usaron.** Se usó Claude (Anthropic) para repartir el trabajo entre los cuatro integrantes,
generar el primer borrador de las clases y de las pantallas, y revisar el cumplimiento de las guías. Clases en las que
intervino: `Reserva`, `Evento`, `Asistencia`, `EstadoReserva`, los cuatro casos de uso, `BuscadorDeEventos`,
`RepositorioEventosEnMemoria`, `RepositorioEventosH2`, `schema.sql`, `ConfiguracionH2`, `DatosIniciales`, los cuatro
controladores, los FXML, el CSS, `App`, `Launcher` y este README. Cada integrante revisó, entendió y probó su parte
antes de integrarla. Las pruebas entregadas por los docentes no se modificaron.

**Propuestas de la IA que había que corregir.** Durante la revisión de `EditarEvento` se identificó el riesgo de modificar parcialmente el evento cuando alguno de los datos o el aforo no fueran válidos. La implementación final valida primero que el nombre, la fecha y el lugar sean correctos; después modifica el aforo mediante `Evento.cambiarAforo()` y actualiza los demás datos. El repositorio solo guarda el evento cuando todas las operaciones terminan correctamente.