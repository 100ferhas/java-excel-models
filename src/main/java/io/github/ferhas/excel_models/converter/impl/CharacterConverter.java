package io.github.ferhas.excel_models.converter.impl;

import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.TypeConverter;
import io.github.ferhas.excel_models.converter.FieldConverter;

import java.lang.reflect.Field;

@TypeConverter(forTypes = {char.class, Character.class})
public class CharacterConverter implements FieldConverter<Character> {
    @Override
    public Character tryParse(Field field, ExcelColumn annotation, Object value) {
        return value.toString().trim().charAt(0);
    }
}