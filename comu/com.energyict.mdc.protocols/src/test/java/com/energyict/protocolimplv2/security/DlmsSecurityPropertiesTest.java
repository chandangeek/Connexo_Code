package com.energyict.protocolimplv2.security;

import com.energyict.CustomPropertiesPersistenceTest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link DlmsSecurityProperties} component.
 */
public class DlmsSecurityPropertiesTest extends CustomPropertiesPersistenceTest {

    @Test
    public void javaNameIsNotNull() {
        List<DlmsSecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(DlmsSecurityProperties.ActualFields.values())
                .filter(field -> field.javaName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<DlmsSecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(DlmsSecurityProperties.ActualFields.values())
                .filter(field -> field.javaName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<DlmsSecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(DlmsSecurityProperties.ActualFields.values())
                .filter(field -> this.fieldDoesNotExists(field.javaName()))
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        return this.fieldDoesNotExists(DlmsSecurityProperties.class, fieldName);
    }

    @Test
    public void checkJavaxAnnotationsOnFields() {
        this.checkJavaxAnnotationsOnFields(DlmsSecurityProperties.class);
    }

    @Test
    public void databaseNameIsNotNull() {
        List<DlmsSecurityProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(DlmsSecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<DlmsSecurityProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(DlmsSecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<DlmsSecurityProperties.ActualFields> fieldsWithTooLongDatabaseName =
            Stream
                .of(DlmsSecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

}