# Documentación de cargas, filtros y reporte estadístico

Este documento consolida los cambios más recientes sobre:
- la incorporación del campo **semestre** en la cadena de ingestión;
- el comportamiento del buscador de resultados internos/externos; y
- la generación del reporte estadístico que sirve como insumo para la IA.

---

## 1. Flujo general

1. **Carga de archivos:** el administrador sube los CSV/XLS. Durante la carga, el asistente solicita *periodo* y *semestre*. Ambos valores se almacenan en `resultado_interno` y en los DTO usados para renderizar la tabla.
2. **Consulta interactiva:** el decano selecciona filtros (periodo, semestre, área, NBC) y presiona **Filtrar**. El backend arma un `InternResultFilter` y ejecuta la consulta paginada sobre `InternalResult` y sus módulos (`InternalModuleResult`).
3. **Reporte estadístico:** con los mismos filtros se puede presionar **Generar reporte**. Se calcula `InternResultReport`, que agrega estadísticas internas y externas para mostrarlas en la UI y posteriormente entregarlas a la IA para el informe interpretativo.

---

## 2. Detalle de filtros aceptados

`dto.InternResultFilter` mantiene cuatro listas:
- `periods (List<Integer>)`
- `semesters (List<Integer>)`
- `areas (List<String>)`
- `nbc (List<String>)`

El controlador `consultResultsDeanController` alimenta las listas usando `MultiSelectComboBox`. Los métodos `parsePeriods` y `parseSemesters` validan que sólo lleguen valores numéricos; cualquier entrada inválida se descarta silenciosamente.

> **Nota:** si no se selecciona ningún valor en un filtro, la lista queda vacía y el repositorio ignora ese criterio.

---

## 3. Esquema y carga del semestre

### 3.1 Migración

```sql
ALTER TABLE resultado_interno ADD COLUMN semestre INTEGER;
```

- El campo es opcional, pero la página de carga solicita 1 o 2. Debe añadirse a cualquier ETL o script de importación.  
- `model.InternalResult` está anotado con `@Column(name = "semestre")` y utiliza `documento` como clave externa para enlazar con `InternalModuleResult`.

### 3.2 Limpieza de datos históricos

- Antes de activar la nueva versión se eliminaron los registros 2023 duplicados para volver a cargarlos con el semestre correcto.  
- Si se detectan archivos viejos sin semestre, deben subirse de nuevo; de lo contrario los filtros por semestre excluirán esos estudiantes.

---

## 4. Repositorio personalizado (`InternalResultRepositoryImpl`)

### 4.1 Búsqueda y paginación

- `findResults` y `countResults` construyen `CriteriaQuery`.  
- Se ordena por `periodo`, `semestre` y `numeroRegistro` para que la tabla quede estable entre páginas.  
- `buildPredicates` agrega dinámicamente:
  - `periodo IN (?)`
  - `semestre IN (?)`
  - `grupoReferencia`/`modulo` según el filtro de áreas
  - Una subconsulta `EXISTS` sobre `ExternalGeneralResult` para los filtros NBC (comparando programa interno vs. `estu_prgm_academico`).

### 4.2 Agregaciones para el reporte

- `generateReport(InternResultFilter)` arma cuatro bloques:
  1. **Contexto:** periodos, semestres, áreas y NBC efectivos; total de estudiantes evaluados (`count(distinct documento)`).
  2. **Estadísticas globales internas:** promedio, desviación y rango del puntaje global + población que presentó la prueba.
  3. **Estadísticas por módulo interno:** `avg`, `stddev_pop`, `min`, `max`, `count(distinct interno_id)` ignorando puntajes nulos/0.
  4. **Referencia externa:** mismas métricas sobre `resultado_modulo_externo` para comparar.
- La misma instancia de `InternResultFilter` se reutiliza para las consultas externas (periodos, NBC y programas). Si se desea excluir el semestre en externos (porque la fuente ICFES no lo maneja) se puede ajustar `buildExternalPredicates`.

### 4.3 Tendencia

- `buildPeriodTrends` calcula el promedio por periodo (ordenado ascendente).  
- `computeTrendVariation` compara el primer vs. último promedio y retorna el porcentaje de variación, mostrado en el reporte como `Variación períodos`.

---

## 5. DTOs clave

| DTO | Descripción |
| --- | --- |
| `InternResultInfo` | Fila paginada para la tabla principal (periodo, semestre, nombre, registro, programa, puntaje, grupo). |
| `InternResultReport` | Contenedor del reporte estadístico. Incluye contexto (`ReportContextStats`), métricas globales internas/externas (`ScoreStatistics`), lista de módulos (`ModulePerformance`) y tendencia (`PeriodTrend`). |
| `ModulePerformance` | Nombre del módulo + estadísticas. Se usa tanto para competencias internas como externas. |

---

## 6. UI y experiencia de usuario

### 6.1 Tabla de resultados

- Componentes clave: `tableView#internResultTable`, `Pagination#pagination`, combos de filtro (`boxPrueba`, `boxSemestre`, `boxArea`, `boxNBC`).
- Acciones:
  1. Seleccionar filtros.
  2. Pulsar **Filtrar** → se refresca la tabla y la paginación vuelve a la página 0.
  3. Navegar por páginas si hay más resultados (se muestran 15 filas por página).

### 6.2 Reporte estadístico

- Botón **Generar reporte** (abajo a la derecha).  
- Muestra un overlay (`VBox#containerReport`) con:
  1. Encabezado “Reporte estadístico” y botón **Cerrar**.
  2. Bloque de contexto (filtros activos y población evaluada).  
  3. KPIs: promedios internos/externos, variación, mejor y peor competencia.  
  4. Tabla “Competencias internas” con columnas: Competencia, Promedio, Desv. estándar, Mínimo, Máximo, Estudiantes.  
  5. Tabla “Competencias de referencia externa” con las mismas columnas (sin columna de variación para evitar saturar la vista).  
- Estilos añadidos en `styles/dean.css`: `lbReportTitle`, `lbReportSubtitle`, `table-red` para los encabezados en degradado rojo y `report-chip` para etiquetas destacadas.

### 6.3 Buenas prácticas de uso

- Tras cambiar filtros es recomendable cerrar el reporte (o volver a generarlo) para evitar ver datos desactualizados.  
- En filtros con grandes combinaciones (muchos NBC y áreas) es preferible seleccionar periodos concretos para reducir el tiempo de consulta.

---

## 7. Comparación con resultados externos

- El filtro NBC se sincroniza con los programas internos gracias a la subconsulta `EXISTS`. Esta valida que el programa interno pertenezca a cualquiera de los `estu_nucleo_pregrado` seleccionados.  
- Los módulos externos están normalizados en la tabla `modulo`, de modo que la comparación se hace módulo a módulo (mismas etiquetas que en el interno).  
- La tabla externa respeta los mismos filtros de periodos, áreas/NBC y sólo ignora semestres (ICFES no provee ese dato). Si en el futuro se necesita mapear semestres contra periodos ICFES se puede agregar una regla adicional antes de ejecutar la consulta externa.

---

## 8. Pasos para desarrolladores

1. **Verificar estructura:** antes de desplegar, asegúrate de que la columna `semestre` exista.  
2. **Levantar backend:**
   ```powershell
   .\mvnw spring-boot:run
   ```
3. **Probar filtros básicos:** ingresar como decano, cargar periodos y semestres, aplicar filtros combinados y revisar la paginación.
4. **Probar reporte:** aplicar filtros con más de un periodo para verificar la variación porcentual; revisar que las tablas internas/externas contengan datos coherentes (rangos, promedios, tamaño de muestra).
5. **Entrega a IA:** serializar `InternResultReport` (JSON) y adjuntarlo a la solicitud de análisis automático.

---

## 9. Futuras mejoras sugeridas

- Persistir el resultado del reporte para reutilizarlo sin recalcular cuando el usuario exporte o solicite el informe IA.
- Añadir indicadores de niveles de desempeño (semáforo) y ausentismo si la fuente provee inscritos vs. evaluados.
- Implementar tests automáticos para validar `buildPredicates` y `buildExternalPredicates`, asegurando que cada filtro se respeta en internos y externos.
