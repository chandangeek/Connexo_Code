package com.energyict.protocolimplv2.securitysupport;

import com.energyict.CustomPropertiesPersistenceTest;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link DlmsSecurityPerClientProperties} component
 */
public class DlmsSecurityPerClientPropertiesTest extends CustomPropertiesPersistenceTest {

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
        return this.fieldDoesNotExists(DlmsSecurityPerClientProperties.class, fieldName);
    }

    @Test
    public void checkJavaxAnnotationsOnFields() {
        this.checkJavaxAnnotationsOnFields(DlmsSecurityPerClientProperties.class);
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
                .filter(field -> field.propertySpecName().getKey().isEmpty())
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