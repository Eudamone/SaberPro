# Documentación de las consultas y reportes de resultados internos

Este documento resume los cambios recientes relacionados con la carga de resultados internos, los filtros disponibles y el flujo previsto para construir el reporte estadístico que alimentará el informe IA.

## 1. Estructura de filtros aceptados

- **`InternResultFilter`** ahora guarda cuatro listas: períodos (`List<Integer>`), semestres (`List<Integer>`), áreas (`List<String>`) y NBC (`List<String>`). La clase garantiza que cada colección tenga valores no nulos y proporciona métodos `hasX()` para saber si aplicar el filtro. El filtro asume que semestre solo puede ser 1 o 2 y mantiene el valor tal cual para que la consulta final los incluya.
- La UI final recopila estas selecciones desde `MultiSelectComboBox` y las mapea con `parsePeriods`/`parseSemesters` antes de construir el filtro. Eso simplifica la validación porque la lógica de criterios (repositorio) no se preocupa por la entrada.

## 2. Entidad y DTO asociados

- En `InternalResult`, la columna `semestre` ahora forma parte del modelo junto con `periodo`, `programa`, `puntajeGlobal`, etc. El enlace con los módulos internos usa `documento` (que representa la cédula) para respetar la llave foránea desde `resultado_modulo_interno`.
- `InternResultInfo` declara un constructor original de siete campos para que Hibernate pueda mapear directamente el resultado de `builder.construct(...)`. Esa clase es la que alimenta la tabla de resultados y mantiene en memoria el programa, el puntaje y el grupo de referencia.

## 3. Repositorio personalizado (`InternalResultRepositoryImpl`)

- Las consultas usan `CriteriaBuilder` para evitar construir sentencias HQL manuales y permiten aplicar ordenamiento por `periodo`, `semestre` y `numeroRegistro`. El método `countResults` reutiliza la misma lista de predicados para paginación y conteo.
- `buildPredicates` crea predicados dinámicos:
  - `periodo` y `semestre` se hacen con `IN`.
  - NBC usa una subconsulta correlacionada a `ExternalGeneralResult`; se comparan programas (trim/upper) y se filtra por `estuNucleoPregrado`. El resultado es una cláusula `EXISTS` que evita repetir la información en la tabla principal. Esta subconsulta respeta el join que mencionaste: un mismo NBC puede agrupar varios programas.
  - Áreas se resuelven con un `JOIN` hacia `InternalModuleResult` y el `modulo`. Las cadenas se normalizan a mayúsculas para evitar diferencias de formato.
- La lista `normalizeStrings` asegura que solo se comparen valores no vacíos.

## 4. Servicio (`CatalogService`)

- `findInternResults` y `countInternResults` encapsulan la paginación y delegan al repositorio personalizado.
- Se agregaron métodos auxiliares (`getPeriodsResult`, `getSemesters`, `getAreas`) para que el controlador configure los multiselects y provee el listado de NBC que a ese decano le corresponden usando `getNBCDean`.

## 5. Controlador y vista (`consultResultsDeanController` / FXML)

- La vista define cajas para las selecciones de período, semestre, área y NBC seguido del botón `Filtrar`. El controlador inicializa cada combo usando los catálogos del servicio.
- `filter(MouseEvent)` levanta las selecciones actuales, arma un nuevo `InternResultFilter` (incluyendo `semestres`) y fuerza la paginación a la primera página para que los resultados se vuelvan a consultar.
- La tabla `internResultTable` se llena con `InternResultInfo` y muestra las columnas `Periodo`, `Semestre`, `Nombre`, `Número de registro`, `Programa` y `Puntaje global`.
- El paginador usa `createPage` para pedir cada página al servicio y aplica una animación de fade para reforzar la recarga. La columna `Semestre` se mantiene visible para que el usuario confirme la selección.

## 6. Reporte estadístico (nueva funcionalidad)

- El backend ahora expone `InternResultReport`, `ReportContext`, `ScoreStatistics`, `ModulePerformance` y `PeriodTrend` para encapsular los bloques sugeridos en la transcripción de los sprints: contexto de los filtros, métricas globales, desglose por competencia y tendencia por periodo.
- `CatalogService.generateInternReport` devuelve el informe calculado por `InternalResultRepositoryImpl`, que a su vez construye la consulta agregada basándose en `InternResultFilter`. Esta consulta calcula 1) la población evaluada usando `countResults`, 2) estadísticas globales con `stddev_pop`, 3) promedios/desviaciones/mínimos/máximos/conteos de cada módulo y 4) una serie de tendencias promedio por periodo.
- El campo `trendVariation` expresa la variación porcentual entre el primer y último periodo incluido en el filtro (cuando hay al menos dos periodos).
- `ReportContext` conserva el estado de los filtros aplicados y la población total entregada al reporte para que la UI muestre metadatos "Periodos: ... | Semestres: ...".

## 7. Frontend (JavaFX) para visualizar el reporte

- En `consultResultsDean.fxml` se añadió el botón "Generar reporte" al lado de "Filtrar" y se creó un bloque adicional que muestra:
  - Un resumen de filtros, población, promedio global y variación.
  - La mejor y la peor competencia disponibles en el reporte.
  - Una tabla agregada con columnas de competencia, promedio, desviación estándar, mínimo, máximo y cantidad de estudiantes.
- El controlador `consultResultsDeanController` ahora inyecta nuevos `Label` y `TableView` para esos datos, y expone el handler `generateReport(MouseEvent)` que:
  1. Usa `CatalogService.generateInternReport` con el filtro vigente.
  2. Llena las etiquetas y la `TableView<ModulePerformance>` con la información devuelta.
  3. Hace visible el contenedor `containerReport`.
- La tabla de módulos usa helper `formatDecimal` y `formatInteger` para presentar valores formateados y mantiene el promedio/desviación/mínimos representados con dos decimales.
- La UI sigue reutilizando los combos múltiples (periodos, semestres, áreas, NBC) definidos anteriormente; sólo se añadió el reporte generado dinámicamente sin alterar los filtros ni la paginación principal.

## 8. Siguientes pasos sugeridos

1. Validar que `containerReport` se oculte nuevamente si cambian los filtros, para evitar mostrar información obsoleta.
2. Implementar la visualización de los niveles de desempeño y semáforo (verde/rojo) si se desea enriquecer la tabla de módulos.
3. Integrar este reporte como insumo para el informe IA (serializar `InternResultReport` y enviarlo al servicio de generación automatizada).

Con estas notas cualquier desarrollador puede entender qué cambios se hicieron, cómo se configuran los filtros y cuál es la base para extender el reporte final.


## Columna `semestre` en `resultado_interno`

La entidad `model.InternalResult` expone el atributo `semestre`, mapeado con `@Column(name = "semestre")`. Para evitar desajustes entre el modelo y la base de datos:

1. **Migración**  
   Ejecuta `ALTER TABLE resultado_interno ADD COLUMN semestre INTEGER;` (o el script equivalente en tu herramienta de migraciones) y registra el cambio en el control de versiones de la BD.

2. **Fuentes de datos**  
   Actualiza cualquier proceso ETL/CSV para poblar el campo `semestre` cuando se disponga del dato. Si no existe en la fuente, documenta el valor por defecto (por ejemplo `NULL`) dentro del flujo de carga.

3. **Auditoría**  
   Incluye en los reportes internos una nota aclarando desde qué versión se consolidó el campo y cómo impacta los cálculos de poblaciones evaluadas.

4. **Verificación**  
   Añade una verificación automatizada (prueba o checklist) que confirme la existencia de la columna antes de desplegar nuevas versiones.v