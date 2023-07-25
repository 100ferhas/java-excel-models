package io.github.ferhas.excel_models.converter.impl;

import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.TypeConverter;
import io.github.ferhas.excel_models.converter.FieldConverter;

import java.lang.reflect.Field;
import java.math.BigInteger;

@TypeConverter(forTypes = {BigInteger.class})
public class BigIntegerConverter implements FieldConverter<BigInteger> {
    @Override
    public BigInteger tryParse(Field field, ExcelColumn annotation, Object value) {
        return BigInteger.valueOf(Long.parseLong(String.valueOf(value)));
    }

    @Override
    public BigInteger getDefaultValue() {
        return BigInteger.ZERO;
    }
}
