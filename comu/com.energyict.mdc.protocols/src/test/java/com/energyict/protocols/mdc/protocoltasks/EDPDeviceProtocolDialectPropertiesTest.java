package com.energyict.protocols.mdc.protocoltasks;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link EDPDeviceProtocolDialectProperties} component.
 */
public class EDPDeviceProtocolDialectPropertiesTest {

    public static final int MAX_COLUMN_NAME_LENGTH = 30;

    @Test
    public void javaNameIsNotNull() {
        List<EDPDeviceProtocolDialectProperties.EDPFields> fieldsWithNullJavaName =
            Stream
                .of(EDPDeviceProtocolDialectProperties.EDPFields.values())
                .filter(field -> field.javaName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<EDPDeviceProtocolDialectProperties.EDPFields> fieldsWithNullJavaName =
            Stream
                .of(EDPDeviceProtocolDialectProperties.EDPFields.values())
                .filter(field -> field.javaName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<EDPDeviceProtocolDialectProperties.EDPFields> fieldsWithNullJavaName =
            Stream
                .of(EDPDeviceProtocolDialectProperties.EDPFields.values())
                .filter(field -> this.fieldDoesNotExists(field.javaName()))
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        try {
            return EDPDeviceProtocolDialectProperties.class.getField(fieldName) == null;
        }
        catch (NoSuchFieldException e) {
            return false;
        }
    }

    @Test
    public void propertySpecNameIsNotNull() {
        List<EDPDeviceProtocolDialectProperties.EDPFields> fieldsWithNullPropertySpecName =
            Stream
                .of(EDPDeviceProtocolDialectProperties.EDPFields.values())
                .filter(field -> field.propertySpecName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void propertySpecNameIsNotEmpty() {
        List<EDPDeviceProtocolDialectProperties.EDPFields> fieldsWithNullPropertySpecName =
            Stream
                .of(EDPDeviceProtocolDialectProperties.EDPFields.values())
                .filter(field -> field.propertySpecName().toString().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void databaseNameIsNotNull() {
        List<EDPDeviceProtocolDialectProperties.EDPFields> fieldsWithNullDatabaseName =
            Stream
                .of(EDPDeviceProtocolDialectProperties.EDPFields.values())
                .filter(field -> field.databaseName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<EDPDeviceProtocolDialectProperties.EDPFields> fieldsWithNullDatabaseName =
            Stream
                .of(EDPDeviceProtocolDialectProperties.EDPFields.values())
                .filter(field -> field.databaseName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<EDPDeviceProtocolDialectProperties.EDPFields> fieldsWithTooLongDatabaseName =
            Stream
                .of(EDPDeviceProtocolDialectProperties.EDPFields.values())
                .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

}