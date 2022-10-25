package it.excel_models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.function.Consumer;
import java.util.function.Function;

@Data
@With
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExcelWriterConfig {
    private String sheetName;
    private Function<Workbook, CellStyle> headerStyleBuilder;
    private Function<Workbook, CellStyle> contentStyleBuilder;
    private Consumer<Sheet> footerBuilder;
}
