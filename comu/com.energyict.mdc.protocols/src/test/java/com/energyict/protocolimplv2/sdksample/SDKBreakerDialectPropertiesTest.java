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
 * Tests the {@link SDKBreakerDialectProperties} component.
 *
 * @author sva
 * @since 8/04/2016 - 15:09
 */
public class SDKBreakerDialectPropertiesTest extends CustomPropertiesPersistenceTest {

    @Test
    public void javaNameIsNotNull() {
        List<SDKBreakerDialectProperties.ActualFields> fieldsWithNullJavaName =
                Stream
                        .of(SDKBreakerDialectProperties.ActualFields.values())
                        .filter(field -> field.javaName() == null)
                        .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<SDKBreakerDialectProperties.ActualFields> fieldsWithNullJavaName =
                Stream
                        .of(SDKBreakerDialectProperties.ActualFields.values())
                        .filter(field -> field.javaName().isEmpty())
                        .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<SDKBreakerDialectProperties.ActualFields> fieldsWithNullJavaName =
                Stream
                        .of(SDKBreakerDialectProperties.ActualFields.values())
                        .filter(field -> this.fieldDoesNotExists(field.javaName()))
                        .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        return this.fieldDoesNotExists(SDKBreakerDialectProperties.class, fieldName);
    }

    @Test
    public void checkJavaxAnnotationsOnFields() {
        this.checkJavaxAnnotationsOnFields(SDKBreakerDialectProperties.class);
    }

    @Test
    public void propertySpecNameIsNotNull() {
        List<SDKBreakerDialectProperties.ActualFields> fieldsWithNullPropertySpecName =
                Stream
                        .of(SDKBreakerDialectProperties.ActualFields.values())
                        .filter(field -> field.propertySpecName() == null)
                        .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void propertySpecNameIsNotEmpty() {
        List<SDKBreakerDialectProperties.ActualFields> fieldsWithNullPropertySpecName =
                Stream
                        .of(SDKBreakerDialectProperties.ActualFields.values())
                        .filter(field -> field.propertySpecName().toString().isEmpty())
                        .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void databaseNameIsNotNull() {
        List<SDKBreakerDialectProperties.ActualFields> fieldsWithNullDatabaseName =
                Stream
                        .of(SDKBreakerDialectProperties.ActualFields.values())
                        .filter(field -> field.databaseName() == null)
                        .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<SDKBreakerDialectProperties.ActualFields> fieldsWithNullDatabaseName =
                Stream
                        .of(SDKBreakerDialectProperties.ActualFields.values())
                        .filter(field -> field.databaseName().isEmpty())
                        .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<SDKBreakerDialectProperties.ActualFields> fieldsWithTooLongDatabaseName =
                Stream
                        .of(SDKBreakerDialectProperties.ActualFields.values())
                        .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                        .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }
}