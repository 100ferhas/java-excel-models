package io.github.ferhas.excel_models.converter.impl;

import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.TypeConverter;
import io.github.ferhas.excel_models.converter.FieldConverter;

import java.lang.reflect.Field;
import java.math.BigDecimal;

@TypeConverter(forTypes = {BigDecimal.class})
public class BigDecimalConverter implements FieldConverter<BigDecimal> {
    @Override
    public BigDecimal tryParse(Field field, ExcelColumn annotation, Object value) {
        return BigDecimal.valueOf(Double.parseDouble(String.valueOf(value)));
    }

    @Override
    public BigDecimal getDefaultValue() {
        return BigDecimal.ZERO;
    }
}
