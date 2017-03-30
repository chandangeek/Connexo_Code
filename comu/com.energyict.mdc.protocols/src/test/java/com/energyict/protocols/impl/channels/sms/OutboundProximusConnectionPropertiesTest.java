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
 * Tests the {@link OutboundProximusConnectionProperties} component.
 */
public class OutboundProximusConnectionPropertiesTest extends CustomPropertiesPersistenceTest {

    @Test
    public void javaNameIsNotNull() {
        List<OutboundProximusConnectionProperties.Fields> fieldsWithNullJavaName =
            getFields()
                .filter(field -> field.javaName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<OutboundProximusConnectionProperties.Fields> fieldsWithNullJavaName =
            getFields()
                .filter(field -> field.javaName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<OutboundProximusConnectionProperties.Fields> fieldsWithNullJavaName =
            getFields()
                .filter(field -> this.fieldDoesNotExists(field.javaName()))
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        return this.fieldDoesNotExists(OutboundProximusConnectionProperties.class, fieldName);
    }

    @Test
    public void checkJavaxAnnotationsOnFields() {
        this.checkJavaxAnnotationsOnFields(OutboundProximusConnectionProperties.class);
    }

    @Test
    public void propertySpecNameIsNotNull() {
        List<OutboundProximusConnectionProperties.Fields> fieldsWithNullPropertySpecName =
            getFields()
                .filter(field -> field.propertySpecName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void propertySpecNameIsNotEmpty() {
        List<OutboundProximusConnectionProperties.Fields> fieldsWithNullPropertySpecName =
            getFields()
                .filter(field -> field.propertySpecName().toString().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullPropertySpecName).isEmpty();
    }

    @Test
    public void databaseNameIsNotNull() {
        List<OutboundProximusConnectionProperties.Fields> fieldsWithNullDatabaseName =
            getFields()
                .filter(field -> field.databaseName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<OutboundProximusConnectionProperties.Fields> fieldsWithNullDatabaseName =
            getFields()
                .filter(field -> field.databaseName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<OutboundProximusConnectionProperties.Fields> fieldsWithTooLongDatabaseName =
            getFields()
                .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

    private Stream<OutboundProximusConnectionProperties.Fields> getFields() {
        Set<OutboundProximusConnectionProperties.Fields> fields = EnumSet.allOf(OutboundProximusConnectionProperties.Fields.class);
        fields.remove(OutboundProximusConnectionProperties.Fields.CONNECTION_PROVIDER);
        return fields.stream();
    }

}