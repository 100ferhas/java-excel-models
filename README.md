[![Publish to Maven Central](https://github.com/100ferhas/java-excel-models/actions/workflows/release.yml/badge.svg)](https://github.com/100ferhas/java-excel-models/actions/workflows/release.yml)

# Java Excel Models Converter

This is a library to convert Excel files into POJO and vice-versa using annotations on Java classes.

You can define models as POJO and annotate model fields with [Annotations](#annotations) annotation to define
reading and writing of model fields.

The library
supports [Model validation annotations](https://jakarta.ee/specifications/bean-validation/3.0/apidocs/jakarta/validation/constraints/package-summary.html)
when converting data from Excel files into models.




## Installation

### Maven
```xml
<dependency>
  <groupId>io.github.100ferhas</groupId>
  <artifactId>java-excel-models</artifactId>
  <version>0.1</version>
</dependency>
```

### Gradle
```text
implementation 'io.github.100ferhas:java-excel-models:0.1'
```




## Usage
After installation, you can start using the library to read Excel files into Java models, or to write Java models into 
Excel files. Look at the following demo class showing different ways on how you can use the library.

```java
import java.util.function.Consumer;

public class Demo {

    public void readFile() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("./test_in.xlsx")) {
            // reader config
            ExcelReaderConfig config = new ExcelReaderConfig()
                    .withSheetIndex(2)
                    .withHeaderOffset(1);

            // create an ExcelReader with optional configuration
            ExcelReader reader = new ExcelReader(config);

            // read file content into models passing InputStream of the file and the type of the model to be converted into
            List<Book> books = reader.parse(is, Book.class);

            // you can also pass a Consumer that execute extra processing after the model has been parsed
            Consumer<Book> postReadConsumer = parsedBook -> {
                Author author = parsedBook.getAuthor();
                String authorFullName = String.format("%s %s", author.getFirstName(), author.getLastName());
                author.setFullName(authorFullName);
            };

            // parse file with the post-read consumer
            books = reader.parse(is, Book.class, postReadConsumer);
        }
    }

    public void writeFile() throws Exception {
        // your models
        List<Book> books = List.of();

        // create a writer with optional configuration
        ExcelWriter writer = new ExcelWriter(
                new ExcelWriterConfig()
                        .withSheetName("Sheet Name")
                        .withHeaderStyleBuilder((workbook) -> {
                            // build style for header
                            // NOTE: not used when headerBuilder is provided
                            CellStyle cellStyle = workbook.createCellStyle();
                            cellStyle.setWrapText(true);
                            return cellStyle;
                        })
                        .withContentStyleBuilder((workbook) -> {
                            // build style for content cells
                            CellStyle cellStyle = workbook.createCellStyle();
                            cellStyle.setWrapText(true);
                            return cellStyle;
                        })
                        .withHeaderBuilder((workbook, sheet) -> {
                            // custom-built header
                        })
                        .withFooterBuilder((workbook, sheet) -> {
                            // custom-built footer
                        })
        );

        // write model into an OutputStream
        try (OutputStream baos = writer.write(books)) {
            // handle your output stream
        } catch (IOException e) {
            // handle exception
        }

        // write models on a file
        writer.write(books, new FileOutputStream("./test_out.xlsx"));
    }
}
```




## Annotations

### @ExcelColumn

Annotation to be used on object properties to map with Excel file structure and can define other behaviour.

| Parameter                | Type    | Required | Description                                                   | Default Value | Read or Write usage |
|--------------------------|---------|----------|---------------------------------------------------------------|---------------|---------------------|
| **index**                | int     | yes      | Index of the column in the file                               |               | Both                |
| **title**                | String  | no       | Title of the column created during write file                 | field name    | Write               |
| **defaultInvalidValues** | boolean | no       | Assign a default value to an invalid data in a column **(*)** | false         | Read                |
| **onlyExport**           | boolean | no       | Mark the field as "export only" ignoring it during read       | false         | Write               |
| **suppressErrors**       | boolean | no       | Suppress errors parsing the field **(*)**                     | false         | Read                |

**(*) If you configure to not suppress errors, it won't assign a default value to the field also if configured so.**

### @ExcelObject

This is a plain annotation to be used on nested objects that maps additional columns.

For example, has been used on [Author](#author) model that is a property of [Book](#book) objects.

### @TypeConverter

Annotation needed on [Custom Field Converters](#field-converters) to register it at the application startup or at least
before reading data from files.

| Parameter    | Type       | Required | Description                                       | Default Value | Read or Write usage |
|--------------|------------|----------|---------------------------------------------------|---------------|---------------------|
| **forTypes** | Class<?>[] | yes      | Destination data types to apply this converter to |               | Read                |




## Field Converters

There are some already defined `FieldConverters` to try to convert value from a data cell into an object attribute type.

For example, if you want to read a cell that contains `UUID` and parse it directly into the model field declared
as `UUID` there is already a converter handling this.

These are the pre-defined converters, more converter will be added over time (if you want to contribute just create a PR):

| Data Type           | Default parsed value (if configured) |
|---------------------|--------------------------------------|
| `boolean`/`Boolean` | `false`                              |
| `int`/`Integer`     | 0                                    |
| `double`/`Double`   | 0                                    |
| `Date`              | `null`                               |
| `Enum`              | `null`                               |
| `UUID`              | `null`                               |


You can define your own additional field converter and register it on your application startup, for example if you want 
to define a converter for your `BigInteger` fields, create a class as below.

You must annotate it with [@TypeConverter](#typeconverter) annotation, and it also must implement `FieldConverter`
interface to make it override the method where you will define your own conversion logic, and, if you want, the method
where you will define your own default return value.

```java

import java.math.BigInteger;

@TypeConverter(forTypes = {BigInteger.class})
public class CustomFieldConverter implements FieldConverter<BigInteger> {
    @Override
    public BigInteger tryParse(Field field, ExcelColumn annotation, Object value) {
        // your converter algorithm
        return BigInteger.ONE;
    }

    // if you want to configure a default return value, just override this method. Return as default 'null' 
    @Override
    public BigInteger getDefaultValue() {
        return BigInteger.ZERO;
    }
}
```

Then you just need to register the new converter when your application starts or at least before reading data from a
file:

```java
public class AppInit {
    public static void main(String[] args) {
        // register a single converter
        FieldConverterProvider.registerAdditionalConverter(CustomFieldConverter.class);

        // register multiple converters at once
        Set<Class<? extends FieldConverter<?>>> converters = Set.of(CustomFieldConverter.class, AnotherCustomFieldConverter.class);
        FieldConverterProvider.registerAdditionalConverters(converters);
    }
}
```

**NB: IF YOU REGISTER A CUSTOM CONVERTER ANNOTATED WITH SAME TYPE AS A PRE-DEFINED ONE, YOU WILL OVERRIDE THE DEFAULT.**




## Example Models

### Book

```java
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
```

### Author

```java
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
```

### AuthorGender

```java
public enum AuthorGender {
    MALE,
    FEMALE
}
```




**NOTE: This library is still under development!**

**Please feel free to open issues if you find some bug or if you want to suggest improvements, PR are also welcome.**

**Please star the repository for visibility if you find this library helpful.** 
