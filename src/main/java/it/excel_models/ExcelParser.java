package it.excel_models;

import it.excel_models.config.ExcelColumn;
import it.excel_models.config.ExcelParserConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ExcelParser {
    private final ExcelParserConfig config;

    public ExcelParser() {
        this.config = new ExcelParserConfig();
    }

    public ExcelParser(ExcelParserConfig config) {
        this.config = config;
    }

    public <T> List<T> parse(InputStream inputStream, final Class<T> type) {
        log.debug("Starting parse excel for '{}' model", type.getSimpleName());
        Map<ExcelColumn, Field> fieldMap = Utils.getFieldMap(type);

        List<T> resultList = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(config.getSheetIndex());

            for (Row row : sheet) {
                if (row.getRowNum() >= config.getHeaderOffset()) {
                    T model = type.getDeclaredConstructor().newInstance();

                    for (Map.Entry<ExcelColumn, Field> entry : fieldMap.entrySet()) {
                        ExcelColumn annotation = entry.getKey();
                        Cell cell = row.getCell(annotation.index());

                        if (cell != null) {
                            Field field = entry.getValue();
                            field.set(model, Utils.getCellValue(cell, field));
                        }
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
}
