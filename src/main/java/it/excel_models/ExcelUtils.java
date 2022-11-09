package it.excel_models;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ExcelUtils {
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
        if (field.getType().isAssignableFrom(double.class) || field.getType().isAssignableFrom(Double.class)) {
            return tryParseDouble(field, annotation, value);
        } else if (field.getType().isAssignableFrom(int.class) || field.getType().isAssignableFrom(Integer.class)) {
            return tryParseInteger(field, annotation, value);
        } else if (field.getType().isAssignableFrom(Date.class)) {
            return tryParseDate(field, value);
        }

        return value;
    }

    private static double tryParseDouble(Field field, ExcelColumn annotation, Object value) {
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (IllegalArgumentException e) {
            if (annotation.defaultInvalidValues()) {
                return 0;
            }

            throw new IllegalArgumentException(String.format("Failed to parse '%s' into %s", value, field.getType()));
        }
    }

    private static int tryParseInteger(Field field, ExcelColumn annotation, Object value) {
        try {
            return Double.valueOf(String.valueOf(value)).intValue();
        } catch (IllegalArgumentException e) {
            if (annotation.defaultInvalidValues()) {
                return 0;
            }

            throw new IllegalArgumentException(String.format("Failed to parse '%s' into %s", value, field.getType()));
        }
    }

    private static Date tryParseDate(Field field, Object value) {
        try {
            return DateUtil.getJavaDate((Double) value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Failed to parse '%s' into %s", value, field.getType()));
        }
    }
}
