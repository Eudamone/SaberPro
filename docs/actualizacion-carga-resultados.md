# Actualización de carga y consulta de resultados (externos e internos)

## Resumen rápido
- Se estabilizó la carga de resultados externos generales, de módulos y específicos.
- Se añadió validación previa contra `resultado_externo` para evitar violar la FK en `resultado_especifico_externo`.
- Se normalizan los nombres de módulos (con y sin tildes) para reutilizar el mismo registro.
- Los filtros y reportes ahora trabajan con los módulos normalizados.
- La consola informa cuántos registros específicos se insertan y cuántos se omiten.

## Flujo de carga actual (externos)
1. **Generales (`resultado_externo`)**
   - Clase: `FileProcessingService.parseAndSaveGeneral`.
   - Detecta delimitador, mapea columnas tolerando variaciones en encabezados y guarda en lotes (`BATCH_SIZE_GENERAL`).
   - Después de cada batch genera sus filas derivadas en `resultado_modulo_externo`.

2. **Módulos externos agregados**
   - Clase: `FileProcessingService.parseAndSaveSpecifics`.
   - Convierte cada fila a `ExternalModuleResult`. Se comparten los mismos IDs de módulo gracias a `resolveOrCreateModuloId`.

3. **Resultados específicos (por área)**
   - Clase: `FileProcessingService.parseAndSaveSpecificsResults`.
   - Pasos clave:
     1. Normaliza el período recibido.
     2. Pre-carga todos los `est_consecutivo` del período vía `ExternalGeneralResultRepository.findEstConsecutivosByPeriodo` para evitar SELECT por cada fila.
     3. Procesa el TXT (con o sin encabezado), normaliza los módulos y convierte los puntajes a `Integer`.
     4. Solo inserta filas cuyo consecutivo exista en `resultado_externo`; las demás se registran como omitidas.
     5. Al finalizar imprime en consola: `Registros insertados: X, omitidos por falta de general: Y`.

## Normalización de módulos
- Método central: `FileProcessingService.resolveOrCreateModuloId`.
- Pasos:
  1. Limpia espacios y lleva a mayúsculas (`key`).
  2. Busca en caché `moduloCache`.
  3. Consulta `ModuloRepository.findFirstByNombreIgnoreCase`.
  4. Normaliza (sin tildes) con `normalizeModuleName` y consulta `ModuloRepository.findFirstByNombreNormalized` (usa `public.unaccent`).
  5. Solo crea un nuevo `Modulo` si ninguna de las variantes existe.
- Resultado: nombres como "PROMOCIÓN DE LA SALUD" y "PROMOCION DE LA SALUD" terminan siendo el mismo módulo.

## Consultas y reportes por área
- Repository: `InternalResultRepositoryImpl`.
- Cambios relevantes:
  - `buildPredicates` y `buildExternalPredicates` normalizan los textos antes de filtrarlos (upper + trim).
  - Las métricas por módulo (`buildModulePerformances` y `buildExternalModulePerformances`) usan el nombre normalizado que proviene del mismo `Modulo` compartido.
- Con esto, los reportes y las "tiles" ya no crean áreas duplicadas por diferencias de acentuación.

## Métricas y logging
- Durante la carga específica:
  - Cada batch guardado incrementa `totalImported`.
  - Los consecutivos sin general se guardan en `missingStudents` y se imprimen solo una vez por valor.
  - Al terminar se imprime un resumen para documentar cuántos registros se persistieron/omitieron.

## Uso recomendado
1. Subir primero el archivo general del período.
2. Subir módulos externos agregados (si aplica).
3. Subir el TXT de específicos.
4. Revisar la consola para confirmar `Registros insertados` y `omitidos`.
5. Abrir el dashboard de reportes y aplicar filtros por área; los nombres aparecerán una sola vez.

## Próximos pasos sugeridos
- Añadir pruebas automáticas (unitarias/integración) para el flujo de carga.
- Extender la normalización a otros catálogos si surgen variaciones de escritura.
- Considerar exponer el resumen de carga en la UI para evitar depender solo de la consola.

