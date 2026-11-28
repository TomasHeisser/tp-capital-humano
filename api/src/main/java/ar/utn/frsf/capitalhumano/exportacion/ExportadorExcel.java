package ar.utn.frsf.capitalhumano.exportacion;

import ar.utn.frsf.capitalhumano.dto.ArchivoDTO;
import ar.utn.frsf.capitalhumano.dto.CandidatoClaveDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Arma el .xlsx con Apache POI: un titulo con el puesto y la fecha, y una fila por candidato.
 */
@Component
public class ExportadorExcel implements ExportadorClaves {

    private static final DateTimeFormatter PARA_NOMBRE = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
    private static final DateTimeFormatter PARA_TITULO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] COLUMNAS = {"Apellido", "Nombre", "Tipo documento", "Nro documento", "Clave de ingreso"};

    @Override
    public ArchivoDTO exportar(String puesto, LocalDateTime fecha, List<CandidatoClaveDTO> claves) {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Claves");
            CellStyle negrita = libro.createCellStyle();
            Font fuente = libro.createFont();
            fuente.setBold(true);
            negrita.setFont(fuente);

            Cell titulo = hoja.createRow(0).createCell(0);
            titulo.setCellValue("Evaluación de candidatos - " + puesto + " - " + fecha.format(PARA_TITULO));
            titulo.setCellStyle(negrita);

            Row encabezado = hoja.createRow(2);
            for (int i = 0; i < COLUMNAS.length; i++) {
                Cell celda = encabezado.createCell(i);
                celda.setCellValue(COLUMNAS[i]);
                celda.setCellStyle(negrita);
            }
            int nro = 3;
            for (CandidatoClaveDTO c : claves) {
                Row fila = hoja.createRow(nro++);
                fila.createCell(0).setCellValue(c.getApellido());
                fila.createCell(1).setCellValue(c.getNombre());
                fila.createCell(2).setCellValue(c.getTipoDocumento() == null ? "" : c.getTipoDocumento().name());
                fila.createCell(3).setCellValue(c.getNroDocumento());
                fila.createCell(4).setCellValue(c.getClave());
            }
            for (int i = 0; i < COLUMNAS.length; i++) {
                hoja.autoSizeColumn(i);
            }
            libro.write(salida);
            return new ArchivoDTO(nombreArchivo(puesto, fecha), salida.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el archivo Excel", e);
        }
    }

    // aaaa-mm-dd-hh-mm-NombreFuncion.xlsx (se sacan los caracteres que no van en un nombre de archivo)
    private String nombreArchivo(String puesto, LocalDateTime fecha) {
        String funcion = puesto.replaceAll("[\\\\/:*?\"<>|]", "").trim();
        return fecha.format(PARA_NOMBRE) + "-" + funcion + ".xlsx";
    }
}
