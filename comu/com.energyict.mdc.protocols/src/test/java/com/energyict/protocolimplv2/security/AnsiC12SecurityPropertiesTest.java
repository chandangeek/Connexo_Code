/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.security;

import com.energyict.CustomPropertiesPersistenceTest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link AnsiC12SecurityProperties} component.
 */
public class AnsiC12SecurityPropertiesTest extends CustomPropertiesPersistenceTest {

    @Test
    public void javaNameIsNotNull() {
        List<AnsiC12SecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(AnsiC12SecurityProperties.ActualFields.values())
                .filter(field -> field.javaName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<AnsiC12SecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(AnsiC12SecurityProperties.ActualFields.values())
                .filter(field -> field.javaName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<AnsiC12SecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(AnsiC12SecurityProperties.ActualFields.values())
                .filter(field -> this.fieldDoesNotExists(field.javaName()))
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        return this.fieldDoesNotExists(AnsiC12SecurityProperties.class, fieldName);
    }

    @Test
    public void checkJavaxAnnotationsOnFields() {
        this.checkJavaxAnnotationsOnFields(AnsiC12SecurityProperties.class);
    }

    @Test
    public void databaseNameIsNotNull() {
        List<AnsiC12SecurityProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(AnsiC12SecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<AnsiC12SecurityProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(AnsiC12SecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<AnsiC12SecurityProperties.ActualFields> fieldsWithTooLongDatabaseName =
            Stream
                .of(AnsiC12SecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

}