package com.energyict.protocolimplv2.ace4000;

import com.energyict.CustomPropertiesPersistenceTest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link ACE4000DeviceProtocolDialectProperties} component.
 */
public class ACE4000DeviceProtocolDialectPropertiesTest extends CustomPropertiesPersistenceTest {

    @Test
    public void javaNameIsNotNull() {
        List<ACE4000DeviceProtocolDialectProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(ACE4000DeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.javaName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<ACE4000DeviceProtocolDialectProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(ACE4000DeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.javaName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<ACE4000DeviceProtocolDialectProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(ACE4000DeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> this.fieldDoesNotExists(field.javaName()))
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        return this.fieldDoesNotExists(ACE4000DeviceProtocolDialectProperties.class, fieldName);
    }

    @Test
    public void checkJavaxAnnotationsOnFields() {
        this.checkJavaxAnnotationsOnFields(ACE4000DeviceProtocolDialectProperties.class);
    }

    @Test
    public void propertySpecNameIsNotNull() {
        List<ACE4000DeviceProtocolDialectProperties.ActualFields> fieldsWithNullPropertySpecName =
            Stream
                .of(ACE4000DeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.propertySpecName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void propertySpecNameIsNotEmpty() {
        List<ACE4000DeviceProtocolDialectProperties.ActualFields> fieldsWithNullPropertySpecName =
            Stream
                .of(ACE4000DeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.propertySpecName().toString().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void databaseNameIsNotNull() {
        List<ACE4000DeviceProtocolDialectProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(ACE4000DeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.databaseName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<ACE4000DeviceProtocolDialectProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(ACE4000DeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.databaseName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<ACE4000DeviceProtocolDialectProperties.ActualFields> fieldsWithTooLongDatabaseName =
            Stream
                .of(ACE4000DeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

}