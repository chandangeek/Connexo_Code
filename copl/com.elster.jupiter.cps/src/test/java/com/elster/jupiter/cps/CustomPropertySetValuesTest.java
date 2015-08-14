package com.elster.jupiter.cps;

import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;

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
        Range<Instant> effectiveRange = empty.getEffectiveRange();
        assertThat(effectiveRange).isNotNull();
        assertThat(effectiveRange.hasLowerBound()).isTrue();
        assertThat(effectiveRange.lowerEndpoint()).isEqualTo(Instant.EPOCH);
        assertThat(effectiveRange.hasUpperBound()).isFalse();
    }

    @Test
    public void testFromEmptyConstructor () {
        Instant from = Instant.ofEpochSecond(1000L);

        // Business method
        CustomPropertySetValues empty = CustomPropertySetValues.emptyFrom(from);

        // Asserts
        assertThat(empty.isEmpty()).isTrue();
        assertThat(empty.size()).isZero();
        assertThat(empty.propertyNames()).isEmpty();
        Range<Instant> effectiveRange = empty.getEffectiveRange();
        assertThat(effectiveRange).isNotNull();
        assertThat(effectiveRange.hasLowerBound()).isTrue();
        assertThat(effectiveRange.lowerEndpoint()).isEqualTo(from);
        assertThat(effectiveRange.hasUpperBound()).isFalse();
    }

    @Test
    public void testDurationEmptyConstructor () {
        Instant from = Instant.ofEpochSecond(1000L);
        Instant to = from.plusSeconds(1000L);

        // Business method
        CustomPropertySetValues empty = CustomPropertySetValues.emptyDuring(Interval.of(from, to));

        // Asserts
        assertThat(empty.isEmpty()).isTrue();
        assertThat(empty.size()).isZero();
        assertThat(empty.propertyNames()).isEmpty();
        Range<Instant> effectiveRange = empty.getEffectiveRange();
        assertThat(effectiveRange).isNotNull();
        assertThat(effectiveRange.hasLowerBound()).isTrue();
        assertThat(effectiveRange.lowerEndpoint()).isEqualTo(from);
        assertThat(effectiveRange.hasUpperBound()).isTrue();
        assertThat(effectiveRange.upperEndpoint()).isEqualTo(to);
    }

    @Test
    public void notEffectiveWithFromConstructor() {
        Instant instant = Instant.ofEpochSecond(1000L);
        Instant from = instant.plusSeconds(1000L);
        CustomPropertySetValues empty = CustomPropertySetValues.emptyFrom(from);

        // Business method & asserts
        assertThat(empty.isEffectiveAt(instant)).isFalse();
    }

    @Test
    public void effectiveAtFromWithFromConstructor() {
        Instant from = Instant.ofEpochSecond(1000L);
        CustomPropertySetValues empty = CustomPropertySetValues.emptyFrom(from);

        // Business method & asserts
        assertThat(empty.isEffectiveAt(from)).isTrue();
    }

    @Test
    public void effectiveAfterFromWithFromConstructor() {
        Instant from = Instant.ofEpochSecond(1000L);
        Instant instant = from.plusSeconds(1000L);
        CustomPropertySetValues empty = CustomPropertySetValues.emptyFrom(from);

        // Business method & asserts
        assertThat(empty.isEffectiveAt(instant)).isTrue();
    }

    @Test
    public void notEffectiveWithDuringConstructor() {
        Instant instant = Instant.ofEpochSecond(1000L);
        Instant from = instant.plusSeconds(1000L);
        Instant to = from.plusSeconds(1000L);
        CustomPropertySetValues empty = CustomPropertySetValues.emptyDuring(Interval.of(from, to));

        // Business method & asserts
        assertThat(empty.isEffectiveAt(instant)).isFalse();
    }

    @Test
    public void effectiveAtFromWithDuringConstructor() {
        Instant from = Instant.ofEpochSecond(1000L);
        Instant to = from.plusSeconds(1000L);
        CustomPropertySetValues empty = CustomPropertySetValues.emptyDuring(Interval.of(from, to));

        // Business method & asserts
        assertThat(empty.isEffectiveAt(from)).isTrue();
    }

    @Test
    public void effectiveAfterFromWithDuringConstructor() {
        Instant from = Instant.ofEpochSecond(1000L);
        Instant instant = from.plusSeconds(500L);
        Instant to = instant.plusSeconds(500L);
        CustomPropertySetValues empty = CustomPropertySetValues.emptyDuring(Interval.of(from, to));

        // Business method & asserts
        assertThat(empty.isEffectiveAt(instant)).isTrue();
    }

    @Test
    public void notEffectiveAtToWithDuringConstructor() {
        Instant from = Instant.ofEpochSecond(1000L);
        Instant to = from.plusSeconds(1000L);
        CustomPropertySetValues empty = CustomPropertySetValues.emptyDuring(Interval.of(from, to));

        // Business method & asserts
        assertThat(empty.isEffectiveAt(to)).isFalse();
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