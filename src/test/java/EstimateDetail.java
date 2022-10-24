import it.excel_models.config.ExcelColumn;
import lombok.Data;

@Data
public class EstimateDetail {
    @ExcelColumn(index = 5, title = "FE", defaultInvalidValues = true)
    private double frontEnd;

    @ExcelColumn(index = 6, title = "BE", defaultInvalidValues = true)
    private double backEnd;

    private double total;
}
