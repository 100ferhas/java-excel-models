package io.github.ferhas.excel_models.converter.impl;

import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.TypeConverter;
import io.github.ferhas.excel_models.converter.FieldConverter;

import java.lang.reflect.Field;
import java.util.UUID;

@TypeConverter(forTypes = {UUID.class})
public class UUIDConverter implements FieldConverter<UUID> {
    @Override
    public UUID tryParse(Field field, ExcelColumn annotation, Object value) {
        return UUID.fromString(String.valueOf(value));
    }
}
