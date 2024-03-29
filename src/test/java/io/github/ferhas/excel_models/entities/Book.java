package io.github.ferhas.excel_models.entities;

import io.github.ferhas.excel_models.annotation.ExcelColumn;
import io.github.ferhas.excel_models.annotation.ExcelObject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class Book {
    @ExcelColumn(index = 1, suppressErrors = true)
    private UUID isbn;

    @ExcelColumn(index = 2)
    private String title;

    @PositiveOrZero
    @ExcelColumn(index = 3, title = "Price", defaultInvalidValues = true)
    private double price;

    @ExcelColumn(index = 4, suppressErrors = true)
    private Date publishedOn;

    @ExcelColumn(index = 5, title = "Number of pages", suppressErrors = true)
    private Integer numberOfPages;

    @Valid
    @ExcelObject
    private Author author;
}
