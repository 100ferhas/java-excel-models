import it.excel_models.ExcelColumn;
import lombok.Data;

import java.util.Date;

@Data
public class FinancialModel {
    @ExcelColumn(index = 1)
    private String segment;

    @ExcelColumn(index = 2)
    private String country;

    @ExcelColumn(index = 3)
    private String product;

    @ExcelColumn(index = 4)
    private String discountBand;

    @ExcelColumn(index = 5)
    private double unitsSold;

    @ExcelColumn(index = 6)
    private double manufacturingPrice;

    @ExcelColumn(index = 7)
    private double salePrice;

    @ExcelColumn(index = 8)
    private double grossSales;

    @ExcelColumn(index = 9, defaultInvalidValues = true)
    private double discounts;

    @ExcelColumn(index = 10)
    private double sales;

    @ExcelColumn(index = 11)
    private double cogs;

    @ExcelColumn(index = 12)
    private double profit;

    @ExcelColumn(index = 13)
    private Date date;

    @ExcelColumn(index = 14)
    private int monthNumber;

    @ExcelColumn(index = 15)
    private String monthName;

    @ExcelColumn(index = 16)
    private int year;
}
