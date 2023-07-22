package io.github.ferhas.excel_models.converter;

import io.github.ferhas.excel_models.exception.ExcelModelParseException;
import io.github.ferhas.excel_models.annotation.ExcelColumn;

import java.lang.reflect.Field;

public interface FieldConverter<T> {
    T tryParse(Field field, ExcelColumn annotation, Object value) throws ExcelModelParseException;
}
