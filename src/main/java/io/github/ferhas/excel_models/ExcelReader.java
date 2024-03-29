package io.github.ferhas.excel_models;

import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.ExcelObject;
import io.github.ferhas.excel_models.config.ExcelReaderConfig;
import io.github.ferhas.excel_models.converter.FieldConverter;
import io.github.ferhas.excel_models.exception.ExcelFieldParseException;
import io.github.ferhas.excel_models.exception.ExcelModelException;
import jakarta.validation.ValidationException;
import lombok.NonNull;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class ExcelReader {
    private final ExcelReaderConfig config;

    private final ExcelReaderValidator validator;

    public ExcelReader() {
        this(new ExcelReaderConfig());
    }

    public ExcelReader(ExcelReaderConfig config) {
        this.config = config;
        this.validator = new ExcelReaderValidator();
    }

    public <T> List<T> parse(@NonNull InputStream inputStream, final Class<T> type) {
        return parse(inputStream, type, null);
    }

    public <T> List<T> parse(@NonNull InputStream inputStream, @NonNull Class<T> type, Consumer<T> afterParse) {
        List<T> resultList = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(config.getSheetIndex() - 1);

            for (int rowNum = config.getHeaderOffset(); rowNum < sheet.getPhysicalNumberOfRows(); rowNum++) {
                Row row = sheet.getRow(rowNum);

                if (!isRowEmpty(row)) {
                    T model = parseModel(type, row);

                    if (afterParse != null) {
                        afterParse.accept(model);
                    }

                    try {
                        validator.validate(model);
                    } catch (ValidationException e) {
                        throw new ValidationException(String.format("Invalid value on row %s, %s", row.getRowNum() + 1, e.getMessage()));
                    }

                    resultList.add(model);
                }
            }
        } catch (Exception e) {
            throw new ExcelModelException("An error occurred while reading file.", e);
        } finally {
            // close validator!
            validator.close();
        }

        return resultList;
    }

    /**
     * Check if the row contains only empty cells, sometimes can occur with Excel files.
     *
     * @param row Inspecting row
     * @return true if empty, false otherwise
     */
    private boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            if (cell.getStringCellValue() != null && !cell.getStringCellValue().isBlank()) {
                return false;
            }
        }
        return true;
    }

    private <T> T parseModel(final Class<T> type, final Row row) throws Exception {
        Map<Annotation, Field> fieldMap = FieldConverterProvider.getFieldMap(type, false);
        T model = type.getDeclaredConstructor().newInstance();

        for (Map.Entry<Annotation, Field> entry : fieldMap.entrySet()) {
            if (entry.getKey() instanceof ExcelColumn) {
                ExcelColumn annotation = (ExcelColumn) entry.getKey();
                Cell cell = row.getCell(annotation.index() - 1);

                if (cell != null) {
                    Field field = entry.getValue();
                    field.set(model, getCellValue(cell, field, annotation));
                }
            } else if (entry.getKey() instanceof ExcelObject) {
                Field field = entry.getValue();
                Object nestedModel = parseModel(field.getType(), row);
                field.set(model, nestedModel);
            }
        }

        return model;
    }

    private Object getCellValue(Cell cell, Field field, ExcelColumn annotation) {
        Object value = getValueByCellType(cell);
        return tryConvertOrDefault(field, annotation, value);
    }

    private Object getValueByCellType(Cell cell) {
        Object value;

        switch (cell.getCellType()) {
            case ERROR:
            case FORMULA:
                return null; // ignore
            case NUMERIC:
                value = cell.getNumericCellValue();
                break;
            case BOOLEAN:
                value = cell.getBooleanCellValue();
                break;
            case STRING:
                value = cell.getStringCellValue();
                if (((String) value).isBlank()) {
                    value = null;
                }
                break;
            case _NONE:
            case BLANK:
            default:
                value = null;
        }

        return value;
    }

    private Object tryConvertOrDefault(Field field, ExcelColumn annotation, Object value) {
        FieldConverter<?> fieldConverter = FieldConverterProvider.getFieldConverter(field);

        try {
            if (value != null) {
                return fieldConverter != null ? fieldConverter.tryParse(field, annotation, value) : value;
            } else if (annotation.defaultInvalidValues()) {
                return fieldConverter.getDefaultValue();
            }
        } catch (Exception e) {
            if (!annotation.suppressErrors()) {
                throw new ExcelFieldParseException(String.format("Failed to parse '%s' into field '%s.%s'", value, field.getDeclaringClass().getSimpleName(), field.getName()), e);
            }
        }

        return null;
    }
}
