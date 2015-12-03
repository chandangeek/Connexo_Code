package com.energyict.protocolimplv2.sdksample;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link SDKFirmwareDialectProperties} component.
 */
public class SDKFirmwareDialectPropertiesTest {

    public static final int MAX_COLUMN_NAME_LENGTH = 30;

    @Test
    public void javaNameIsNotNull() {
        List<SDKFirmwareDialectProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(SDKFirmwareDialectProperties.ActualFields.values())
                .filter(field -> field.javaName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<SDKFirmwareDialectProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(SDKFirmwareDialectProperties.ActualFields.values())
                .filter(field -> field.javaName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<SDKFirmwareDialectProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(SDKFirmwareDialectProperties.ActualFields.values())
                .filter(field -> this.fieldDoesNotExists(field.javaName()))
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        try {
            return SDKFirmwareDialectProperties.class.getField(fieldName) == null;
        }
        catch (NoSuchFieldException e) {
            return false;
        }
    }

    @Test
    public void propertySpecNameIsNotNull() {
        List<SDKFirmwareDialectProperties.ActualFields> fieldsWithNullPropertySpecName =
            Stream
                .of(SDKFirmwareDialectProperties.ActualFields.values())
                .filter(field -> field.propertySpecName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void propertySpecNameIsNotEmpty() {
        List<SDKFirmwareDialectProperties.ActualFields> fieldsWithNullPropertySpecName =
            Stream
                .of(SDKFirmwareDialectProperties.ActualFields.values())
                .filter(field -> field.propertySpecName().toString().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void databaseNameIsNotNull() {
        List<SDKFirmwareDialectProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(SDKFirmwareDialectProperties.ActualFields.values())
                .filter(field -> field.databaseName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<SDKFirmwareDialectProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(SDKFirmwareDialectProperties.ActualFields.values())
                .filter(field -> field.databaseName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<SDKFirmwareDialectProperties.ActualFields> fieldsWithTooLongDatabaseName =
            Stream
                .of(SDKFirmwareDialectProperties.ActualFields.values())
                .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

}