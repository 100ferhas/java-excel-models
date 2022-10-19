import it.excel_models.config.ExcelColumn;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Model {
    @ExcelColumn(index = 1, title = "col1")
    private String col1;

    @ExcelColumn(index = 2, title = "col2")
    private UUID col2;

    @ExcelColumn(index = 3, title = "col3")
    private Integer col3;

    @ExcelColumn(index = 4, title = "col4")
    private Boolean col4;

    @ExcelColumn(index = 5, title = "col5")
    private Long col5;

    @ExcelColumn(index = 6, title = "col6")
    private Double col6;

    @ExcelColumn(index = 7, title = "col7")
    private Date col7;

    @ExcelColumn(index = 8, title = "col8")
    private Instant col8;

    @ExcelColumn(index = 9, title = "col9")
    private List<Object> col9;
}
