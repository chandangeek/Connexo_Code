/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.properties.HasIdAndName;

import java.util.function.Function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class IdWithNameFactoryTest {
    private static <T> Function<T, String> toStringFunction() {
        return Object::toString;
    }

    private static <T, R> Function<T, R> throwingFunction() {
        return obj -> {
            throw new IllegalStateException("Throwing function");
        };
    }

    private String testInstance = "testInstance";

    @Test
    public void testCanConvertFromNullStringValue() {
        HasIdAndName result = new IdWithNameFactory<>(throwingFunction(), throwingFunction(), throwingFunction())
                .fromStringValue(null);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isNull();
    }

    @Test
    public void testCanConvertToStringValueNullSource() {
        assertThat(new IdWithNameFactory<>(throwingFunction(), throwingFunction(), throwingFunction())
                .toStringValue(null)).isNull();
    }

    @Test
    public void testCanConvertValueFromNullDataBaseObject() {
        HasIdAndName result = new IdWithNameFactory<>(throwingFunction(), throwingFunction(), throwingFunction())
                .valueFromDatabase(null);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isNull();
    }

    @Test
    public void testCanConvertValueToDatabaseFromNullObject() {
        assertThat(new IdWithNameFactory<>(throwingFunction(), throwingFunction(), throwingFunction())
                .valueToDatabase(null)).isNull();
    }

    @Test
    public void testCanConvertValueToDatabaseFromObjectWithNullId() {
        assertThat(new IdWithNameFactory<>(throwingFunction(), throwingFunction(), throwingFunction())
                .valueToDatabase(new IdWithNameFactory.IdWithNameImpl(null, null))).isNull();
    }

    @Test
    public void testCanWrapNullObjects() {
        HasIdAndName result = new IdWithNameFactory<>(throwingFunction(), throwingFunction(), throwingFunction())
                .wrap(null);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isNull();
    }

    @Test
    public void testCanUnwrapNullObjects() {
        Object result = new IdWithNameFactory<>(throwingFunction(), throwingFunction(), throwingFunction())
                .unwrap(null);
        assertThat(result).isNull();
    }

    @Test
    public void testCanUnwrapObjectsWithNullId() {
        Object result = new IdWithNameFactory<>(throwingFunction(), throwingFunction(), throwingFunction())
                .unwrap(new IdWithNameFactory.IdWithNameImpl(null, null));
        assertThat(result).isNull();
    }

    @Test
    public void testCanConvertFromStringValue() {
        HasIdAndName result = new IdWithNameFactory<>(toStringFunction(), toStringFunction(), toStringFunction())
                .fromStringValue(testInstance);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testInstance);
        assertThat(result.getName()).isEqualTo(testInstance);
    }

    @Test
    public void testCanConvertToStringValue() {
        assertThat(new IdWithNameFactory<>(toStringFunction(), toStringFunction(), toStringFunction())
                .toStringValue(new IdWithNameFactory.IdWithNameImpl(testInstance, null)))
                .isEqualTo(testInstance);
    }

    @Test
    public void testCanConvertValueFromDatabase() {
        HasIdAndName result = new IdWithNameFactory<>(toStringFunction(), toStringFunction(), toStringFunction())
                .valueFromDatabase(testInstance);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testInstance);
        assertThat(result.getName()).isEqualTo(testInstance);
    }

    @Test
    public void testCanConvertValueToDatabase() {
        assertThat(new IdWithNameFactory<>(toStringFunction(), toStringFunction(), toStringFunction())
                .valueToDatabase(new IdWithNameFactory.IdWithNameImpl(testInstance, null)))
                .isEqualTo(testInstance);
    }

    @Test
    public void testCanWrapObjects() {
        HasIdAndName result = new IdWithNameFactory<>(toStringFunction(), toStringFunction(), toStringFunction())
                .valueFromDatabase(testInstance);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testInstance);
        assertThat(result.getName()).isEqualTo(testInstance);
    }

    @Test
    public void testCanUnwrapObjects() {
        Object result = new IdWithNameFactory<>(toStringFunction(), toStringFunction(), toStringFunction())
                .unwrap(new IdWithNameFactory.IdWithNameImpl(testInstance, null));
        assertThat(result).isEqualTo(testInstance);
    }
}
