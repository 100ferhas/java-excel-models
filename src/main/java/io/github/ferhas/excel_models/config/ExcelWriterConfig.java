package io.github.ferhas.excel_models.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;
import org.apache.poi.ss.usermodel.*;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@Data
@With
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExcelWriterConfig {
    private String sheetName;
    private BiConsumer<Workbook, Sheet> headerBuilder;
    private BiConsumer<Workbook, Sheet> footerBuilder;
    private Function<Workbook, CellStyle> headerStyleBuilder = workbook -> {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        cellStyle.setFont(font);

        return cellStyle;
    };
    private BiFunction<Workbook, Integer, CellStyle> contentRowStyleBuilder = (workbook, integer) -> {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);
        return cellStyle;
    };
}
