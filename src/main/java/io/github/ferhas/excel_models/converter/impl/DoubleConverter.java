package io.github.ferhas.excel_models.converter.impl;

import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.TypeConverter;
import io.github.ferhas.excel_models.converter.FieldConverter;

import java.lang.reflect.Field;

@TypeConverter(forTypes = {Double.class, double.class})
public class DoubleConverter implements FieldConverter<Double> {
    @Override
    public Double tryParse(Field field, ExcelColumn annotation, Object value) {
        return Double.parseDouble(String.valueOf(value));
    }

    @Override
    public Double getDefaultValue() {
        return 0D;
    }
}
