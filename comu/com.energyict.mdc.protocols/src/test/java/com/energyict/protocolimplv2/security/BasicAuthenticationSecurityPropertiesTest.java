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
 * Tests the {@link BasicAuthenticationSecurityProperties} component.
 */
public class BasicAuthenticationSecurityPropertiesTest extends CustomPropertiesPersistenceTest {

    @Test
    public void javaNameIsNotNull() {
        List<BasicAuthenticationSecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(BasicAuthenticationSecurityProperties.ActualFields.values())
                .filter(field -> field.javaName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<BasicAuthenticationSecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(BasicAuthenticationSecurityProperties.ActualFields.values())
                .filter(field -> field.javaName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<BasicAuthenticationSecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(BasicAuthenticationSecurityProperties.ActualFields.values())
                .filter(field -> this.fieldDoesNotExists(field.javaName()))
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        return this.fieldDoesNotExists(BasicAuthenticationSecurityProperties.class, fieldName);
    }

    @Test
    public void checkJavaxAnnotationsOnFields() {
        this.checkJavaxAnnotationsOnFields(BasicAuthenticationSecurityProperties.class);
    }

    @Test
    public void databaseNameIsNotNull() {
        List<BasicAuthenticationSecurityProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(BasicAuthenticationSecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<BasicAuthenticationSecurityProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(BasicAuthenticationSecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<BasicAuthenticationSecurityProperties.ActualFields> fieldsWithTooLongDatabaseName =
            Stream
                .of(BasicAuthenticationSecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

}