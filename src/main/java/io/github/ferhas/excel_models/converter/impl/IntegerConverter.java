package io.github.ferhas.excel_models.converter.impl;

import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.TypeConverter;
import io.github.ferhas.excel_models.converter.FieldConverter;

import java.lang.reflect.Field;

@TypeConverter(forTypes = {Integer.class, int.class})
public class IntegerConverter implements FieldConverter<Integer> {
    @Override
    public Integer tryParse(Field field, ExcelColumn annotation, Object value) {
        // we do not parse integers because excel returns doubles for numeric type cells
        return Double.valueOf(String.valueOf(value)).intValue();
    }

    @Override
    public Integer getDefaultValue() {
        return 0;
    }
}
