import it.excel_models.ExcelParser;
import it.excel_models.ExcelWriter;
import it.excel_models.config.ExcelParserConfig;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class ParseTest {

    @Test
    public void testParseandWrite() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("StimaInnovaFase2Sprint2.xlsx")) {
            ExcelParserConfig config = new ExcelParserConfig().withSheetIndex(2).withHeaderOffset(2).withFooterIndex(19);
            List<EstimateRow> estimateRows = new ExcelParser(config).parse(is, EstimateRow.class, estimateRow -> {
                EstimateDetail detail = estimateRow.getDetail();
                detail.setTotal(detail.getBackEnd() + detail.getFrontEnd());
                return estimateRow;
            });
            System.out.println(estimateRows);

            try (FileOutputStream os = new FileOutputStream("StimaInnovaFase2Sprint2_OUT.xlsx")) {
                new ExcelWriter().write(estimateRows, os);
            }
        }
    }
}
