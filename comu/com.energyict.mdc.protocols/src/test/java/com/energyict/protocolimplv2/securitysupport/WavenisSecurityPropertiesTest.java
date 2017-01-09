package com.energyict.protocolimplv2.securitysupport;

import com.energyict.CustomPropertiesPersistenceTest;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link WavenisSecurityProperties} component.
 */
public class WavenisSecurityPropertiesTest extends CustomPropertiesPersistenceTest {

    @Test
    public void javaNameIsNotNull() {
        List<WavenisSecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(WavenisSecurityProperties.ActualFields.values())
                .filter(field -> field.javaName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<WavenisSecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(WavenisSecurityProperties.ActualFields.values())
                .filter(field -> field.javaName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<WavenisSecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(WavenisSecurityProperties.ActualFields.values())
                .filter(field -> this.fieldDoesNotExists(field.javaName()))
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        return this.fieldDoesNotExists(WavenisSecurityProperties.class, fieldName);
    }

    @Test
    public void checkJavaxAnnotationsOnFields() {
        this.checkJavaxAnnotationsOnFields(WavenisSecurityProperties.class);
    }

    @Test
    public void databaseNameIsNotNull() {
        List<WavenisSecurityProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(WavenisSecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<WavenisSecurityProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(WavenisSecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<WavenisSecurityProperties.ActualFields> fieldsWithTooLongDatabaseName =
            Stream
                .of(WavenisSecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

}