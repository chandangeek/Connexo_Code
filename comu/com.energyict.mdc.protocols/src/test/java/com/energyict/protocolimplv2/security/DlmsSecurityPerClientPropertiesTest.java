package com.energyict.protocolimplv2.security;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link DlmsSecurityPerClientProperties} component
 */
public class DlmsSecurityPerClientPropertiesTest {

    public static final int MAX_COLUMN_NAME_LENGTH = 30;

    @Test
    public void javaNameIsNotNull() {
        List<DlmsSecurityPerClientProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(DlmsSecurityPerClientProperties.ActualFields.values())
                .filter(field -> field.javaName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<DlmsSecurityPerClientProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(DlmsSecurityPerClientProperties.ActualFields.values())
                .filter(field -> field.javaName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<DlmsSecurityPerClientProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(DlmsSecurityPerClientProperties.ActualFields.values())
                .filter(field -> this.fieldDoesNotExists(field.javaName()))
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        try {
            return DlmsSecurityPerClientProperties.class.getField(fieldName) == null;
        }
        catch (NoSuchFieldException e) {
            return false;
        }
    }

    @Test
    public void propertySpecNameIsNotNull() {
        List<DlmsSecurityPerClientProperties.ActualFields> fieldsWithNullPropertySpecName =
            Stream
                .of(DlmsSecurityPerClientProperties.ActualFields.values())
                .filter(field -> field.propertySpecName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void propertySpecNameIsNotEmpty() {
        List<DlmsSecurityPerClientProperties.ActualFields> fieldsWithNullPropertySpecName =
            Stream
                .of(DlmsSecurityPerClientProperties.ActualFields.values())
                .filter(field -> field.propertySpecName().toString().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void databaseNameIsNotNull() {
        List<DlmsSecurityPerClientProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(DlmsSecurityPerClientProperties.ActualFields.values())
                .filter(field -> field.databaseName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<DlmsSecurityPerClientProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(DlmsSecurityPerClientProperties.ActualFields.values())
                .filter(field -> field.databaseName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<DlmsSecurityPerClientProperties.ActualFields> fieldsWithTooLongDatabaseName =
            Stream
                .of(DlmsSecurityPerClientProperties.ActualFields.values())
                .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

}