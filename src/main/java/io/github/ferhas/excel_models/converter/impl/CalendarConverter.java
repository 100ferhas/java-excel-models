package io.github.ferhas.excel_models.converter.impl;

import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.TypeConverter;
import io.github.ferhas.excel_models.converter.FieldConverter;
import org.apache.poi.ss.usermodel.DateUtil;

import java.lang.reflect.Field;
import java.util.Calendar;

@TypeConverter(forTypes = {Calendar.class})
public class CalendarConverter implements FieldConverter<Calendar> {
    @Override
    public Calendar tryParse(Field field, ExcelColumn annotation, Object value) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtil.getJavaDate((Double) value));
        return calendar;
    }
}
