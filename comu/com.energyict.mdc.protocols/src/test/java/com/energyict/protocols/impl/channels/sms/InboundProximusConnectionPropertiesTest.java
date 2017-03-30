/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.sms;

import com.energyict.CustomPropertiesPersistenceTest;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link InboundProximusConnectionProperties} component.
 */
public class InboundProximusConnectionPropertiesTest extends CustomPropertiesPersistenceTest {

    @Test
    public void javaNameIsNotNull() {
        List<InboundProximusConnectionProperties.Fields> fieldsWithNullJavaName =
            getFields()
                .filter(field -> field.javaName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<InboundProximusConnectionProperties.Fields> fieldsWithNullJavaName =
            getFields()
                .filter(field -> field.javaName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<InboundProximusConnectionProperties.Fields> fieldsWithNullJavaName =
            getFields()
                .filter(field -> this.fieldDoesNotExists(field.javaName()))
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        return this.fieldDoesNotExists(InboundProximusConnectionProperties.class, fieldName);
    }

    @Test
    public void checkJavaxAnnotationsOnFields() {
        this.checkJavaxAnnotationsOnFields(InboundProximusConnectionProperties.class);
    }

    @Test
    public void propertySpecNameIsNotNull() {
        List<InboundProximusConnectionProperties.Fields> fieldsWithNullPropertySpecName =
            getFields()
                .filter(field -> field.propertySpecName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void propertySpecNameIsNotEmpty() {
        List<InboundProximusConnectionProperties.Fields> fieldsWithNullPropertySpecName =
            getFields()
                .filter(field -> field.propertySpecName().toString().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void databaseNameIsNotNull() {
        List<InboundProximusConnectionProperties.Fields> fieldsWithNullDatabaseName =
            getFields()
                .filter(field -> field.databaseName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<InboundProximusConnectionProperties.Fields> fieldsWithNullDatabaseName =
            getFields()
                .filter(field -> field.databaseName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<InboundProximusConnectionProperties.Fields> fieldsWithTooLongDatabaseName =
            getFields()
                .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

    private Stream<InboundProximusConnectionProperties.Fields> getFields() {
        Set<InboundProximusConnectionProperties.Fields> fields = EnumSet.allOf(InboundProximusConnectionProperties.Fields.class);
        fields.remove(InboundProximusConnectionProperties.Fields.CONNECTION_PROVIDER);
        return fields.stream();
    }

}