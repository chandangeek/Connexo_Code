package com.energyict.protocolimplv2.securitysupport;

import com.energyict.CustomPropertiesPersistenceTest;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link MTU155SecurityProperties} component.
 */
public class MTU155SecurityPropertiesTest extends CustomPropertiesPersistenceTest {

    @Test
    public void javaNameIsNotNull() {
        List<MTU155SecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(MTU155SecurityProperties.ActualFields.values())
                .filter(field -> field.javaName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<MTU155SecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(MTU155SecurityProperties.ActualFields.values())
                .filter(field -> field.javaName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<MTU155SecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(MTU155SecurityProperties.ActualFields.values())
                .filter(field -> this.fieldDoesNotExists(field.javaName()))
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        return this.fieldDoesNotExists(MTU155SecurityProperties.class, fieldName);
    }

    @Test
    public void checkJavaxAnnotationsOnFields() {
        this.checkJavaxAnnotationsOnFields(MTU155SecurityProperties.class);
    }

    @Test
    public void propertySpecNameIsNotNull() {
        List<MTU155SecurityProperties.ActualFields> fieldsWithNullPropertySpecName =
            Stream
                .of(MTU155SecurityProperties.ActualFields.values())
                .filter(field -> field.propertySpecName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void propertySpecNameIsNotEmpty() {
        List<MTU155SecurityProperties.ActualFields> fieldsWithNullPropertySpecName =
            Stream
                .of(MTU155SecurityProperties.ActualFields.values())
                .filter(field -> field.propertySpecName().toString().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void databaseNameIsNotNull() {
        List<MTU155SecurityProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(MTU155SecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<MTU155SecurityProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(MTU155SecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<MTU155SecurityProperties.ActualFields> fieldsWithTooLongDatabaseName =
            Stream
                .of(MTU155SecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

}