package com.energyict.protocolimplv2.securitysupport;

import com.energyict.CustomPropertiesPersistenceTest;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link IEC1107SecurityProperties} component.
 */
public class IEC1107SecurityPropertiesTest extends CustomPropertiesPersistenceTest {

    @Test
    public void javaNameIsNotNull() {
        List<IEC1107SecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(IEC1107SecurityProperties.ActualFields.values())
                .filter(field -> field.javaName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void javaNameIsNotEmpty() {
        List<IEC1107SecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(IEC1107SecurityProperties.ActualFields.values())
                .filter(field -> field.javaName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    @Test
    public void fieldsExist() {
        List<IEC1107SecurityProperties.ActualFields> fieldsWithNullJavaName =
            Stream
                .of(IEC1107SecurityProperties.ActualFields.values())
                .filter(field -> this.fieldDoesNotExists(field.javaName()))
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullJavaName).isEmpty();
    }

    private boolean fieldDoesNotExists(String fieldName) {
        return this.fieldDoesNotExists(IEC1107SecurityProperties.class, fieldName);
    }

    @Test
    public void checkJavaxAnnotationsOnFields() {
        this.checkJavaxAnnotationsOnFields(IEC1107SecurityProperties.class);
    }

    @Test
    public void databaseNameIsNotNull() {
        List<IEC1107SecurityProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(IEC1107SecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName() == null)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void databaseNameIsNotEmpty() {
        List<IEC1107SecurityProperties.ActualFields> fieldsWithNullDatabaseName =
            Stream
                .of(IEC1107SecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName().isEmpty())
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithNullDatabaseName).isEmpty();
    }

    @Test
    public void maximumLengthOfColumnNames() {
        List<IEC1107SecurityProperties.ActualFields> fieldsWithTooLongDatabaseName =
            Stream
                .of(IEC1107SecurityProperties.ActualFields.values())
                .filter(field -> field.databaseName().length() > MAX_COLUMN_NAME_LENGTH)
                .collect(Collectors.toList());

        // Asserts
        assertThat(fieldsWithTooLongDatabaseName).isEmpty();
    }

}