package io.github.ferhas.excel_models.converter.impl;

import io.github.ferhas.excel_models.exception.ExcelModelParseException;
import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.TypeConverter;
import io.github.ferhas.excel_models.converter.FieldConverter;

import java.lang.reflect.Field;

@TypeConverter(forTypes = {Enum.class})
class EnumConverter implements FieldConverter<Enum<?>> {
    @Override
    public Enum<?> tryParse(Field field, ExcelColumn annotation, Object value) throws ExcelModelParseException {
        Class<?> type = field.getType();

        for (Object enumConstant : type.getEnumConstants()) {
            if (enumConstant.toString().equalsIgnoreCase(value.toString())) {
                return (Enum<?>) enumConstant;
            }
        }

        throw new ExcelModelParseException();
    }
}