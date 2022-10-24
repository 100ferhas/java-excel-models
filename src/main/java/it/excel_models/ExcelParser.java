package it.excel_models;

import it.excel_models.config.ExcelColumn;
import it.excel_models.config.ExcelObject;
import it.excel_models.config.ExcelParserConfig;
import lombok.extern.log4j.Log4j2;
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
import java.util.function.Function;

@Log4j2
public class ExcelParser {
    private final ExcelParserConfig config;

    public ExcelParser() {
        this.config = new ExcelParserConfig();
    }

    public ExcelParser(ExcelParserConfig config) {
        this.config = config;
    }

    public <T> List<T> parse(InputStream inputStream, final Class<T> type) {
        return parse(inputStream, type, null);
    }

    public <T> List<T> parse(InputStream inputStream, final Class<T> type, Function<T, T> afterParse) {
        log.debug("Starting parse excel for '{}' model", type.getSimpleName());

        List<T> resultList = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(config.getSheetIndex() - 1);

            for (Row row : sheet) {
                if (row.getRowNum() >= config.getHeaderOffset()) {
                    if (row.getRowNum() >= (config.getFooterIndex() - 1)) {
                        log.info("Detected footer configuration, stopping parse");
                        break;
                    }

                    T model = parseModel(type, row);

                    if (afterParse != null) {
                        afterParse.apply(model);
                    }

                    resultList.add(model);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("An error occurred while reading file.");
        }

        log.info("File parsed successfully for '{}' model", type.getSimpleName());
        return resultList;
    }

    private <T> T parseModel(final Class<T> type, final Row row) throws Exception {
        Map<Annotation, Field> fieldMap = Utils.getFieldMap(type);
        T model = type.getDeclaredConstructor().newInstance();

        for (Map.Entry<Annotation, Field> entry : fieldMap.entrySet()) {
            if (entry.getKey() instanceof ExcelColumn) {
                ExcelColumn annotation = (ExcelColumn) entry.getKey();
                Cell cell = row.getCell(annotation.index() - 1);

                if (cell != null) {
                    Field field = entry.getValue();
                    field.set(model, Utils.getCellValue(cell, field, annotation));
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
