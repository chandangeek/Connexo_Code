/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.impl;

import com.elster.jupiter.properties.impl.PropertySpecPossibleValuesImpl;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PropertySpecPossibleValuesImplTest {

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
        PropertySpecPossibleValuesImpl possibleValues = new PropertySpecPossibleValuesImpl(false, Arrays.asList(otherValue1, otherValue2));

        // Asserts
        assertThat(possibleValues.isExhaustive()).isFalse();
        assertThat((List<String>) possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.getDefault()).isNull();
    }

    @Test
    public void testSetDefaultWithoutPrevious () {
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";
        String defaultValue = "DEFAULT";
        PropertySpecPossibleValuesImpl possibleValues = new PropertySpecPossibleValuesImpl(false, Arrays.asList(otherValue1, otherValue2));

        // Business method
        possibleValues.setDefault(defaultValue);

        // Asserts
        assertThat(possibleValues.isExhaustive()).isFalse();
        assertThat((List<String>) possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.getDefault()).isEqualTo(defaultValue);
    }

    @Test
    public void testSetDefaultWithPrevious () {
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";
        String initialDefaultValue = "INITIAL-DEFAULT";
        PropertySpecPossibleValuesImpl possibleValues = new PropertySpecPossibleValuesImpl(initialDefaultValue, false, Arrays.asList(otherValue1, otherValue2));
        String newDefaultValue = "NEW-DEFAULT";

        // Business method
        possibleValues.setDefault(newDefaultValue);

        // Asserts
        assertThat(possibleValues.isExhaustive()).isFalse();
        assertThat((List<String>) possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.getDefault()).isEqualTo(newDefaultValue);
    }

    @Test
    public void testConstructorWithDefaultAndNoOtherValues () {
        String defaultValue = "DEFAULT";

        // Business method
        PropertySpecPossibleValuesImpl possibleValues =
                new PropertySpecPossibleValuesImpl(defaultValue, false);

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
        PropertySpecPossibleValuesImpl possibleValues =
                new PropertySpecPossibleValuesImpl(defaultValue, false);

        // Business method
        possibleValues.add(otherValue1, otherValue2);

        // Asserts
        assertThat(possibleValues.isExhaustive()).isFalse();
        assertThat((List<String>) possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.getDefault()).isEqualTo(defaultValue);
    }

    @Test
    public void testSetExhaustiveFlagAfterConstruction () {
        String defaultValue = "DEFAULT";
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";
        PropertySpecPossibleValuesImpl possibleValues =
                new PropertySpecPossibleValuesImpl(defaultValue, false, Arrays.asList(otherValue1, otherValue2));

        // Business method
        possibleValues.setExhaustive(true);

        // Asserts
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat((List<String>) possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.getDefault()).isEqualTo(defaultValue);
    }

    @Test
    public void testConstructorWithDefaultAndOtherValues () {
        // Business method
        String defaultValue = "DEFAULT";
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";
        PropertySpecPossibleValuesImpl possibleValues =
                new PropertySpecPossibleValuesImpl(defaultValue, false, otherValue1, otherValue2);

        // Asserts
        assertThat(possibleValues.isExhaustive()).isFalse();
        assertThat((List<String>) possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.getDefault()).isEqualTo(defaultValue);
    }

    @Test
    public void testConstructorWithDefaultAndOtherExhaustiveValues () {
        // Business method
        String defaultValue = "DEFAULT";
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";
        PropertySpecPossibleValuesImpl possibleValues =
                new PropertySpecPossibleValuesImpl(defaultValue, true, otherValue1, otherValue2);

        // Asserts
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat((List<String>) possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.getDefault()).isEqualTo(defaultValue);
    }

}