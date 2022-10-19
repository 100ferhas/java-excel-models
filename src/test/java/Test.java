import it.excel_models.ExcelParser;
import it.excel_models.ExcelWriter;
import it.excel_models.config.ExcelParserConfig;
import it.excel_models.config.ExcelWriterConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Test {

    public void test() throws FileNotFoundException {

        new ExcelParserConfig()
                .withSheetIndex(0);


        ExcelParser excelParser = new ExcelParser();
        List<HttpRequest> parse = excelParser.parse(null, HttpRequest.class);


        new ExcelWriterConfig()
                .withSheetName("");
        ExcelWriter excelWriter = new ExcelWriter();
        excelWriter.write(parse, new FileOutputStream("test"));


    }
}
