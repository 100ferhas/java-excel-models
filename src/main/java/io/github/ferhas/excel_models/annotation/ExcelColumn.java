package io.github.ferhas.excel_models.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelColumn {
    int index(); // column index, starting from 1

    String title() default ""; // column title

    boolean defaultInvalidValues() default false; // try to assign default value on invalid column value

    boolean onlyExport() default false; // if it has to be included in export but not for import

    boolean suppressErrors() default false; // If it has to suppress parsing errors
}
