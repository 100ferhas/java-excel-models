package it.excel_models;

import it.excel_models.config.ExcelColumn;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Cell;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Utils {
    static <E> Map<ExcelColumn, Field> getFieldMap(Class<E> type) {
        Map<ExcelColumn, Field> fieldMap = new HashMap<>();

        for (Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(ExcelColumn.class)) {
                field.setAccessible(true);
                ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);

                if (fieldMap.containsKey(annotation)) {
                    throw new IllegalArgumentException(String.format("Duplicated column index detected, check your configuration on model %s.", type.getSimpleName()));
                }

                fieldMap.put(annotation, field);
            }
        }

        if (fieldMap.isEmpty()) {
            throw new IllegalArgumentException(String.format("Model '%s' has not been configured? check if you inserted @%s annotations", type.getSimpleName(), ExcelColumn.class.getSimpleName()));
        }

        // todo sort by index

        return fieldMap;
    }

    static Object getCellValue(Cell cell, Field field) {
        Class<?> type = field.getType();

        // todo estendere con altri type se necessario
        if (Date.class.equals(type)) {
            return cell.getDateCellValue();
        } else if (Long.class.equals(type)) {
            return Long.valueOf(cell.getStringCellValue());
        } else {
            return cell.getStringCellValue();
        }
    }

    /**
     * Get type of collection field.
     */
    @SneakyThrows
    static Class<?> getCollectionType(List<?> list) {
        return ((Class<?>) ((ParameterizedType) list.getClass().getGenericSuperclass()).getActualTypeArguments()[1]);
    }
}
