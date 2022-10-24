package it.excel_models;

import it.excel_models.config.ExcelColumn;
import it.excel_models.config.ExcelObject;
import it.excel_models.config.ExcelWriterConfig;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Log4j2
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

    public <T> void write(List<T> models, OutputStream outputStream) {
        Class<?> type = Utils.getCollectionType(models);
        log.debug("Starting writing excel for '{}' model", type);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = config.getSheetName() != null ? workbook.createSheet(config.getSheetName()) : workbook.createSheet();

            // todo header

            int currentRowIndex = 0;
            for (T model : models) {
                Row row = sheet.createRow(currentRowIndex++);
                writeModel(model, row);
            }

            workbook.write(outputStream);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("An error occurred while writing file.");
        }

        log.info("File parsed successfully for '{}' model", type.getSimpleName());
    }

    private void writeModel(final Object model, final Row row) throws Exception {
        Map<Annotation, Field> fieldMap = Utils.getFieldMap(model.getClass());

        for (Map.Entry<Annotation, Field> entry : fieldMap.entrySet()) {
            if (entry.getKey() instanceof ExcelColumn) {
                ExcelColumn annotation = (ExcelColumn) entry.getKey();
                Field field = entry.getValue();
                Cell cell = row.createCell(annotation.index());

                Object value = field.get(model);
                if (value != null) {
                    // todo types
                    cell.setCellValue(value.toString());
                }
            } else if (entry.getKey() instanceof ExcelObject) {
                Field field = entry.getValue();
                writeModel(field.getType(), row);
            }
        }
    }
}
