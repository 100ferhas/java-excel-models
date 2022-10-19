package it.excel_models;

import it.excel_models.config.ExcelColumn;
import it.excel_models.config.ExcelWriterConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Slf4j
public class ExcelWriter {
    private final ExcelWriterConfig config;

    public ExcelWriter() {
        this.config = new ExcelWriterConfig();
    }

    public ExcelWriter(ExcelWriterConfig config) {
        this.config = config;
    }

    public <E> OutputStream write(List<E> models) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        write(models, byteArrayOutputStream);
        return byteArrayOutputStream;
    }

    public <E> void write(List<E> models, OutputStream outputStream) {
        Class<?> type = Utils.getCollectionType(models);
        log.debug("Starting writing excel for '{}' model", type);

        Map<ExcelColumn, Field> fieldMap = Utils.getFieldMap(type);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = config.getSheetName() != null ? workbook.createSheet(config.getSheetName()) : workbook.createSheet();

            int currentRowIndex = 0;

            // todo header

            for (E model : models) {
                Row row = sheet.createRow(currentRowIndex++);

                for (Map.Entry<ExcelColumn, Field> entry : fieldMap.entrySet()) {
                    ExcelColumn annotation = entry.getKey();
                    Field field = entry.getValue();
                    Cell cell = row.createCell(annotation.index());

                    Object value = field.get(model);
                    if (value != null) {
                        // todo types
                        cell.setCellValue(value.toString());
                    }
                }
            }

            workbook.write(outputStream);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("An error occurred while writing file.");
        }

        log.info("File parsed successfully for '{}' model", type.getSimpleName());
    }
}
