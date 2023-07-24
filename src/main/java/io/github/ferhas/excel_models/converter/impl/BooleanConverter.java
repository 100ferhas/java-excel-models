package io.github.ferhas.excel_models.converter.impl;

import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.TypeConverter;
import io.github.ferhas.excel_models.converter.FieldConverter;

import java.lang.reflect.Field;

@TypeConverter(forTypes = {boolean.class, Boolean.class})
public class BooleanConverter implements FieldConverter<Boolean> {
    @Override
    public Boolean tryParse(Field field, ExcelColumn annotation, Object value) {
        return Boolean.valueOf(String.valueOf(value));
    }

    @Override
    public Boolean getDefaultValue() {
        return false;
    }
}
