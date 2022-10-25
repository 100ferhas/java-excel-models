package it.excel_models;

import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
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
    private CellStyle contentStyle;

    public ExcelWriter() {
        config = new ExcelWriterConfig();
    }

    public ExcelWriter(ExcelWriterConfig config) {
        this.config = config;
    }

    public <T> OutputStream write(List<T> models) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        write(models, byteArrayOutputStream);
        return byteArrayOutputStream;
    }

    public <T> void write(List<T> models, OutputStream outputStream) {
        log.debug("Starting writing excel");

        if (models == null || models.isEmpty()) {
            log.warn("No data to export...");
            return;
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            if (config.getContentStyleBuilder() != null) {
                contentStyle = config.getContentStyleBuilder().apply(workbook);
            } else {
                contentStyle = workbook.createCellStyle();
                contentStyle.setWrapText(true);
            }

            Sheet sheet = config.getSheetName() != null ? workbook.createSheet(config.getSheetName()) : workbook.createSheet();

            CellStyle cellStyle = config.getHeaderStyleBuilder() != null ? config.getHeaderStyleBuilder().apply(workbook) : null;
            writeHeader(sheet.createRow(sheet.getPhysicalNumberOfRows()), cellStyle, models.get(0));

            int currentRowIndex = sheet.getPhysicalNumberOfRows();
            for (T model : models) {
                Row row = sheet.createRow(currentRowIndex++);
                row.setHeight((short) -1);
                writeModel(model, row);
            }

            for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
                sheet.autoSizeColumn(i, true);
            }

            if (config.getFooterBuilder() != null) {
                config.getFooterBuilder().accept(sheet);
            }

            workbook.write(outputStream);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("An error occurred while writing file.");
        }

        log.info("File written successfully");
    }

    private <T> void writeHeader(Row row, CellStyle cellStyle, T model) throws Exception {
        Map<Annotation, Field> fieldMap = Utils.getFieldMap(Class.forName(model.getClass().getTypeName()), true);

        for (Map.Entry<Annotation, Field> entry : fieldMap.entrySet()) {
            if (entry.getKey() instanceof ExcelColumn) {
                ExcelColumn annotation = (ExcelColumn) entry.getKey();
                Field field = entry.getValue();
                Cell cell = row.createCell(annotation.index() - 1);
                cell.setCellValue(annotation.title() != null && !annotation.title().isBlank() ? annotation.title() : field.getName());

                if (cellStyle != null) {
                    cell.setCellStyle(cellStyle);
                }
            } else if (entry.getKey() instanceof ExcelObject) {
                Field field = entry.getValue();
                writeHeader(row, cellStyle, field.get(model));
            }
        }
    }

    private <T> void writeModel(final T model, final Row row) throws Exception {
        Map<Annotation, Field> fieldMap = Utils.getFieldMap(Class.forName(model.getClass().getTypeName()), true);

        for (Map.Entry<Annotation, Field> entry : fieldMap.entrySet()) {
            if (entry.getKey() instanceof ExcelColumn) {
                ExcelColumn annotation = (ExcelColumn) entry.getKey();
                Field field = entry.getValue();
                Cell cell = row.createCell(annotation.index() - 1);
                cell.setCellStyle(contentStyle);

                Object value = field.get(model);
                if (value != null) {
                    // todo types
                    cell.setCellValue(value.toString());
                }
            } else if (entry.getKey() instanceof ExcelObject) {
                Field field = entry.getValue();
                writeModel(field.get(model), row);
            }
        }
    }
}
