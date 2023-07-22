package io.github.ferhas.excel_models.converter.impl;

import io.github.ferhas.excel_models.exception.ExcelModelParseException;
import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.TypeConverter;
import io.github.ferhas.excel_models.converter.FieldConverter;

import java.lang.reflect.Field;

@TypeConverter(forTypes = {Integer.class, int.class})
class IntegerConverter implements FieldConverter<Integer> {
    @Override
    public Integer tryParse(Field field, ExcelColumn annotation, Object value) throws ExcelModelParseException {
        try {
            return Double.valueOf(String.valueOf(value)).intValue();
        } catch (IllegalArgumentException e) {
            if (annotation.defaultInvalidValues()) {
                return 0;
            }

            throw new ExcelModelParseException();
        }
    }
}
