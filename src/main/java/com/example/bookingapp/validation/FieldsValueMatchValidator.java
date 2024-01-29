package com.example.bookingapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.Objects;

public class FieldsValueMatchValidator implements ConstraintValidator<FieldsValueMatch, Object> {
    private String field;
    private String fieldMatch;

    public void initialize(FieldsValueMatch constraintAnnotation) {
        this.field = constraintAnnotation.field();

        this.fieldMatch = constraintAnnotation.fieldMatch();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        try {
            Field fieldValue = value.getClass().getDeclaredField(this.field);
            Field fieldMatchValue = value.getClass().getDeclaredField(this.fieldMatch);
            fieldValue.setAccessible(true);
            fieldMatchValue.setAccessible(true);
            Object firstValue = fieldValue.get(value);
            Object secondValue = fieldMatchValue.get(value);
            return Objects.equals(firstValue, secondValue);
        } catch (Exception e) {
            throw new RuntimeException("Can't get value " + value, e);
        }
    }
}
