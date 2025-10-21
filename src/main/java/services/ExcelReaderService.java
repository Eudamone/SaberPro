package services;

import model.Estudiante;
import model.Usuario;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ExcelReaderService {
    public List<Estudiante> readStudentsFromExcel(File excelFile) throws IOException, IllegalArgumentException {
        List<Estudiante> estudiantes = new ArrayList<>();

        try (FileInputStream file = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(file)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Saltar la fila de encabezados
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                // Ignorar filas vacías
                if (isRowEmpty(row)) continue;

                try {
                    Estudiante estudiante = mapRowToEstudiante(row);
                    if (estudiante != null) {
                        estudiantes.add(estudiante);
                    }
                } catch (IllegalArgumentException e) {
                    // Propaga el error de mapeo para que el controlador lo muestre
                    throw new IllegalArgumentException("Error en la fila " + (row.getRowNum() + 1) + ": " + e.getMessage(), e);
                }
            }

        }
        return estudiantes;
    }

    private Estudiante mapRowToEstudiante(Row row) {

        // 1. Campos de Usuario (Comunes)
        String nombre = getCellValue(row.getCell(0));
        String email = getCellValue(row.getCell(1));
        String tipoDocumentoStr = getCellValue(row.getCell(2));
        String numIdentificacion = getCellValue(row.getCell(3));
        String username = getCellValue(row.getCell(4));

        // 2. Campos de Estudiante (Específicos)
        String codeStudent = getCellValue(row.getCell(5));
        String estadoAcademicoStr = getCellValue(row.getCell(6));

        // Validaciones básicas (puedes añadir más)
        if (nombre.isEmpty() || email.isEmpty() || numIdentificacion.isEmpty() || codeStudent.isEmpty()) {
            throw new IllegalArgumentException("Campos de usuario o código de estudiante no pueden estar vacíos.");
        }

        // Crear objeto Estudiante
        Estudiante estudiante = new Estudiante();

        // Crear un objeto Usuario temporal para almacenar los datos comunes
        Usuario usuarioTemp = new Usuario();
        usuarioTemp.setNombre(nombre);
        usuarioTemp.setEmail(email);
        usuarioTemp.setNumIdentification(numIdentificacion);
        usuarioTemp.setUsername(username);
        usuarioTemp.setRol(Usuario.rolType.Estudiante); // Rol fijo

        // Mapear Enums (Lanzará IllegalArgumentException si el valor es incorrecto)
        usuarioTemp.setDocument(Usuario.typeDocument.valueOf(tipoDocumentoStr.toUpperCase()));
        estudiante.setAcademicStatus(Estudiante.EstadoAcademico.valueOf(estadoAcademicoStr));

        // Asignar y vincular
        estudiante.setUsuario(usuarioTemp);
        estudiante.setCodeStudent(codeStudent);

        return estudiante;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellValue(cell).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
