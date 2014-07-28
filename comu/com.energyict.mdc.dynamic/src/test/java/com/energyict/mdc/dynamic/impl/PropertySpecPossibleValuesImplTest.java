package com.energyict.mdc.dynamic.impl;

import org.junit.*;

import com.elster.jupiter.properties.PropertySpecPossibleValuesImpl;

import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link PropertySpecPossibleValuesImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-19 (16:19)
 */
public class PropertySpecPossibleValuesImplTest extends AbstractPropertySpecTest {

    @Test
    public void testDefaultConstructor () {
        // Business method
        PropertySpecPossibleValuesImpl possibleValues = new PropertySpecPossibleValuesImpl();

        // Asserts
        assertThat(possibleValues.isExhaustive()).isFalse();
        assertThat(possibleValues.getAllValues()).isEmpty();
        assertThat(possibleValues.getDefault()).isNull();
    }

    @Test
    public void testConstructorWithoutDefault () {
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";

        // Business method
        PropertySpecPossibleValuesImpl<String> possibleValues = new PropertySpecPossibleValuesImpl<>(false, Arrays.asList(otherValue1, otherValue2));

        // Asserts
        assertThat(possibleValues.isExhaustive()).isFalse();
        assertThat(possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.getDefault()).isNull();
    }

    @Test
    public void testSetDefaultWithoutPrevious () {
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";
        String defaultValue = "DEFAULT";
        PropertySpecPossibleValuesImpl<String> possibleValues = new PropertySpecPossibleValuesImpl<>(false, Arrays.asList(otherValue1, otherValue2));

        // Business method
        possibleValues.setDefault(defaultValue);

        // Asserts
        assertThat(possibleValues.isExhaustive()).isFalse();
        assertThat(possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.getDefault()).isEqualTo(defaultValue);
    }

    @Test
    public void testSetDefaultWithPrevious () {
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";
        String initialDefaultValue = "INITIAL-DEFAULT";
        PropertySpecPossibleValuesImpl<String> possibleValues = new PropertySpecPossibleValuesImpl<>(initialDefaultValue, false, Arrays.asList(otherValue1, otherValue2));
        String newDefaultValue = "NEW-DEFAULT";

        // Business method
        possibleValues.setDefault(newDefaultValue);

        // Asserts
        assertThat(possibleValues.isExhaustive()).isFalse();
        assertThat(possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.getDefault()).isEqualTo(newDefaultValue);
    }

    @Test
    public void testConstructorWithDefaultAndNoOtherValues () {
        String defaultValue = "DEFAULT";

        // Business method
        PropertySpecPossibleValuesImpl<String> possibleValues =
                new PropertySpecPossibleValuesImpl<>(defaultValue, false);

        // Asserts
        assertThat(possibleValues.isExhaustive()).isFalse();
        assertThat(possibleValues.getAllValues()).isEmpty();
        assertThat(possibleValues.getDefault()).isEqualTo(defaultValue);
    }

    @Test
    public void testAddValuesAfterConstructionWithoutInitialValues () {
        String defaultValue = "DEFAULT";
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";
        PropertySpecPossibleValuesImpl<String> possibleValues =
                new PropertySpecPossibleValuesImpl<>(defaultValue, false);

        // Business method
        possibleValues.add(otherValue1, otherValue2);

        // Asserts
        assertThat(possibleValues.isExhaustive()).isFalse();
        assertThat(possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.getDefault()).isEqualTo(defaultValue);
    }

    @Test
    public void testSetExhaustiveFlagAfterConstruction () {
        String defaultValue = "DEFAULT";
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";
        PropertySpecPossibleValuesImpl<String> possibleValues =
                new PropertySpecPossibleValuesImpl<>(defaultValue, false, Arrays.asList(otherValue1, otherValue2));

        // Business method
        possibleValues.setExhaustive(true);

        // Asserts
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.getDefault()).isEqualTo(defaultValue);
    }

    @Test
    public void testConstructorWithDefaultAndOtherValues () {
        // Business method
        String defaultValue = "DEFAULT";
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";
        PropertySpecPossibleValuesImpl<String> possibleValues =
                new PropertySpecPossibleValuesImpl<>(defaultValue, false, otherValue1, otherValue2);

        // Asserts
        assertThat(possibleValues.isExhaustive()).isFalse();
        assertThat(possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.getDefault()).isEqualTo(defaultValue);
    }

    @Test
    public void testConstructorWithDefaultAndOtherExhaustiveValues () {
        // Business method
        String defaultValue = "DEFAULT";
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";
        PropertySpecPossibleValuesImpl<String> possibleValues =
                new PropertySpecPossibleValuesImpl<>(defaultValue, true, otherValue1, otherValue2);

        // Asserts
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.getDefault()).isEqualTo(defaultValue);
    }

}