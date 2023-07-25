package io.github.ferhas.excel_models.converter.impl;

import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.TypeConverter;
import io.github.ferhas.excel_models.converter.FieldConverter;

import java.lang.reflect.Field;

@TypeConverter(forTypes = {long.class, Long.class})
class LongConverter implements FieldConverter<Long> {
    @Override
    public Long tryParse(Field field, ExcelColumn annotation, Object value) {
        return Double.valueOf(String.valueOf(value)).longValue();
    }

    @Override
    public Long getDefaultValue() {
        return 0L;
    }
}
