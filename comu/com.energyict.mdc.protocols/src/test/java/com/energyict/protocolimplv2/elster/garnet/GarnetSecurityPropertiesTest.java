package com.energyict.protocolimplv2.elster.garnet;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link GarnetSecurityProperties} component.
 */
public class GarnetSecurityPropertiesTest {

    public static final int MAX_COLUMN_NAME_LENGTH = 30;

    @Test
    public void javaNameIsNotNull() {
        List<GarnetSecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(GarnetSecurityProperties.ActualFields.values())
                .filter(field -> field.javaName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<GarnetSecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(GarnetSecurityProperties.ActualFields.values())
                .filter(field -> field.javaName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<GarnetSecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(GarnetSecurityProperties.ActualFields.values())
                .filter(field -> this.fieldDoesNotExists(field.javaName()))
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        try {
            return GarnetSecurityProperties.class.getField(fieldName) == null;
        }
        catch (NoSuchFieldException e) {
            return false;
        }
    }

    @Test
    public void databaseNameIsNotNull() {
        List<GarnetSecurityProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(GarnetSecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<GarnetSecurityProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(GarnetSecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<GarnetSecurityProperties.ActualFields> fieldsWithTooLongDatabaseName =
            Stream
                .of(GarnetSecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

}