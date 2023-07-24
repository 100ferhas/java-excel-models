package io.github.ferhas.excel_models;

import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.ExcelObject;
import io.github.ferhas.excel_models.config.ExcelWriterConfig;
import io.github.ferhas.excel_models.exception.ExcelModelException;
import lombok.NonNull;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public final class ExcelWriter {
    private final ExcelWriterConfig config;
    private CellStyle contentStyle;

    public ExcelWriter() {
        this(new ExcelWriterConfig());
    }

    public ExcelWriter(ExcelWriterConfig config) {
        this.config = config;
    }

    public <T> OutputStream write(@NonNull Collection<T> models) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        write(models, byteArrayOutputStream);
        return byteArrayOutputStream;
    }

    public <T> void write(@NonNull Collection<T> models, @NonNull OutputStream outputStream) {
        if (models.isEmpty()) {
            return;
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Map<Annotation, Field> fieldMap = ExcelUtils.getFieldMap(Class.forName(models.iterator().next().getClass().getTypeName()), true);

            // init content style
            if (config.getContentStyleBuilder() != null) {
                contentStyle = config.getContentStyleBuilder().apply(workbook);
            } else {
                contentStyle = workbook.createCellStyle();
                contentStyle.setWrapText(true);
            }

            Sheet sheet = config.getSheetName() != null ? workbook.createSheet(config.getSheetName()) : workbook.createSheet();

            // create header or call consumer created by user
            if (config.getHeaderBuilder() != null) {
                config.getHeaderBuilder().accept(workbook, sheet);
            } else {
                CellStyle cellStyle = config.getHeaderStyleBuilder() != null ? config.getHeaderStyleBuilder().apply(workbook) : null;
                Row headerRow = sheet.createRow(sheet.getPhysicalNumberOfRows());
                writeHeader(fieldMap, headerRow, cellStyle, models.iterator().next());
            }

            int currentRowIndex = sheet.getPhysicalNumberOfRows();
            for (T model : models) {
                Row row = sheet.createRow(currentRowIndex++);
                row.setHeight((short) -1);
                writeModel(fieldMap, model, row);
            }

            for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
                sheet.autoSizeColumn(i, true);
            }

            if (config.getFooterBuilder() != null) {
                config.getFooterBuilder().accept(workbook, sheet);
            }

            workbook.write(outputStream);

        } catch (Exception e) {
            throw new ExcelModelException("An error occurred while writing file.", e);
        }
    }

    private <T> void writeHeader(Map<Annotation, Field> fieldMap, Row row, CellStyle cellStyle, T model) throws Exception {
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
                writeHeader(fieldMap, row, cellStyle, field.get(model));
            }
        }
    }

    private <T> void writeModel(Map<Annotation, Field> fieldMap, final T model, final Row row) throws Exception {
        for (Map.Entry<Annotation, Field> entry : fieldMap.entrySet()) {
            if (entry.getKey() instanceof ExcelColumn) {
                ExcelColumn annotation = (ExcelColumn) entry.getKey();
                Field field = entry.getValue();
                Cell cell = row.createCell(annotation.index() - 1);
                cell.setCellStyle(contentStyle);

                Object value = field.get(model);
                if (value != null) {
                    cell.setCellValue(value.toString());
                }
            } else if (entry.getKey() instanceof ExcelObject) {
                Field field = entry.getValue();
                writeModel(fieldMap, field.get(model), row);
            }
        }
    }
}
