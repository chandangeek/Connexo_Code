/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.impl;

import com.elster.jupiter.properties.ListValueFactory;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ListValueFactory} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-17 (16:51)
 */
@RunWith(MockitoJUnitRunner.class)
public class ListValueFactoryTest {

    @Test
    public void fromNullStringValue() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        List list = testInstance.fromStringValue(null);

        // Asserts
        assertThat(list).isEmpty();
    }

    @Test
    public void fromEmptyStringValue() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        List list = testInstance.fromStringValue("");

        // Asserts
        assertThat(list).isEmpty();
    }

    @Test
    public void fromWhiteSpaceStringValue() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        List list = testInstance.fromStringValue("     ");

        // Asserts
        assertThat(list).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void fromSingleStringValue() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        List list = testInstance.fromStringValue("hello");

        // Asserts
        assertThat(list).containsOnly("hello");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void fromStringValue() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        List list = testInstance.fromStringValue("hello" + PropertySpecBuilder.DEFAULT_MULTI_VALUE_SEPARATOR + "world");

        // Asserts
        assertThat(list).containsOnly("hello", "world");
    }

    @Test
    public void toStringValueForNullList() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        String value = testInstance.toStringValue(null);

        // Asserts
        assertThat(value).isNull();
    }

    @Test
    public void toStringValueForEmptyList() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        String value = testInstance.toStringValue(Collections.emptyList());

        // Asserts
        assertThat(value).isEmpty();
    }

    @Test
    public void toStringValueForSingletonList() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        String value = testInstance.toStringValue(Collections.singletonList("hello"));

        // Asserts
        assertThat(value).isEqualTo("hello");
    }

    @Test
    public void toStringValue() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        String value = testInstance.toStringValue(Arrays.asList("hello", "world"));

        // Asserts
        assertThat(value).isEqualTo("hello" + PropertySpecBuilder.DEFAULT_MULTI_VALUE_SEPARATOR + "world");
    }

    @Test
    public void valueTypeIsNotNull() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        Class<List> valueType = testInstance.getValueType();

        // Asserts
        assertThat(valueType).isNotNull();
    }

    @Test
    public void valueFromDatabaseForNullStringValue() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        List list = testInstance.valueFromDatabase(null);

        // Asserts
        assertThat(list).isEmpty();
    }

    @Test
    public void valueFromDatabaseForEmptyStringValue() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        List list = testInstance.valueFromDatabase("");

        // Asserts
        assertThat(list).isEmpty();
    }

    @Test
    public void valueFromDatabaseForWhiteSpaceStringValue() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        List list = testInstance.valueFromDatabase("     ");

        // Asserts
        assertThat(list).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void valueFromDatabaseForSingleStringValue() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        List list = testInstance.valueFromDatabase("hello");

        // Asserts
        assertThat(list).containsOnly("hello");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void valueFromDatabaseForStringValue() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        List list = testInstance.valueFromDatabase("hello" + PropertySpecBuilder.DEFAULT_MULTI_VALUE_SEPARATOR + "world");

        // Asserts
        assertThat(list).containsOnly("hello", "world");
    }

    @Test
    public void valueToDatabaseForNullList() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        Object value = testInstance.valueToDatabase(null);

        // Asserts
        assertThat(value).isNull();
    }

    @Test
    public void valueToDatabaseForEmptyList() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        Object value = testInstance.valueToDatabase(Collections.emptyList());

        // Asserts
        assertThat(value).isInstanceOf(String.class);
        assertThat((String) value).isEmpty();
    }

    @Test
    public void valueToDatabaseForSingletonList() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        Object value = testInstance.valueToDatabase(Collections.singletonList("hello"));

        // Asserts
        assertThat(value).isInstanceOf(String.class);
        assertThat((String) value).isEqualTo("hello");
    }

    @Test
    public void valueToDatabaseForStringValue() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        Object value = testInstance.valueToDatabase(Arrays.asList("hello", "world"));

        // Asserts
        assertThat(value).isInstanceOf(String.class);
        assertThat((String) value).isEqualTo("hello" + PropertySpecBuilder.DEFAULT_MULTI_VALUE_SEPARATOR + "world");
    }

    @Test
    public void valueToDatabaseForStringValueWithCustomSeparator() {
        ListValueFactory<String> testInstance = new ListValueFactory<>(new StringFactory(), "##");

        // Business method
        Object value = testInstance.valueToDatabase(Arrays.asList("hello", "world"));

        // Asserts
        assertThat(value).isInstanceOf(String.class);
        assertThat((String) value).isEqualTo("hello##world");
    }

    @Test
    public void bindNullListToPreparedStatement() throws SQLException {
        ListValueFactory<String> testInstance = this.getTestInstance();
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        // Business method
        testInstance.bind(preparedStatement, 1, null);

        // Asserts
        verify(preparedStatement).setNull(1, Types.VARCHAR);
    }

    @Test
    public void bindEmptyListToPreparedStatement() throws SQLException {
        ListValueFactory<String> testInstance = this.getTestInstance();
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        // Business method
        testInstance.bind(preparedStatement, 1, Collections.emptyList());

        // Asserts
        verify(preparedStatement).setNull(1, Types.VARCHAR);
    }

    @Test
    public void bindToPreparedStatement() throws SQLException {
        ListValueFactory<String> testInstance = this.getTestInstance();
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        // Business method
        testInstance.bind(preparedStatement, 1, Arrays.asList("hello", "world"));

        // Asserts
        verify(preparedStatement).setObject(1, "hello" + PropertySpecBuilder.DEFAULT_MULTI_VALUE_SEPARATOR + "world");
    }

    @Test
    public void nullListIsNull() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        boolean isNull = testInstance.isNull(null);

        // Asserts
        assertThat(isNull).isTrue();
    }

    @Test
    public void emptyListIsNull() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        boolean isNull = testInstance.isNull(Collections.emptyList());

        // Asserts
        assertThat(isNull).isTrue();
    }

    @Test
    public void nonEmptyListIsNotNull() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        boolean isNull = testInstance.isNull(Arrays.asList("hello", "world"));

        // Asserts
        assertThat(isNull).isFalse();
    }

    @Test
    public void nullListIsValid() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        boolean isValid = testInstance.isValid(null);

        // Asserts
        assertThat(isValid).isTrue();
    }

    @Test
    public void emptyListIsValid() {
        ListValueFactory<String> testInstance = this.getTestInstance();

        // Business method
        boolean isValid = testInstance.isValid(Collections.emptyList());

        // Asserts
        assertThat(isValid).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void isValidDelegatesToActualValueFactory() {
        ValueFactory actualValueFactory = mock(ValueFactory.class);
        when(actualValueFactory.isValid(any())).thenReturn(true);
        ListValueFactory<String> testInstance = new ListValueFactory<>(actualValueFactory);

        // Business method
        testInstance.isValid(Arrays.asList("hello", "world"));

        // Asserts
        verify(actualValueFactory).isValid("hello");
        verify(actualValueFactory).isValid("world");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void isNotValidWhenAtLeastOneListElementIsNotValid() {
        ValueFactory actualValueFactory = mock(ValueFactory.class);
        when(actualValueFactory.isValid("hello")).thenReturn(true);
        when(actualValueFactory.isValid("world")).thenReturn(false);
        ListValueFactory<String> testInstance = new ListValueFactory<>(actualValueFactory);

        // Business method
        boolean isValid = testInstance.isValid(Arrays.asList("hello", "world"));

        // Asserts
        assertThat(isValid).isFalse();
    }

    private ListValueFactory<String> getTestInstance() {
        return new ListValueFactory<>(new StringFactory());
    }

}