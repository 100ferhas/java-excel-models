package it.excel_models.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;

@Data
@With
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExcelWriterConfig {
    private String sheetName;
}
