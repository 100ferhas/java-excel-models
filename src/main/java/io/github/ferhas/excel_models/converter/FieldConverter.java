package io.github.ferhas.excel_models.converter;

import io.github.ferhas.excel_models.annotation.ExcelColumn;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public interface FieldConverter<T> {
    T tryParse(Field field, ExcelColumn annotation, Object value);

    default T getDefaultValue() {
        return null;
    }

    default String toExcelValue(@Nullable Object value) {
        return value != null ? value.toString() : null;
    }
}
