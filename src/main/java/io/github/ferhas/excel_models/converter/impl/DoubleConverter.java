package io.github.ferhas.excel_models.converter.impl;

import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.TypeConverter;
import io.github.ferhas.excel_models.converter.FieldConverter;
import io.github.ferhas.excel_models.exception.ExcelModelParseException;

import java.lang.reflect.Field;

@TypeConverter(forTypes = {Double.class, double.class})
class DoubleConverter implements FieldConverter<Double> {
    @Override
    public Double tryParse(Field field, ExcelColumn annotation, Object value) {
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (IllegalArgumentException e) {
            if (annotation.defaultInvalidValues()) {
                return 0D;
            }

            throw new ExcelModelParseException(e);
        }
    }
}
