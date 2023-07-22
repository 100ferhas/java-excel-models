package io.github.ferhas.excel_models;

import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.ExcelObject;
import io.github.ferhas.excel_models.converter.FieldConverter;
import io.github.ferhas.excel_models.exception.ExcelModelParseException;
import org.apache.poi.ss.usermodel.Cell;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

abstract class ExcelUtils {
    public static <E> Map<Annotation, Field> getFieldMap(Class<E> type, boolean isExport) {
        Map<Annotation, Field> fieldMap = new HashMap<>();

        for (Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(ExcelColumn.class)) {
                field.setAccessible(true);
                ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);

                if (!(annotation.onlyExport() && !isExport)) {
                    fieldMap.put(annotation, field);
                }
            } else if (field.isAnnotationPresent(ExcelObject.class)) {
                field.setAccessible(true);
                ExcelObject annotation = field.getAnnotation(ExcelObject.class);
                fieldMap.put(annotation, field);
            }
        }

        if (fieldMap.isEmpty()) {
            throw new IllegalArgumentException(String.format("Model '%s' has not been configured? check if you inserted @%s annotations", type.getSimpleName(), ExcelColumn.class.getSimpleName()));
        }

        return fieldMap;
    }

    static Object getCellValue(Cell cell, Field field, ExcelColumn annotation) {
        Object value;

        switch (cell.getCellType()) {
            case ERROR:
            case FORMULA:
                return null; // IGNORE!
            case NUMERIC:
                value = cell.getNumericCellValue();
                break;
            case BOOLEAN:
                value = cell.getBooleanCellValue();
                break;
            case STRING:
                value = cell.getStringCellValue();
                break;
            case _NONE:
            case BLANK:
            default:
                value = null;
        }

        return tryConvert(field, annotation, value);
    }

    private static Object tryConvert(Field field, ExcelColumn annotation, Object value) {
        try {
            Object fieldType = field.getType().isEnum() ? Enum.class : field.getType();
            FieldConverter<?> fieldConverter = FieldConverterProvider.converters.get(fieldType);
            return fieldConverter == null ? value : fieldConverter.tryParse(field, annotation, value);
        } catch (ExcelModelParseException e) {
            if (!annotation.suppressErrors()) {
                throw new IllegalArgumentException(String.format("Failed to parse '%s' into field '%s.%s'", value, field.getDeclaringClass().getSimpleName(), field.getName()));
            }
            return null;
        }
    }
}
