/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.serial.optical.dlms;

import com.energyict.CustomPropertiesPersistenceTest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link LegacyOpticalDlmsConnectionProperties} component.
 */
public class LegacyOpticalDlmsConnectionPropertiesTest extends CustomPropertiesPersistenceTest {

    @Test
    public void javaNameIsNotNull() {
        List<LegacyOpticalDlmsConnectionProperties.Field> fieldsWithNullJavaName =
            Stream
                .of(LegacyOpticalDlmsConnectionProperties.Field.values())
                .filter(field -> field.javaName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<LegacyOpticalDlmsConnectionProperties.Field> fieldsWithNullJavaName =
            Stream
                .of(LegacyOpticalDlmsConnectionProperties.Field.values())
                .filter(field -> field.javaName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<LegacyOpticalDlmsConnectionProperties.Field> fieldsWithNullJavaName =
            Stream
                .of(LegacyOpticalDlmsConnectionProperties.Field.values())
                .filter(field -> this.fieldDoesNotExists(field.javaName()))
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        return this.fieldDoesNotExists(LegacyOpticalDlmsConnectionProperties.class, fieldName);
    }

    @Test
    public void checkJavaxAnnotationsOnFields() {
        this.checkJavaxAnnotationsOnFields(LegacyOpticalDlmsConnectionProperties.class);
    }

    @Test
    public void databaseNameIsNotNull() {
        List<LegacyOpticalDlmsConnectionProperties.Field> fieldsWithNullDatabaseName =
            Stream
                .of(LegacyOpticalDlmsConnectionProperties.Field.values())
                .filter(field -> field.databaseName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<LegacyOpticalDlmsConnectionProperties.Field> fieldsWithNullDatabaseName =
            Stream
                .of(LegacyOpticalDlmsConnectionProperties.Field.values())
                .filter(field -> field.databaseName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<LegacyOpticalDlmsConnectionProperties.Field> fieldsWithTooLongDatabaseName =
            Stream
                .of(LegacyOpticalDlmsConnectionProperties.Field.values())
                .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

}