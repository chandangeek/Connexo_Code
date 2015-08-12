package com.elster.jupiter.cps;

import java.math.BigDecimal;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link CustomPropertySetValues} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-20 (17:04)
 */
public class CustomPropertySetValuesTest {

    private static final String PROP1_NAME = "prop1";
    private static final String PROP1_VALUE = "value1";
    private static final String PROP2_NAME = "prop2";
    private static final String NUMERIC_PROP1_NAME = "numeric_prop1";
    private static final BigDecimal NUMERIC_PROP1_VALUE = BigDecimal.TEN;
    private static final String NUMERIC_PROP2_NAME = "numeric_prop2";
    private static final BigDecimal NUMERIC_PROP2_VALUE = new BigDecimal(951);

    @Test
    public void testEmptyConstructor () {
        // Business method
        CustomPropertySetValues empty = CustomPropertySetValues.empty();

        // Asserts
        assertThat(empty.isEmpty()).isTrue();
        assertThat(empty.size()).isZero();
        assertThat(empty.propertyNames()).isEmpty();
    }

    @Test
    public void testSetProperty() {
        CustomPropertySetValues typedProperties = CustomPropertySetValues.empty();
        typedProperties.setProperty(PROP1_NAME, PROP1_VALUE);

        // Asserts
        assertThat(typedProperties.isEmpty()).isFalse();
    }

    @Test
    public void testGetProperty() {
        CustomPropertySetValues typedProperties = CustomPropertySetValues.empty();
        typedProperties.setProperty(PROP1_NAME, PROP1_VALUE);

        // Asserts
        assertThat(typedProperties.getProperty(PROP1_NAME)).isEqualTo(PROP1_VALUE);
        assertThat(typedProperties.getProperty(PROP2_NAME)).isNull();
    }

    @Test
    public void testSize() {
        CustomPropertySetValues typedProperties = CustomPropertySetValues.empty();
        typedProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);

        // Asserts
        assertThat(typedProperties.size()).isEqualTo(1);
    }

    @Test
    public void testPropertyNames() {
        CustomPropertySetValues typedProperties = CustomPropertySetValues.empty();
        typedProperties.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);

        // Asserts
        assertThat(typedProperties.propertyNames()).containsOnly(NUMERIC_PROP1_NAME);
    }

    @Test
    public void testCopyOfOtherProperties () {
        CustomPropertySetValues original = CustomPropertySetValues.empty();
        original.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        original.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);

        // Business method
        CustomPropertySetValues copied = CustomPropertySetValues.copyOf(original);

        // Asserts
        assertThat(copied.getProperty(NUMERIC_PROP1_NAME)).isEqualTo(NUMERIC_PROP1_VALUE);
        assertThat(copied.getProperty(NUMERIC_PROP2_NAME)).isEqualTo(NUMERIC_PROP2_VALUE);
    }

    @Test
    public void testCopyOfOtherPropertiesIsDetachedFromCopy () {
        CustomPropertySetValues original = CustomPropertySetValues.empty();
        original.setProperty(NUMERIC_PROP1_NAME, NUMERIC_PROP1_VALUE);
        original.setProperty(NUMERIC_PROP2_NAME, NUMERIC_PROP2_VALUE);

        // Business method
        CustomPropertySetValues copied = CustomPropertySetValues.copyOf(original);
        copied.removeProperty(NUMERIC_PROP1_NAME);
        copied.removeProperty(NUMERIC_PROP2_NAME);

        // Asserts
        assertThat(original.getProperty(NUMERIC_PROP1_NAME)).isNotNull();
        assertThat(original.getProperty(NUMERIC_PROP2_NAME)).isNotNull();
    }

}