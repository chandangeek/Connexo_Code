/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.sdksample;

import com.energyict.CustomPropertiesPersistenceTest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link SDKTopologyTaskDialectProperties} component.
 */
public class SDKTopologyTaskDialectPropertiesTest extends CustomPropertiesPersistenceTest {

    @Test
    public void javaNameIsNotNull() {
        List<SDKTopologyTaskDialectProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(SDKTopologyTaskDialectProperties.ActualFields.values())
                .filter(field -> field.javaName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<SDKTopologyTaskDialectProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(SDKTopologyTaskDialectProperties.ActualFields.values())
                .filter(field -> field.javaName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<SDKTopologyTaskDialectProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(SDKTopologyTaskDialectProperties.ActualFields.values())
                .filter(field -> this.fieldDoesNotExists(field.javaName()))
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        return this.fieldDoesNotExists(SDKTopologyTaskDialectProperties.class, fieldName);
    }

    @Test
    public void checkJavaxAnnotationsOnFields() {
        this.checkJavaxAnnotationsOnFields(SDKTopologyTaskDialectProperties.class);
    }

    @Test
    public void propertySpecNameIsNotNull() {
        List<SDKTopologyTaskDialectProperties.ActualFields> fieldsWithNullPropertySpecName =
            Stream
                .of(SDKTopologyTaskDialectProperties.ActualFields.values())
                .filter(field -> field.propertySpecName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void propertySpecNameIsNotEmpty() {
        List<SDKTopologyTaskDialectProperties.ActualFields> fieldsWithNullPropertySpecName =
            Stream
                .of(SDKTopologyTaskDialectProperties.ActualFields.values())
                .filter(field -> field.propertySpecName().toString().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void databaseNameIsNotNull() {
        List<SDKTopologyTaskDialectProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(SDKTopologyTaskDialectProperties.ActualFields.values())
                .filter(field -> field.databaseName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<SDKTopologyTaskDialectProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(SDKTopologyTaskDialectProperties.ActualFields.values())
                .filter(field -> field.databaseName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<SDKTopologyTaskDialectProperties.ActualFields> fieldsWithTooLongDatabaseName =
            Stream
                .of(SDKTopologyTaskDialectProperties.ActualFields.values())
                .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

}