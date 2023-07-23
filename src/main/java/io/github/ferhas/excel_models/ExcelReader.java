package io.github.ferhas.excel_models;

import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.ExcelObject;
import io.github.ferhas.excel_models.config.ExcelReaderConfig;
import io.github.ferhas.excel_models.exception.ExcelModelException;
import lombok.NonNull;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ExcelReader {
    private final ExcelReaderConfig config;

    public ExcelReader() {
        this.config = new ExcelReaderConfig();
    }

    public ExcelReader(ExcelReaderConfig config) {
        this.config = config;
    }

    public final <T> List<T> parse(@NonNull InputStream inputStream, final Class<T> type) {
        return parse(inputStream, type, null);
    }

    public final <T> List<T> parse(@NonNull InputStream inputStream, @NonNull Class<T> type, Consumer<T> afterParse) {
        List<T> resultList = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(config.getSheetIndex() - 1);

            for (Row row : sheet) {
                if (row.getRowNum() >= config.getHeaderOffset()) {
                    T model = parseModel(type, row);

                    if (afterParse != null) {
                        afterParse.accept(model);
                    }

                    resultList.add(model);
                }
            }
        } catch (Exception e) {
            throw new ExcelModelException("An error occurred while reading file.", e);
        }

        return resultList;
    }

    private <T> T parseModel(final Class<T> type, final Row row) throws Exception {
        Map<Annotation, Field> fieldMap = ExcelUtils.getFieldMap(type, false);
        T model = type.getDeclaredConstructor().newInstance();

        for (Map.Entry<Annotation, Field> entry : fieldMap.entrySet()) {
            if (entry.getKey() instanceof ExcelColumn) {
                ExcelColumn annotation = (ExcelColumn) entry.getKey();
                Cell cell = row.getCell(annotation.index() - 1);

                if (cell != null) {
                    Field field = entry.getValue();
                    field.set(model, ExcelUtils.getCellValue(cell, field, annotation));
                }
            } else if (entry.getKey() instanceof ExcelObject) {
                Field field = entry.getValue();
                Object nestedModel = parseModel(field.getType(), row);
                field.set(model, nestedModel);
            }
        }

        return model;
    }
}