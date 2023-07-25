package io.github.ferhas.excel_models;

import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.ExcelObject;
import io.github.ferhas.excel_models.config.ExcelWriterConfig;
import io.github.ferhas.excel_models.converter.FieldConverter;
import io.github.ferhas.excel_models.exception.ExcelModelException;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
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
            Sheet sheet = config.getSheetName() != null ? workbook.createSheet(config.getSheetName()) : workbook.createSheet();

            // HEADER
            if (config.getHeaderBuilder() != null) {
                config.getHeaderBuilder().accept(workbook, sheet);
            } else {
                CellStyle cellStyle = config.getHeaderStyleBuilder() != null ? config.getHeaderStyleBuilder().apply(workbook) : null;
                Row headerRow = sheet.createRow(sheet.getPhysicalNumberOfRows());
                writeHeader(headerRow, cellStyle, models.iterator().next());
            }

            // CONTENT
            int currentRowIndex = sheet.getPhysicalNumberOfRows();
            for (T model : models) {
                Row row = sheet.createRow(currentRowIndex++);
                CellStyle cellStyle = config.getContentRowStyleBuilder().apply(workbook, row.getRowNum());
                writeModel(model, row, cellStyle);
                row.setHeight((short) -1);
            }

            // FOOTER
            if (config.getFooterBuilder() != null) {
                config.getFooterBuilder().accept(workbook, sheet);
            }

            for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
                sheet.autoSizeColumn(i, true);
            }

            workbook.write(outputStream);

        } catch (Exception e) {
            throw new ExcelModelException("An error occurred while writing file.", e);
        }
    }

    private <T> void writeHeader(Row row, CellStyle cellStyle, T model) throws Exception {
        Map<Annotation, Field> fieldMap = FieldConverterProvider.getFieldMap(Class.forName(model.getClass().getTypeName()), true);

        for (Map.Entry<Annotation, Field> entry : fieldMap.entrySet()) {
            if (entry.getKey() instanceof ExcelColumn) {
                ExcelColumn annotation = (ExcelColumn) entry.getKey();
                Field field = entry.getValue();
                Cell cell = row.createCell(annotation.index() - 1);
                String headerTitle = annotation.title() == null || annotation.title().isBlank() ?
                        StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(field.getName()), StringUtils.SPACE)) :
                        annotation.title();

                cell.setCellValue(headerTitle);

                if (cellStyle != null) {
                    cell.setCellStyle(cellStyle);
                }
            } else if (entry.getKey() instanceof ExcelObject) {
                Field field = entry.getValue();
                writeHeader(row, cellStyle, field.get(model));
            }
        }
    }

    private <T> void writeModel(final T model, final Row row, CellStyle cellStyle) throws Exception {
        Map<Annotation, Field> fieldMap = FieldConverterProvider.getFieldMap(Class.forName(model.getClass().getTypeName()), true);

        for (Map.Entry<Annotation, Field> entry : fieldMap.entrySet()) {
            if (entry.getKey() instanceof ExcelColumn) {
                ExcelColumn annotation = (ExcelColumn) entry.getKey();
                Field field = entry.getValue();
                Cell cell = row.createCell(annotation.index() - 1);
                cell.setCellStyle(cellStyle);

                Object value = field.get(model);
                FieldConverter<?> fieldConverter = FieldConverterProvider.getFieldConverter(field);

                String cellValue = null;
                if (fieldConverter != null) {
                    cellValue = fieldConverter.toExcelValue(value);
                } else if (value != null) {
                    cellValue = value.toString();
                }

                cell.setCellValue(cellValue);

            } else if (entry.getKey() instanceof ExcelObject) {
                Field field = entry.getValue();
                writeModel(field.get(model), row, cellStyle);
            }
        }
    }
}
