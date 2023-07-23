package io.github.ferhas.excel_models;

import jakarta.validation.*;

import java.util.Set;

class ExcelReaderValidator {
    private final ValidatorFactory factory;

    public ExcelReaderValidator() {
        this.factory = Validation.buildDefaultValidatorFactory();
    }

    public <T> void validate(T model) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            Set<ConstraintViolation<T>> violations = validator.validate(model);

            if (!violations.isEmpty()) {
                ConstraintViolation<T> violation = violations.iterator().next();
                throw new ValidationException(String.format(
                        "field '%s' (%s): %s",
                        violation.getPropertyPath().toString(),
                        violation.getInvalidValue(),
                        violation.getMessage()
                ));
            }
        }
    }

    public void close() {
        this.factory.close();
    }
}
