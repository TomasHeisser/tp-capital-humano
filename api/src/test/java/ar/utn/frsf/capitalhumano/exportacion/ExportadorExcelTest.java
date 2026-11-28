package ar.utn.frsf.capitalhumano.exportacion;

import ar.utn.frsf.capitalhumano.dominio.TipoDocumento;
import ar.utn.frsf.capitalhumano.dto.ArchivoDTO;
import ar.utn.frsf.capitalhumano.dto.CandidatoClaveDTO;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportadorExcelTest {

    private final LocalDateTime fecha = LocalDateTime.of(2026, 11, 26, 10, 30);

    @Test
    void nombreDelArchivoConFechaYFuncionYUnaFilaPorCandidato() throws Exception {
        ArchivoDTO a = new ExportadorExcel().exportar("DESARROLLADOR JAVA JUNIOR", fecha, List.of(
                new CandidatoClaveDTO(5L, "LÓPEZ", "CAMILA", TipoDocumento.DNI, "40111222", "T6Q9D2LM"),
                new CandidatoClaveDTO(6L, "DÍAZ", "JULIETA", null, "38222333", "R8V3K5NW")));

        assertEquals("2026-11-26-10-30-DESARROLLADOR JAVA JUNIOR.xlsx", a.getNombre());
        try (Workbook libro = new XSSFWorkbook(new ByteArrayInputStream(a.getContenido()))) {
            Sheet hoja = libro.getSheetAt(0);
            assertTrue(hoja.getRow(0).getCell(0).getStringCellValue().contains("DESARROLLADOR JAVA JUNIOR - 26/11/2026 10:30"));
            assertEquals("Clave de ingreso", hoja.getRow(2).getCell(4).getStringCellValue());
            assertEquals("LÓPEZ", hoja.getRow(3).getCell(0).getStringCellValue());
            assertEquals("DNI", hoja.getRow(3).getCell(2).getStringCellValue());
            assertEquals("T6Q9D2LM", hoja.getRow(3).getCell(4).getStringCellValue());
            assertEquals("", hoja.getRow(4).getCell(2).getStringCellValue());
            assertEquals(4, hoja.getLastRowNum());
        }
    }

    @Test
    void sacaLosCaracteresQueNoVanEnUnNombreDeArchivo() {
        ArchivoDTO a = new ExportadorExcel().exportar("ANALISTA QA/QC: \"SENIOR\"", fecha, List.of());
        assertEquals("2026-11-26-10-30-ANALISTA QAQC SENIOR.xlsx", a.getNombre());
    }
}
