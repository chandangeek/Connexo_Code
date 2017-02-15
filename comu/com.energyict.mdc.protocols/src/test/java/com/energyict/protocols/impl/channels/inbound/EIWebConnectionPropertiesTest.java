/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.inbound;

import com.energyict.CustomPropertiesPersistenceTest;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link EIWebConnectionProperties} component.
 */
public class EIWebConnectionPropertiesTest extends CustomPropertiesPersistenceTest {

    @Test
    public void javaNameIsNotNull() {
        List<EIWebConnectionProperties.Fields> fieldsWithNullJavaName =
            getFields()
                .filter(field -> field.javaName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<EIWebConnectionProperties.Fields> fieldsWithNullJavaName =
            getFields()
                .filter(field -> field.javaName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<EIWebConnectionProperties.Fields> fieldsWithNullJavaName =
            getFields()
                .filter(field -> this.fieldDoesNotExists(field.javaName()))
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        return this.fieldDoesNotExists(EIWebConnectionProperties.class, fieldName);
    }

    @Test
    public void checkJavaxAnnotationsOnFields() {
        this.checkJavaxAnnotationsOnFields(EIWebConnectionProperties.class);
    }

    @Test
    public void propertySpecNameIsNotNull() {
        List<EIWebConnectionProperties.Fields> fieldsWithNullPropertySpecName =
            getFields()
                .filter(field -> field.propertySpecName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void propertySpecNameIsNotEmpty() {
        List<EIWebConnectionProperties.Fields> fieldsWithNullPropertySpecName =
            getFields()
                .filter(field -> field.propertySpecName().toString().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void databaseNameIsNotNull() {
        List<EIWebConnectionProperties.Fields> fieldsWithNullDatabaseName =
            getFields()
                .filter(field -> field.databaseName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<EIWebConnectionProperties.Fields> fieldsWithNullDatabaseName =
            getFields()
                .filter(field -> field.databaseName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<EIWebConnectionProperties.Fields> fieldsWithTooLongDatabaseName =
            getFields()
                .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

    private Stream<EIWebConnectionProperties.Fields> getFields() {
        Set<EIWebConnectionProperties.Fields> fields = EnumSet.allOf(EIWebConnectionProperties.Fields.class);
        fields.remove(EIWebConnectionProperties.Fields.CONNECTION_PROVIDER);
        return fields.stream();
    }

}