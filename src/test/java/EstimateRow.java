import it.excel_models.config.ExcelColumn;
import it.excel_models.config.ExcelObject;
import lombok.Data;

@Data
public class EstimateRow {
    @ExcelColumn(index = 1, title = "Area")
    private String area;

    @ExcelColumn(index = 2, title = "Funzionalità")
    private String funzionalita;

    @ExcelColumn(index = 3)
    private String task;

    @ExcelColumn(index = 4, title = "Note")
    private String note;

    @ExcelObject
    private EstimateDetail detail;
}
