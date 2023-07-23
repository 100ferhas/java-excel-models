package io.github.ferhas.excel_models.entities;

import io.github.ferhas.excel_models.annotation.ExcelColumn;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class Author {
    @ExcelColumn(index = 6)
    private String firstName;

    @ExcelColumn(index = 7)
    private String lastName;

    @NotNull
    private String fullName;

    @ExcelColumn(index = 8)
    private Date dateOfBirth;

    @ExcelColumn(index = 9, suppressErrors = true)
    private AuthorGender gender;
}
