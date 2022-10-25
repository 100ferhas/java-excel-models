import it.excel_models.ExcelParser;
import it.excel_models.ExcelParserConfig;
import it.excel_models.ExcelWriter;
import it.excel_models.ExcelWriterConfig;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ParseTest {

    @Test
    public void testParseandWrite() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("StimaInnovaFase2Sprint2.xlsx")) {
            ExcelParserConfig config = new ExcelParserConfig().withSheetIndex(2).withHeaderOffset(2).withFooterIndex(19);
            List<EstimateRow> estimateRows = new ExcelParser(config).parse(is, EstimateRow.class, estimateRow -> {
                EstimateDetail detail = estimateRow.getDetail();
                detail.setTotal(detail.getBackEnd() + detail.getFrontEnd());
            });
            System.out.println(estimateRows);

            try (FileOutputStream os = new FileOutputStream("StimaInnovaFase2Sprint2_OUT.xls")) {

                Function<Workbook, CellStyle> headerStyle = workbook -> {
                    CellStyle cellStyle = workbook.createCellStyle();
                    cellStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
                    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    return cellStyle;
                };

                Consumer<Sheet> footerBuilder = sheet -> {
                    CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
                    cellStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
                    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
                    Cell cell = row.createCell(0);
                    cell.setCellValue("FOOOOOOOOOOOOOOOOTTTTTTTTTTTTTTer");
                    cell.setCellStyle(cellStyle);
                };

                ExcelWriterConfig excelWriterConfig = new ExcelWriterConfig()
                        .withHeaderStyleBuilder(headerStyle)
                        .withFooterBuilder(footerBuilder);

                new ExcelWriter(excelWriterConfig).write(estimateRows, os);
            }
        }
    }

    @Test
    public void testParseandWrite2() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("Financial Sample.xlsx")) {
            ExcelParserConfig config = new ExcelParserConfig().withHeaderOffset(1);
            List<FinancialModel> estimateRows = new ExcelParser(config).parse(is, FinancialModel.class);

            try (FileOutputStream os = new FileOutputStream("Financial Sample_OUT.xlsx")) {

                Function<Workbook, CellStyle> headerStyle = workbook -> {
                    CellStyle cellStyle = workbook.createCellStyle();
                    cellStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
                    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    return cellStyle;
                };

                Consumer<Sheet> footerBuilder = sheet -> {
                    CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
                    cellStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
                    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
                    Cell cell = row.createCell(0);
                    cell.setCellValue("Footer!!!!!!!");
                    cell.setCellStyle(cellStyle);
                };

                ExcelWriterConfig excelWriterConfig = new ExcelWriterConfig()
                        .withHeaderStyleBuilder(headerStyle)
                        .withFooterBuilder(footerBuilder);

                new ExcelWriter(excelWriterConfig).write(estimateRows, os);
            }
        }
    }
}
