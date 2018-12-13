/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.protocoltasks;

import com.energyict.CustomPropertiesPersistenceTest;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link TcpDeviceProtocolDialectProperties} component.
 */
public class TcpDeviceProtocolDialectPropertiesTest extends CustomPropertiesPersistenceTest {

    @Test
    public void javaNameIsNotNull() {
        List<TcpDeviceProtocolDialectProperties.ActualFields> fieldsWithNullJavaName =
                Stream
                        .of(TcpDeviceProtocolDialectProperties.ActualFields.values())
                        .filter(field -> field.javaName() == null)
                        .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<TcpDeviceProtocolDialectProperties.ActualFields> fieldsWithNullJavaName =
                Stream
                        .of(TcpDeviceProtocolDialectProperties.ActualFields.values())
                        .filter(field -> field.javaName().isEmpty())
                        .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<TcpDeviceProtocolDialectProperties.ActualFields> fieldsWithNullJavaName =
                Stream
                        .of(TcpDeviceProtocolDialectProperties.ActualFields.values())
                        .filter(field -> this.fieldDoesNotExists(field.javaName()))
                        .collect(Collectors.toList());

        // One field should remain: DELAY_AFTER_ERROR. It is a legacy column (up to 10.3) which no longer has a java field
        assertThat(fieldsWithNullJavaName.size() == 1);
        assertThat(fieldsWithNullJavaName.contains(TcpDeviceProtocolDialectProperties.ActualFields.DELAY_AFTER_ERROR));
    }

    private boolean fieldDoesNotExists(String fieldName) {
        return this.fieldDoesNotExists(TcpDeviceProtocolDialectProperties.class, fieldName);
    }

    @Test
    public void checkJavaxAnnotationsOnFields() {
        this.checkJavaxAnnotationsOnFields(TcpDeviceProtocolDialectProperties.class);
    }

    @Test
    public void propertySpecNameIsNotNull() {
        List<TcpDeviceProtocolDialectProperties.ActualFields> fieldsWithNullPropertySpecName =
                Stream
                        .of(TcpDeviceProtocolDialectProperties.ActualFields.values())
                        .filter(field -> field.propertySpecName() == null)
                        .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void propertySpecNameIsNotEmpty() {
        List<TcpDeviceProtocolDialectProperties.ActualFields> fieldsWithNullPropertySpecName =
                Stream
                        .of(TcpDeviceProtocolDialectProperties.ActualFields.values())
                        .filter(field -> field.propertySpecName().toString().isEmpty())
                        .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void databaseNameIsNotNull() {
        List<TcpDeviceProtocolDialectProperties.ActualFields> fieldsWithNullDatabaseName =
                Stream
                        .of(TcpDeviceProtocolDialectProperties.ActualFields.values())
                        .filter(field -> field.databaseName() == null)
                        .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<TcpDeviceProtocolDialectProperties.ActualFields> fieldsWithNullDatabaseName =
                Stream
                        .of(TcpDeviceProtocolDialectProperties.ActualFields.values())
                        .filter(field -> field.databaseName().isEmpty())
                        .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<TcpDeviceProtocolDialectProperties.ActualFields> fieldsWithTooLongDatabaseName =
                Stream
                        .of(TcpDeviceProtocolDialectProperties.ActualFields.values())
                        .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                        .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

}