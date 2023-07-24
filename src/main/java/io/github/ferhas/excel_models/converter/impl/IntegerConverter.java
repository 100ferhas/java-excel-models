package io.github.ferhas.excel_models.converter.impl;

import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.TypeConverter;
import io.github.ferhas.excel_models.converter.FieldConverter;

import java.lang.reflect.Field;

@TypeConverter(forTypes = {Integer.class, int.class})
class IntegerConverter implements FieldConverter<Integer> {
    @Override
    public Integer tryParse(Field field, ExcelColumn annotation, Object value) {
        return Integer.parseInt(String.valueOf(value));
    }

    @Override
    public Integer getDefaultValue() {
        return 0;
    }
}
