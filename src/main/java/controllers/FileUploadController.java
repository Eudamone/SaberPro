package controllers;

import service.FileProcessingService;
import model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/uploads")
public class FileUploadController {

    private final FileProcessingService fileService;

    public FileUploadController(FileProcessingService fileService) {
        this.fileService = fileService;
    }

    /**
     * Endpoint para subir archivos externos:
     * - optional MultipartFile general: archivo externo general (contiene 'periodo').
     * - optional MultipartFile[] specifics: archivos externos específicos. Debe enviarse el parámetro 'periodo'
     *   si no se envió el general en la misma petición, y deberá existir ese periodo en la tabla general.
     *
     * Reglas:
     *  - Si se envía 'general' y 'specifics' en la misma petición, se procesa primero general y luego specifics.
     *  - Si se envía sólo 'specifics', se requiere parámetro 'periodo' y que exista en la tabla general.
     */
    @PostMapping("/external")
    public ResponseEntity<?> uploadExternal(
            @RequestParam(value = "general", required = false) MultipartFile general,
            @RequestParam(value = "specifics", required = false) MultipartFile[] specifics,
            @RequestParam(value = "periodo", required = false) Integer periodo
    ) {
        try {
            if (general != null) {
                List<ExternalGeneralResult> savedGeneral = fileService.parseAndSaveGeneral(general);
                // if periodo not provided, try set from first saved row
                if (periodo == null && !savedGeneral.isEmpty()) {
                    periodo = savedGeneral.get(0).getPeriodo();
                }
            }

            if (specifics != null && specifics.length > 0) {
                if (periodo == null) {
                    return ResponseEntity.badRequest().body("El parámetro 'periodo' es requerido si no se subió 'general' en la misma petición.");
                }
                // validar existencia del general para ese periodo
                if (!fileService.existsGeneralPeriod(periodo)) {
                    return ResponseEntity.badRequest().body("No existe archivo general para el periodo indicado: " + periodo);
                }
                for (MultipartFile spec : specifics) {
                    fileService.parseAndSaveSpecifics(spec, periodo);
                }
            }

            return ResponseEntity.ok("Archivos procesados correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error procesando archivos: " + e.getMessage());
        }
    }

    @PostMapping("/internal")
    public ResponseEntity<?> uploadInternal(@RequestParam("file") MultipartFile file) {
        try {
            fileService.parseAndSaveInternal(file);
            return ResponseEntity.ok("Archivo interno procesado.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error procesando archivo interno: " + e.getMessage());
        }
    }
}
