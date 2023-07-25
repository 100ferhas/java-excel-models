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
  <version>0.4</version>
</dependency>
```

### Gradle
```text
implementation 'io.github.100ferhas:java-excel-models:0.4'
```




## Usage
After installation, you can start using the library to read Excel files into Java models, or to write Java models into 
Excel files. Look at the following demo class showing different ways on how you can use the library.

```java
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

        // create a writer with OPTIONAL configuration
        ExcelWriter writer = new ExcelWriter(
                new ExcelWriterConfig()
                        .withSheetName("Sheet Name")
                        .withHeaderStyleBuilder(workbook -> {
                            // build custom style for header
                            // NOTE: not used when headerBuilder is provided
                            CellStyle cellStyle = workbook.createCellStyle();
                            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                            cellStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
                            return cellStyle;
                        })
                        .withContentRowStyleBuilder((workbook, rowNum) -> {
                            // build custom style for content
                            // for example, if you want to style differently odd and even rows
                            CellStyle style = workbook.createCellStyle();
                            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                            if (rowNum % 2 == 0) {
                                style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
                            } else {
                                style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                            }

                            return style;
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

Annotation to be used on object's attributes to map it with the Excel file structure. You can also define other behaviours,
for all the available configurations refer to the following table.

| Parameter                | Type    | Required | Description                                             | Default Value | Read or Write usage |
|--------------------------|---------|----------|---------------------------------------------------------|---------------|---------------------|
| **index**                | int     | yes      | Index of the column in the file                         |               | Both                |
| **title**                | String  | no       | Title of the column created during write file           | field name    | Write               |
| **defaultInvalidValues** | boolean | no       | Assign a default value to an invalid data in a column   | false         | Read                |
| **onlyExport**           | boolean | no       | Mark the field as "export only" ignoring it during read | false         | Write               |
| **suppressErrors**       | boolean | no       | Suppress errors parsing the field                       | false         | Read                |


### @ExcelObject

This is a plain annotation to be used on nested objects that maps additional columns.

For example, has been used on [Author](#author) model that is a property of [Book](#book) objects.

### @TypeConverter

Annotation needed on [Custom Field Converters](#field-converters) to register it at the application startup or at least
before reading data from files.

| Parameter    | Type       | Required | Description                                       | Default Value | Read or Write usage |
|--------------|------------|----------|---------------------------------------------------|---------------|---------------------|
| **forTypes** | Class<?>[] | yes      | Destination data types to apply this converter to |               | Both                |




## Field Converters

`FieldConverter` interface is the abstraction of the classes that will convert that specific data type from and to 
Excel columns. Those classes will be used to convert a cell content to the specific data type you have in your model,
and to convert your data type you have in your model to a `String` value to be written in the Excel file.

There are already some defined `FieldConverter` to help you to convert value from a data cell into an object attribute type.

For example, if you want to read a cell that contains `UUID` and parse it directly into the model field declared
as `UUID` there is already a converter handling this.

These are the pre-defined converters, more converter will be added over time (if you want to contribute just create a PR):

| Converter Class         | Data Type           | Default parsed value (if configured) |
|-------------------------|---------------------|--------------------------------------|
| No Converter (built-in) | `boolean`/`Boolean` | `false`                              |
| `IntegerConverter`      | `int`/`Integer`     | 0                                    |
| `DoubleConverter`       | `double`/`Double`   | 0                                    |
| `LongConverter`         | `long`/`Long`       | 0                                    |
| `CharacterConverter`    | `char`/`Character`  | `null`                               |
| `BigIntegerConverter`   | `BigInteger`        | `BigInteger.ZERO`                    |
| `BigDecimalConverter`   | `BigDecimal`        | `BigDecimal.ZERO`                    |
| `EnumConverter`         | `Enum`              | `null`                               |
| `UUIDConverter`         | `UUID`              | `null`                               |
| `DateConverter`         | `Date`              | `null`                               |
| `CalendarConverter`     | `Calendar`          | `null`                               |
| `InstantConverter`      | `Instant`           | `null`                               |

**NOTE: `EnumConverter` will be used as a fallback only if a more specific enum converter was not found**

You can create your own additional field converter and register it on your application startup, you must annotate it 
with [@TypeConverter](#typeconverter) annotation, and it also must implement `FieldConverter` interface to override 
the `tryParse()` method where you will define your own conversion logic. 

If needed, you can also override other methods.

| Method            | Description                                                         | Default                              | Read or Write usage |
|-------------------|---------------------------------------------------------------------|--------------------------------------|---------------------|
| `tryParse`        | The method that must be implemented to define your conversion logic | no, must be implemented              | Read                |
| `getDefaultValue` | The method to define your own default return value                  | yes, returns null                    | Read                |
| `toExcelValue`    | The method to define your own value to be written in the Excel file | yes, returns to `toString()` or null | Write               |

For example, if you want to define a `FieldConverter` for your `BigInteger` fields, create a class as follows:

```java
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

    // if you want to convert your data type to be written in the Excel
    @Override
    public String toExcelValue(Object value) {
        // for example if we want to write a price in this column
        return "€" + ((BigInteger) value).doubleValue();
    }
}
```

If you prefer and need to, you can also extend an existing `FieldConverter` and override methods you want to modify.
For example lets extend the `CalendarConverter` to make it convert as we want, in this case to a formatted date:
```java
public class MyCalendarConverter extends CalendarConverter {

    @Override
    public String toExcelValue(@Nullable Object value) {
        return value != null ? DateFormatUtils.format((Calendar) value, "dd-MM-yyyy") : null;
    }
}
```

**NOTE: when you extend an existing converter is not needed to annotate it with 
[@TypeConverter](#typeconverter) annotation**

After create the new converter (also if you're extending an existing one) you just need to register it when your 
application starts or at least before reading data from a file:

```java
public class AppInit {
    public static void main(String[] args) {
        // register a single converter
        FieldConverterProvider.registerAdditionalConverter(CustomFieldConverter.class);

        // register multiple converters at once
        Set<Class<? extends FieldConverter<?>>> converters = Set.of(CustomFieldConverter.class, MyCalendarConverter.class);
        FieldConverterProvider.registerAdditionalConverters(converters);
    }
}
```

**NB: IF YOU REGISTER A CUSTOM CONVERTER ANNOTATED WITH SAME TYPE AS A PRE-DEFINED ONE, YOU WILL OVERRIDE THE DEFAULT.**

A `FieldConverter` for a class can be also defined, for example you can convert your author instance based on the data in that cell.
Let think that you have an ID in that column, all you need to do is just to define your own [FieldConverter](#field-converters)
with your own conversion logic.

```java
public class Book {
    
    // ... other omitted fields
    
    @ExcelColumn(index = 6)
    private Author author;
}
```




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
    
    // unmapped attribute, will be just ignored during both read and write
    private String soldBy;
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
