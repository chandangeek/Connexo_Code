/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.dialects;

import com.energyict.CustomPropertiesPersistenceTest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link AbntDeviceProtocolDialectProperties} component.
 */
public class AbntDeviceProtocolDialectPropertiesTest extends CustomPropertiesPersistenceTest {

    @Test
    public void javaNameIsNotNull() {
        List<AbntDeviceProtocolDialectProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(AbntDeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.javaName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<AbntDeviceProtocolDialectProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(AbntDeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.javaName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<AbntDeviceProtocolDialectProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(AbntDeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> this.fieldDoesNotExists(field.javaName()))
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        return this.fieldDoesNotExists(AbntDeviceProtocolDialectProperties.class, fieldName);
    }

    @Test
    public void checkJavaxAnnotationsOnFields() {
        this.checkJavaxAnnotationsOnFields(AbntDeviceProtocolDialectProperties.class);
    }

    @Test
    public void propertySpecNameIsNotNull() {
        List<AbntDeviceProtocolDialectProperties.ActualFields> fieldsWithNullPropertySpecName =
            Stream
                .of(AbntDeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.propertySpecName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void propertySpecNameIsNotEmpty() {
        List<AbntDeviceProtocolDialectProperties.ActualFields> fieldsWithNullPropertySpecName =
            Stream
                .of(AbntDeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.propertySpecName().toString().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void databaseNameIsNotNull() {
        List<AbntDeviceProtocolDialectProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(AbntDeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.databaseName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<AbntDeviceProtocolDialectProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(AbntDeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.databaseName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<AbntDeviceProtocolDialectProperties.ActualFields> fieldsWithTooLongDatabaseName =
            Stream
                .of(AbntDeviceProtocolDialectProperties.ActualFields.values())
                .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

}