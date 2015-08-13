package com.energyict.mdc.common;

import org.junit.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * User: gde
 * Date: 1/06/12
 */
public class TypedPropertiesTest {

    private static final String PROP1_NAME = "prop1";
    private static final String PROP1_VALUE = "value1";
    private static final String PROP2_NAME = "prop2";
    private static final String PROP2_VALUE = "value2";
    private static final String NUMERIC_PROP1_NAME = "numeric_prop1";
    private static final BigDecimal NUMERIC_PROP1_VALUE = BigDecimal.TEN;
    private static final String NUMERIC_PROP2_NAME = "numeric_prop2";
    private static final BigDecimal NUMERIC_PROP2_VALUE = new BigDecimal(951);

    @Test
    public void testEmptyConstructor () {
        // Business method
        TypedProperties empty = TypedProperties.empty();

        // Asserts
        assertThat(empty.localSize()).isZero();
        assertThat(empty.size()).isZero();
        assertThat(empty.getInheritedProperties()).isNull();
        assertThat(empty.localPropertyNames()).isEmpty();
        assertThat(empty.propertyNames()).isEmpty();
    }

    @Test
    public void testInheritedConstructor () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(PROP1_NAME, PROP1_VALUE);
        parentProperties.setProperty(PROP2_NAME, PROP2_VALUE);

        // Business method
        TypedProperties locallyEmpty = TypedProperties.inheritingFrom(parentProperties);

        // Asserts
        assertThat(locallyEmpty.localSize()).isZero();
        assertThat(locallyEmpty.size()).isEqualTo(2);
        assertThat(locallyEmpty.getInheritedProperties()).isNotNull();
        assertThat(locallyEmpty.localPropertyNames()).isEmpty();
        assertThat(locallyEmpty.propertyNames()).hasSize(2);
    }

    @Test
    public void testMultipleInheritedConstructor () {
        TypedProperties level1Properties = TypedProperties.empty();
        level1Properties.setProperty(PROP1_NAME, PROP1_VALUE);
        TypedProperties level2Properties = TypedProperties.inheritingFrom(level1Properties);
        level1Properties.setProperty(PROP2_NAME, PROP2_VALUE);

        // Business method
        TypedProperties locallyEmpty = TypedProperties.inheritingFrom(level2Properties);

        // Asserts
        assertThat(locallyEmpty.localSize()).isZero();
        assertThat(locallyEmpty.size()).isEqualTo(2);
        assertThat(locallyEmpty.getInheritedProperties()).isNotNull();
        assertThat(locallyEmpty.localPropertyNames()).isEmpty();
        assertThat(locallyEmpty.propertyNames()).hasSize(2);
    }

    @Test
    public void testGetPropertyWithoutInheritance () {
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(PROP1_NAME, PROP1_VALUE);

        // Asserts
        assertThat(typedProperties.getProperty(PROP1_NAME)).isEqualTo(PROP1_VALUE);
        assertThat(typedProperties.getProperty(PROP2_NAME)).isNull();
    }

    @Test
    public void testGetPropertyWithoutInheritanceButDefaultValue () {
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(PROP1_NAME, PROP1_VALUE);

        // Asserts
        assertThat(typedProperties.getProperty(PROP1_NAME)).isEqualTo(PROP1_VALUE);
        String defaultValue = "default";
        assertThat(typedProperties.getProperty(PROP2_NAME, defaultValue)).isEqualTo(defaultValue);
    }

    @Test
    public void testGetPropertyWithOnlyInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(PROP1_NAME, PROP1_VALUE);
        TypedProperties locallyEmpty = TypedProperties.inheritingFrom(parentProperties);

        // Asserts
        assertThat(locallyEmpty.getProperty(PROP1_NAME)).isEqualTo(PROP1_VALUE);
        assertThat(locallyEmpty.getProperty(PROP2_NAME)).isNull();
    }

    @Test
    public void testGetPropertyWithOnlyInheritedValuesButDefaultValue () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(PROP1_NAME, PROP1_VALUE);
        TypedProperties locallyEmpty = TypedProperties.inheritingFrom(parentProperties);

        // Asserts
        assertThat(locallyEmpty.getProperty(PROP1_NAME)).isEqualTo(PROP1_VALUE);
        String defaultValue = "default";
        assertThat(locallyEmpty.getProperty(PROP2_NAME, defaultValue)).isEqualTo(defaultValue);
    }

    @Test
    public void testGetPropertyWithLocalValuesAndInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(PROP1_NAME, PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(PROP2_NAME, PROP2_VALUE);

        // Asserts
        assertThat(typedProperties.getProperty(PROP1_NAME)).isEqualTo(PROP1_VALUE);
        assertThat(typedProperties.getProperty(PROP2_NAME)).isEqualTo(PROP2_VALUE);
    }

    @Test
    public void testGetPropertyWithLocalValuesAndInheritedValuesButDefaultValue () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(PROP1_NAME, PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(PROP2_NAME, PROP2_VALUE);

        // Asserts
        String defaultValue = "default";
        assertThat(typedProperties.getProperty(PROP1_NAME, defaultValue)).isEqualTo(PROP1_VALUE);
        assertThat(typedProperties.getProperty(PROP2_NAME, defaultValue)).isEqualTo(PROP2_VALUE);
        assertThat(typedProperties.getProperty("undefined", defaultValue)).isEqualTo(defaultValue);
    }

    @Test
    public void testGetTypedPropertyWithoutInheritance () {
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);

        // Asserts
        assertThat(typedProperties.<BigDecimal>getTypedProperty(NUMERIC_PROP1_NAME)).isEqualTo(NUMERIC_PROP1_VALUE);
        assertThat(typedProperties.<BigDecimal>getTypedProperty(NUMERIC_PROP2_NAME)).isNull();
    }

    @Test
    public void testGetTypedPropertyWithoutInheritanceButDefaultValue () {
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);

        // Asserts
        assertThat(typedProperties.<BigDecimal>getTypedProperty(NUMERIC_PROP1_NAME)).isEqualTo(NUMERIC_PROP1_VALUE);
        BigDecimal defaultValue = new BigDecimal(753);
        assertThat(typedProperties.getTypedProperty(NUMERIC_PROP2_NAME, defaultValue)).isEqualTo(defaultValue);
    }

    @Test
    public void testGetTypedPropertyWithOnlyInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties locallyEmpty = TypedProperties.inheritingFrom(parentProperties);

        // Asserts
        assertThat(locallyEmpty.<BigDecimal>getTypedProperty(NUMERIC_PROP1_NAME)).isEqualTo(NUMERIC_PROP1_VALUE);
        assertThat(locallyEmpty.<BigDecimal>getTypedProperty(NUMERIC_PROP2_NAME)).isNull();
    }

    @Test
    public void testGetTypedPropertyWithOnlyInheritedValuesButDefaultValue () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties locallyEmpty = TypedProperties.inheritingFrom(parentProperties);

        // Asserts
        assertThat(locallyEmpty.<BigDecimal>getTypedProperty(NUMERIC_PROP1_NAME)).isEqualTo(NUMERIC_PROP1_VALUE);
        BigDecimal defaultValue = new BigDecimal(753);
        assertThat(locallyEmpty.getTypedProperty(NUMERIC_PROP2_NAME, defaultValue)).isEqualTo(defaultValue);
    }

    @Test
    public void testGetTypedPropertyWithLocalValuesAndInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);

        // Asserts
        assertThat(typedProperties.<BigDecimal>getTypedProperty(NUMERIC_PROP1_NAME)).isEqualTo(NUMERIC_PROP1_VALUE);
        assertThat(typedProperties.<BigDecimal>getTypedProperty(NUMERIC_PROP2_NAME)).isEqualTo(NUMERIC_PROP2_VALUE);
    }

    @Test
    public void testGetTypedPropertyWithLocalValuesAndInheritedValuesButDefaultValue () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);

        // Asserts
        assertThat(typedProperties.<BigDecimal>getTypedProperty(NUMERIC_PROP1_NAME)).isEqualTo(NUMERIC_PROP1_VALUE);
        assertThat(typedProperties.<BigDecimal>getTypedProperty(NUMERIC_PROP2_NAME)).isEqualTo(NUMERIC_PROP2_VALUE);
        BigDecimal defaultValue = new BigDecimal(753);
        assertThat(typedProperties.getTypedProperty("undefined", defaultValue)).isEqualTo(defaultValue);
    }

    @Test
    public void testHasLocalValueForWithoutInheritance () {
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);

        // Asserts
        assertThat(typedProperties.hasLocalValueFor(NUMERIC_PROP1_NAME)).isTrue();
        assertThat(typedProperties.hasLocalValueFor(NUMERIC_PROP2_NAME)).isFalse();
    }

    @Test
    public void testHasLocalValueForWithOnlyInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties locallyEmpty = TypedProperties.inheritingFrom(parentProperties);

        // Asserts
        assertThat(locallyEmpty.hasLocalValueFor(NUMERIC_PROP1_NAME)).isFalse();
        assertThat(locallyEmpty.hasLocalValueFor(NUMERIC_PROP2_NAME)).isFalse();
    }

    @Test
    public void testHasLocalValueForWithLocalValuesAndInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);

        // Asserts
        assertThat(typedProperties.hasLocalValueFor(NUMERIC_PROP1_NAME)).isFalse();
        assertThat(typedProperties.hasLocalValueFor(NUMERIC_PROP2_NAME)).isTrue();
    }

    @Test
    public void testLocalSizeWithoutInheritance () {
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);

        // Asserts
        assertThat(typedProperties.localSize()).isEqualTo(1);
    }

    @Test
    public void testLocalSizeWithOnlyInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties locallyEmpty = TypedProperties.inheritingFrom(parentProperties);

        // Asserts
        assertThat(locallyEmpty.localSize()).isZero();
    }

    @Test
    public void testLocalSizeWithLocalValuesAndInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);

        // Asserts
        assertThat(typedProperties.localSize()).isEqualTo(1);
    }

    @Test
    public void testSizeWithoutInheritance () {
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);

        // Asserts
        assertThat(typedProperties.size()).isEqualTo(1);
    }

    @Test
    public void testSizeWithOnlyInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties locallyEmpty = TypedProperties.inheritingFrom(parentProperties);

        // Asserts
        assertThat(locallyEmpty.size()).isEqualTo(1);
    }

    @Test
    public void testSizeWithLocalValuesAndInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);

        // Asserts
        assertThat(typedProperties.size()).isEqualTo(2);
    }

    @Test
    public void testSizeWithLocalValuesAndOverruledInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);

        // Asserts
        assertThat(typedProperties.size()).isEqualTo(1);
    }

    @Test
    public void testIsLocalValueForWithoutInheritance () {
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);

        // Asserts
        assertThat(typedProperties.isLocalValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP1_NAME)).isTrue();
        assertThat(typedProperties.isLocalValueFor(NUMERIC_PROP2_VALUE, NUMERIC_PROP1_NAME)).isFalse();
        assertThat(typedProperties.isLocalValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP2_NAME)).isFalse();
        assertThat(typedProperties.isLocalValueFor(NUMERIC_PROP2_VALUE, NUMERIC_PROP2_NAME)).isFalse();
    }

    @Test
    public void testIsLocalValueForWithOnlyInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties locallyEmpty = TypedProperties.inheritingFrom(parentProperties);

        // Asserts
        assertThat(locallyEmpty.isLocalValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP1_NAME)).isFalse();
        assertThat(locallyEmpty.isLocalValueFor(NUMERIC_PROP2_VALUE, NUMERIC_PROP1_NAME)).isFalse();
        assertThat(locallyEmpty.isLocalValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP2_NAME)).isFalse();
        assertThat(locallyEmpty.isLocalValueFor(NUMERIC_PROP2_VALUE, NUMERIC_PROP2_NAME)).isFalse();
    }

    @Test
    public void testIsLocalValueForWithLocalValuesAndInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);

        // Asserts
        assertThat(typedProperties.isLocalValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP1_NAME)).isFalse();
        assertThat(typedProperties.isLocalValueFor(NUMERIC_PROP2_VALUE, NUMERIC_PROP1_NAME)).isFalse();
        assertThat(typedProperties.isLocalValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP2_NAME)).isFalse();
        assertThat(typedProperties.isLocalValueFor(NUMERIC_PROP2_VALUE, NUMERIC_PROP2_NAME)).isTrue();
    }

    @Test
    public void testIsLocalValueForWithOverruledInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);
        String overruledValue = "overruled";

        // Business method
        typedProperties.setProperty(NUMERIC_PROP1_NAME, overruledValue);

        // Asserts
        assertThat(typedProperties.isLocalValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP1_NAME)).isFalse();
        assertThat(typedProperties.isLocalValueFor(overruledValue, NUMERIC_PROP1_NAME)).isTrue();
    }

    @Test
    public void testIsLocalValueForWithRemovedOverruledInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);
        String overruledValue = "overruled";
        typedProperties.setProperty(NUMERIC_PROP1_NAME, overruledValue);

        // Business method
        typedProperties.removeProperty(NUMERIC_PROP1_NAME);

        // Asserts
        assertThat(typedProperties.isLocalValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP1_NAME)).isFalse();
        assertThat(typedProperties.isLocalValueFor(overruledValue, NUMERIC_PROP1_NAME)).isFalse();
    }

    @Test
    public void testIsInheritedValueForWithOverruledInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);
        String overruledValue = "overruled";

        // Business method
        typedProperties.setProperty(NUMERIC_PROP1_NAME, overruledValue);

        // Asserts
        assertThat(typedProperties.isInheritedValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP1_NAME)).isTrue();
        assertThat(typedProperties.isInheritedValueFor(overruledValue, NUMERIC_PROP1_NAME)).isFalse();
    }

    @Test
    public void testIsInheritedValueForWithRemovedOverruledInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);
        String overruledValue = "overruled";
        typedProperties.setProperty(NUMERIC_PROP1_NAME, overruledValue);

        // Business method
        typedProperties.removeProperty(NUMERIC_PROP1_NAME);

        // Asserts
        assertThat(typedProperties.isInheritedValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP1_NAME)).isTrue();
        assertThat(typedProperties.isInheritedValueFor(overruledValue, NUMERIC_PROP1_NAME)).isFalse();
    }

    @Test
    public void testHasInheritedValueForWithoutInheritance () {
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);

        // Asserts
        assertThat(typedProperties.hasInheritedValueFor(NUMERIC_PROP1_NAME)).isFalse();
        assertThat(typedProperties.hasInheritedValueFor(NUMERIC_PROP2_NAME)).isFalse();
    }

    @Test
    public void testGetInheritedValueWithoutInheritance () {
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);

        // Asserts
        assertThat(typedProperties.getInheritedValue(NUMERIC_PROP1_NAME)).isNull();
        assertThat(typedProperties.getInheritedValue(NUMERIC_PROP2_NAME)).isNull();
    }

    @Test
    public void testHasInheritedValueForWithOnlyInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties locallyEmpty = TypedProperties.inheritingFrom(parentProperties);

        // Asserts
        assertThat(locallyEmpty.hasInheritedValueFor(NUMERIC_PROP1_NAME)).isTrue();
        assertThat(locallyEmpty.hasInheritedValueFor(NUMERIC_PROP2_NAME)).isFalse();
    }

    @Test
    public void testGetInheritedValueWithOnlyInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties locallyEmpty = TypedProperties.inheritingFrom(parentProperties);

        // Asserts
        assertThat(locallyEmpty.getInheritedValue(NUMERIC_PROP1_NAME)).isEqualTo(NUMERIC_PROP1_VALUE);
        assertThat(locallyEmpty.getInheritedValue(NUMERIC_PROP2_NAME)).isNull();
    }

    @Test
    public void testHasInheritedValueForWithLocalValuesAndInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);

        // Asserts
        assertThat(typedProperties.hasInheritedValueFor(NUMERIC_PROP1_NAME)).isTrue();
        assertThat(typedProperties.hasInheritedValueFor(NUMERIC_PROP2_NAME)).isFalse();
    }

    @Test
    public void testGetInheritedValueWithLocalValuesAndInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);

        // Asserts
        assertThat(typedProperties.getInheritedValue(NUMERIC_PROP1_NAME)).isEqualTo(NUMERIC_PROP1_VALUE);
        assertThat(typedProperties.getInheritedValue(NUMERIC_PROP2_NAME)).isNull();
    }

    @Test
    public void testHasInheritedValueForTwoLevelsOfInheritance () {
        TypedProperties topLevelProperties = TypedProperties.empty();
        topLevelProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties midLevelProperties = TypedProperties.inheritingFrom(topLevelProperties);
        // No local properties at the mid level
        TypedProperties typedProperties = TypedProperties.inheritingFrom(midLevelProperties);
        typedProperties.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);

        // Asserts
        assertThat(typedProperties.hasInheritedValueFor(NUMERIC_PROP1_NAME)).isTrue();
        assertThat(typedProperties.hasInheritedValueFor(NUMERIC_PROP2_NAME)).isFalse();
    }

    @Test
    public void testGetInheritedValueForTwoLevelsOfInheritance () {
        TypedProperties topLevelProperties = TypedProperties.empty();
        topLevelProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties midLevelProperties = TypedProperties.inheritingFrom(topLevelProperties);
        // No local properties at the mid level
        TypedProperties typedProperties = TypedProperties.inheritingFrom(midLevelProperties);
        typedProperties.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);

        // Asserts
        assertThat(typedProperties.getInheritedValue(NUMERIC_PROP1_NAME)).isEqualTo(NUMERIC_PROP1_VALUE);
        assertThat(typedProperties.getInheritedValue(NUMERIC_PROP2_NAME)).isNull();
    }

    @Test
    public void testIsInheritedValueForWithoutInheritance () {
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);

        // Asserts
        assertThat(typedProperties.isInheritedValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP1_NAME)).isFalse();
        assertThat(typedProperties.isInheritedValueFor(NUMERIC_PROP2_VALUE, NUMERIC_PROP1_NAME)).isFalse();
        assertThat(typedProperties.isInheritedValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP2_NAME)).isFalse();
        assertThat(typedProperties.isInheritedValueFor(NUMERIC_PROP2_VALUE, NUMERIC_PROP2_NAME)).isFalse();
    }

    @Test
    public void testIsInheritedValueForWithOnlyInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties locallyEmpty = TypedProperties.inheritingFrom(parentProperties);

        // Asserts
        assertThat(locallyEmpty.isInheritedValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP1_NAME)).isTrue();
        assertThat(locallyEmpty.isInheritedValueFor(NUMERIC_PROP2_VALUE, NUMERIC_PROP1_NAME)).isFalse();
        assertThat(locallyEmpty.isInheritedValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP2_NAME)).isFalse();
        assertThat(locallyEmpty.isInheritedValueFor(NUMERIC_PROP2_VALUE, NUMERIC_PROP2_NAME)).isFalse();
    }

    @Test
    public void testIsInheritedValueForWithLocalValuesAndInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);

        // Asserts
        assertThat(typedProperties.isInheritedValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP1_NAME)).isTrue();
        assertThat(typedProperties.isInheritedValueFor(NUMERIC_PROP2_VALUE, NUMERIC_PROP1_NAME)).isFalse();
        assertThat(typedProperties.isInheritedValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP2_NAME)).isFalse();
        assertThat(typedProperties.isInheritedValueFor(NUMERIC_PROP2_VALUE, NUMERIC_PROP2_NAME)).isFalse();
    }

    @Test
    public void testHasValueForWithoutInheritance () {
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);

        // Asserts
        assertThat(typedProperties.hasValueFor(NUMERIC_PROP1_NAME)).isTrue();
        assertThat(typedProperties.hasValueFor(NUMERIC_PROP2_NAME)).isFalse();
    }

    @Test
    public void testHasValueForWithOnlyInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties locallyEmpty = TypedProperties.inheritingFrom(parentProperties);

        // Asserts
        assertThat(locallyEmpty.hasValueFor(NUMERIC_PROP1_NAME)).isTrue();
        assertThat(locallyEmpty.hasValueFor(NUMERIC_PROP2_NAME)).isFalse();
    }

    @Test
    public void testHasValueForWithLocalValuesAndInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);

        // Asserts
        assertThat(typedProperties.hasValueFor(NUMERIC_PROP1_NAME)).isTrue();
        assertThat(typedProperties.hasValueFor(NUMERIC_PROP2_NAME)).isTrue();
    }

    @Test
    public void testIsValueForWithoutInheritance () {
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);

        // Asserts
        assertThat(typedProperties.isValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP1_NAME)).isTrue();
        assertThat(typedProperties.isValueFor(NUMERIC_PROP2_VALUE, NUMERIC_PROP1_NAME)).isFalse();
        assertThat(typedProperties.isValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP2_NAME)).isFalse();
        assertThat(typedProperties.isValueFor(NUMERIC_PROP2_VALUE, NUMERIC_PROP2_NAME)).isFalse();
    }

    @Test
    public void testIsValueForWithOnlyInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties locallyEmpty = TypedProperties.inheritingFrom(parentProperties);

        // Asserts
        assertThat(locallyEmpty.isValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP1_NAME)).isTrue();
        assertThat(locallyEmpty.isValueFor(NUMERIC_PROP2_VALUE, NUMERIC_PROP1_NAME)).isFalse();
        assertThat(locallyEmpty.isValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP2_NAME)).isFalse();
        assertThat(locallyEmpty.isValueFor(NUMERIC_PROP2_VALUE, NUMERIC_PROP2_NAME)).isFalse();
    }

    @Test
    public void testIsValueForWithLocalValuesAndInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);

        // Asserts
        assertThat(typedProperties.isValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP1_NAME)).isTrue();
        assertThat(typedProperties.isValueFor(NUMERIC_PROP2_VALUE, NUMERIC_PROP1_NAME)).isFalse();
        assertThat(typedProperties.isValueFor(NUMERIC_PROP1_VALUE, NUMERIC_PROP2_NAME)).isFalse();
        assertThat(typedProperties.isValueFor(NUMERIC_PROP2_VALUE, NUMERIC_PROP2_NAME)).isTrue();
    }

    @Test
    public void testPropertyNamesWithoutInheritance () {
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);

        // Asserts
        assertThat(typedProperties.propertyNames()).containsOnly(NUMERIC_PROP1_NAME);
    }

    @Test
    public void testPropertyNamesWithOnlyInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties locallyEmpty = TypedProperties.inheritingFrom(parentProperties);

        // Asserts
        assertThat(locallyEmpty.propertyNames()).containsOnly(NUMERIC_PROP1_NAME);
    }

    @Test
    public void testPropertyNamesWithLocalValuesAndInheritedValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);

        // Asserts
        assertThat(typedProperties.propertyNames()).containsOnly(NUMERIC_PROP1_NAME, NUMERIC_PROP2_NAME);
    }

    @Test
    public void testCopyOfOtherProperties () {
        Properties original = new Properties();
        original.setProperty(PROP1_NAME, PROP1_VALUE);
        original.setProperty(PROP2_NAME, PROP2_VALUE);

        // Business method
        TypedProperties copied = TypedProperties.copyOf(original);

        // Asserts
        assertThat(copied.getProperty(PROP1_NAME)).isEqualTo(PROP1_VALUE);
        assertThat(copied.getProperty(PROP2_NAME)).isEqualTo(PROP2_VALUE);
    }

    @Test
    public void testCopyOfOtherTypedProperties () {
        TypedProperties original = TypedProperties.empty();
        original.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        original.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);

        // Business method
        TypedProperties copied = TypedProperties.copyOf(original);

        // Asserts
        assertThat(copied.getProperty(NUMERIC_PROP1_NAME)).isEqualTo(NUMERIC_PROP1_VALUE);
        assertThat(copied.getProperty(NUMERIC_PROP2_NAME)).isEqualTo(NUMERIC_PROP2_VALUE);
    }

    @Test
    public void testCopyOfOtherTypedPropertiesIsDetachedFromCopy () {
        TypedProperties original = TypedProperties.empty();
        original.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        original.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);

        // Business method
        TypedProperties copied = TypedProperties.copyOf(original);
        copied.removeProperty(NUMERIC_PROP1_NAME);
        copied.removeProperty(NUMERIC_PROP2_NAME);

        // Asserts
        assertThat(original.hasValueFor(NUMERIC_PROP1_NAME)).isTrue();
        assertThat(original.hasValueFor(NUMERIC_PROP2_NAME)).isTrue();
    }

    @Test
    public void testToStringPropertiesOnlyCopiesLocalValues () {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(PROP1_NAME, PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(PROP2_NAME, PROP2_VALUE);
        String booleanPropertyName = "boolean";
        typedProperties.setProperty(booleanPropertyName, Boolean.TRUE);

        // Business method
        Properties properties = typedProperties.toStringProperties();

        // Asserts
        assertThat(properties.keySet()).containsOnly(PROP2_NAME, booleanPropertyName);
        assertThat(properties.get(PROP2_NAME)).isEqualTo(PROP2_VALUE);
        assertThat(properties.get(booleanPropertyName)).isEqualTo("1");
    }

    @Test
    public void testEqualityWithTypedProperties () {
        List<String> keys = new ArrayList<>();
        keys.add("azerty");
        keys.add("verhko");
        keys.add("mgfo");
        keys.add("jhpujxlghl");
        keys.add("mgi");
        keys.add("mdghergsdmuj");
        keys.add("fvbopeuyaz");

        TypedProperties props1 = TypedProperties.empty();
        for (String each : keys) {
            props1.setProperty(each, each.length());
        }

        Collections.shuffle(keys);
        TypedProperties props2 = TypedProperties.empty();
        for (String each : keys) {
            props2.setProperty(each, each.length());
        }

        assertThat(props1.equals(props2)).isTrue();
    }

    @Test
    public void testEqualityWithCopyOfTypedProperties () {
        List<String> keys = new ArrayList<>();
        keys.add("azerty");
        keys.add("verhko");
        keys.add("mgfo");
        keys.add("jhpujxlghl");
        keys.add("mgi");
        keys.add("mdghergsdmuj");
        keys.add("fvbopeuyaz");
        TypedProperties props1 = TypedProperties.empty();
        for (String each : keys) {
            props1.setProperty(each, each.length());
        }
        TypedProperties props2 = TypedProperties.copyOf(props1);

        assertThat(props1.equals(props2)).isTrue();
    }

    @Test
    public void testEqualityOnEmptyTypedProperties() {
        TypedProperties props1 = TypedProperties.empty();
        TypedProperties props2 = TypedProperties.empty();
        assertThat(props1.equals(props2)).isTrue();
    }

    @Test
    public void testEqualityWithSameTypedProperties() {
        TypedProperties props1 = TypedProperties.empty();
        assertThat(props1.equals(props1)).isTrue();
    }

    @Test
    public void testEqualityWithNull () {
        TypedProperties props1 = TypedProperties.empty();
        assertThat(props1.equals(null)).isFalse();
    }

    @Test
    public void testEqualityWithString () {
        TypedProperties props1 = TypedProperties.empty();
        assertThat(props1.equals("aString")).isFalse();
    }

    @Test
    public void testToStringPropertiesHasAllProperties() {
        TypedProperties parentProperties = TypedProperties.empty();
        parentProperties.setProperty(PROP1_NAME, PROP1_VALUE);
        TypedProperties typedProperties = TypedProperties.inheritingFrom(parentProperties);
        typedProperties.setProperty(PROP2_NAME, PROP2_VALUE);
        String booleanPropertyName = "boolean";
        typedProperties.setProperty(booleanPropertyName, Boolean.TRUE);

        // Business method
        Properties properties = typedProperties.toStringProperties();

        // Asserts
        assertThat(properties.keySet()).containsOnly(PROP1_NAME, PROP2_NAME, booleanPropertyName);
        assertThat(properties.get(PROP2_NAME)).isEqualTo(PROP2_VALUE);
        assertThat(properties.get(booleanPropertyName)).isEqualTo("1");
    }

}
