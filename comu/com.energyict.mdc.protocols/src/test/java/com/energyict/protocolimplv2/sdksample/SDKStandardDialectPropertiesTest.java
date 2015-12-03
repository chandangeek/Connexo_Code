package com.energyict.protocolimplv2.sdksample;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link SDKStandardDialectProperties} component.
 */
public class SDKStandardDialectPropertiesTest {

    public static final int MAX_COLUMN_NAME_LENGTH = 30;

    @Test
    public void javaNameIsNotNull() {
        List<SDKStandardDialectProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(SDKStandardDialectProperties.ActualFields.values())
                .filter(field -> field.javaName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<SDKStandardDialectProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(SDKStandardDialectProperties.ActualFields.values())
                .filter(field -> field.javaName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<SDKStandardDialectProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(SDKStandardDialectProperties.ActualFields.values())
                .filter(field -> this.fieldDoesNotExists(field.javaName()))
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        try {
            return SDKStandardDialectProperties.class.getField(fieldName) == null;
        }
        catch (NoSuchFieldException e) {
            return false;
        }
    }

    @Test
    public void propertySpecNameIsNotNull() {
        List<SDKStandardDialectProperties.ActualFields> fieldsWithNullPropertySpecName =
            Stream
                .of(SDKStandardDialectProperties.ActualFields.values())
                .filter(field -> field.propertySpecName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void propertySpecNameIsNotEmpty() {
        List<SDKStandardDialectProperties.ActualFields> fieldsWithNullPropertySpecName =
            Stream
                .of(SDKStandardDialectProperties.ActualFields.values())
                .filter(field -> field.propertySpecName().toString().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void databaseNameIsNotNull() {
        List<SDKStandardDialectProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(SDKStandardDialectProperties.ActualFields.values())
                .filter(field -> field.databaseName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<SDKStandardDialectProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(SDKStandardDialectProperties.ActualFields.values())
                .filter(field -> field.databaseName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<SDKStandardDialectProperties.ActualFields> fieldsWithTooLongDatabaseName =
            Stream
                .of(SDKStandardDialectProperties.ActualFields.values())
                .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

}