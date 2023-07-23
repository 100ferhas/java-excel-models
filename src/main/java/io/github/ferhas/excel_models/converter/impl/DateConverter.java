package io.github.ferhas.excel_models.converter.impl;

import io.github.ferhas.excel_models.exception.ExcelModelParseException;
import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.TypeConverter;
import io.github.ferhas.excel_models.converter.FieldConverter;
import org.apache.poi.ss.usermodel.DateUtil;

import java.lang.reflect.Field;
import java.util.Date;

@TypeConverter(forTypes = {Date.class})
class DateConverter implements FieldConverter<Date> {
    @Override
    public Date tryParse(Field field, ExcelColumn annotation, Object value) throws ExcelModelParseException {
        try {
            return DateUtil.getJavaDate((Double) value);
        } catch (ClassCastException | IllegalArgumentException e) {
            throw new ExcelModelParseException(e);
        }
    }
}
