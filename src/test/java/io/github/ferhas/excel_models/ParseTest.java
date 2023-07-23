package io.github.ferhas.excel_models;

import io.github.ferhas.excel_models.config.ExcelReaderConfig;
import io.github.ferhas.excel_models.entities.Author;
import io.github.ferhas.excel_models.entities.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseTest {

    @Test
    @DisplayName("Parse xlsx file into model")
    public void testParseFile() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("./parse_test_file.xlsx")) {
            // parse config
            ExcelReaderConfig config = new ExcelReaderConfig()
                    .withSheetIndex(2)
                    .withHeaderOffset(1);

            // parse
            List<Book> books = new ExcelReader(config).parse(Objects.requireNonNull(is), Book.class, parsedBook -> {
                Author author = parsedBook.getAuthor();
                String authorFullName = author.getFirstName() + author.getLastName();
                author.setFullName(authorFullName.trim());
            });

            assertEquals(3, books.size(), "Unexpected results number!");
            assertEquals("Marcel de la vega", books.get(2).getAuthor().getFullName());
            // todo other test assertions

        }
    }

    // todo other test cases
}
