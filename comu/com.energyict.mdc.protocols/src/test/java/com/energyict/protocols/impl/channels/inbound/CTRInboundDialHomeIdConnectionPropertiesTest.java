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
 * Tests the {@link CTRInboundDialHomeIdConnectionProperties} component.
 */
public class CTRInboundDialHomeIdConnectionPropertiesTest extends CustomPropertiesPersistenceTest {

    @Test
    public void javaNameIsNotNull() {
        List<CTRInboundDialHomeIdConnectionProperties.Fields> fieldsWithNullJavaName =
            getFields()
                .filter(field -> field.javaName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<CTRInboundDialHomeIdConnectionProperties.Fields> fieldsWithNullJavaName =
            getFields()
                .filter(field -> field.javaName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<CTRInboundDialHomeIdConnectionProperties.Fields> fieldsWithNullJavaName =
            getFields()
                .filter(field -> this.fieldDoesNotExists(field.javaName()))
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        return this.fieldDoesNotExists(CTRInboundDialHomeIdConnectionProperties.class, fieldName);
    }

    @Test
    public void checkJavaxAnnotationsOnFields() {
        this.checkJavaxAnnotationsOnFields(CTRInboundDialHomeIdConnectionProperties.class);
    }

    @Test
    public void propertySpecNameIsNotNull() {
        List<CTRInboundDialHomeIdConnectionProperties.Fields> fieldsWithNullPropertySpecName =
            getFields()
                .filter(field -> field.propertySpecName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void propertySpecNameIsNotEmpty() {
        List<CTRInboundDialHomeIdConnectionProperties.Fields> fieldsWithNullPropertySpecName =
            getFields()
                .filter(field -> field.propertySpecName().toString().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void databaseNameIsNotNull() {
        List<CTRInboundDialHomeIdConnectionProperties.Fields> fieldsWithNullDatabaseName =
            getFields()
                .filter(field -> field.databaseName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<CTRInboundDialHomeIdConnectionProperties.Fields> fieldsWithNullDatabaseName =
            getFields()
                .filter(field -> field.databaseName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<CTRInboundDialHomeIdConnectionProperties.Fields> fieldsWithTooLongDatabaseName =
            getFields()
                .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

    private Stream<CTRInboundDialHomeIdConnectionProperties.Fields> getFields() {
        Set<CTRInboundDialHomeIdConnectionProperties.Fields> fields = EnumSet.allOf(CTRInboundDialHomeIdConnectionProperties.Fields.class);
        fields.remove(CTRInboundDialHomeIdConnectionProperties.Fields.CONNECTION_PROVIDER);
        return fields.stream();
    }

}