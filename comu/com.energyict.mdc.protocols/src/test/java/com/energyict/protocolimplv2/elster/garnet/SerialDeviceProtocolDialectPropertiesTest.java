package com.energyict.protocolimplv2.elster.garnet;

import com.energyict.CustomPropertiesPersistenceTest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.energyict.protocols.mdc.protocoltasks.SerialDeviceProtocolDialectProperties;
import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link SerialDeviceProtocolDialectProperties} component.
 */
public class SerialDeviceProtocolDialectPropertiesTest extends CustomPropertiesPersistenceTest {

    @Test
    public void javaNameIsNotNull() {
        List<SerialDeviceProtocolDialectProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(SerialDeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.javaName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<SerialDeviceProtocolDialectProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(SerialDeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.javaName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<SerialDeviceProtocolDialectProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(SerialDeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> this.fieldDoesNotExists(field.javaName()))
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        return this.fieldDoesNotExists(SerialDeviceProtocolDialectProperties.class, fieldName);
    }

    @Test
    public void checkJavaxAnnotationsOnFields() {
        this.checkJavaxAnnotationsOnFields(SerialDeviceProtocolDialectProperties.class);
    }

    @Test
    public void propertySpecNameIsNotNull() {
        List<SerialDeviceProtocolDialectProperties.ActualFields> fieldsWithNullPropertySpecName =
            Stream
                .of(SerialDeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.propertySpecName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void propertySpecNameIsNotEmpty() {
        List<SerialDeviceProtocolDialectProperties.ActualFields> fieldsWithNullPropertySpecName =
            Stream
                .of(SerialDeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.propertySpecName().toString().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void databaseNameIsNotNull() {
        List<SerialDeviceProtocolDialectProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(SerialDeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.databaseName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<SerialDeviceProtocolDialectProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(SerialDeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.databaseName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<SerialDeviceProtocolDialectProperties.ActualFields> fieldsWithTooLongDatabaseName =
            Stream
                .of(SerialDeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

}