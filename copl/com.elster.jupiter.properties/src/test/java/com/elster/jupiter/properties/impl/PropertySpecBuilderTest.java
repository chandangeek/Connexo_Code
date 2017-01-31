/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.impl;


import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.StringFactory;

import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PropertySpecBuilderTest {

    @Test
    public void testSimpleStringProperty () {
        String specName = "exampleStringProperty";
        String specDisplayName = "Example string property";
        String specDescription = "Description for example string property";

        // Business methods
        PropertySpec propertySpec =
                new PropertySpecBuilderImpl<>(new StringFactory())
                        .setNameAndDescription(NameAndDescription.stringBased(specName, specDisplayName, specDescription))
                        .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(specName);
        assertThat(propertySpec.getDisplayName()).isEqualTo(specDisplayName);
        assertThat(propertySpec.getDescription()).isEqualTo(specDescription);
        assertThat(propertySpec.getPossibleValues()).isNull();
    }

    @Test
    public void testSimpleStringPropertyWithoutDescription () {
        String specName = "exampleStringProperty";
        String specDisplayName = "Example string property";

        // Business methods
        PropertySpec propertySpec =
                new PropertySpecBuilderImpl<>(new StringFactory())
                        .setNameAndDescription(NameAndDescription.stringBased(specName, specDisplayName, null))
                        .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(specName);
        assertThat(propertySpec.getDisplayName()).isEqualTo(specDisplayName);
        assertThat(propertySpec.getDescription()).isNull();
        assertThat(propertySpec.getPossibleValues()).isNull();
    }

    @Test
    public void testStringPropertyWithDefaultAndNoOtherValues () {
        String specName = "exampleStringProperty";
        String defaultValue = "DEFAULT";

        // Business methods
        PropertySpec propertySpec =
                new PropertySpecBuilderImpl<>(new StringFactory())
                        .setNameAndDescription(NameAndDescription.stringBased(specName, specName, null))
                        .setDefaultValue(defaultValue)
                        .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(specName);
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.getDefault()).isEqualTo(defaultValue);
        assertThat(possibleValues.getAllValues()).isEmpty();
        assertThat(possibleValues.isExhaustive()).isFalse();
    }

    @Test
    public void testChangeDefaultValueForStringProperty () {
        String specName = "exampleStringProperty";
        String initialDefaultValue = "INITIAL-DEFAULT";
        String newDefaultValue = "NEW-DEFAULT";
        PropertySpecBuilder<String> builder =
                new PropertySpecBuilderImpl<>(new StringFactory())
                        .setNameAndDescription(NameAndDescription.stringBased(specName, specName, null))
                        .setDefaultValue(initialDefaultValue);

        // Business methods
        builder.setDefaultValue(newDefaultValue);
        PropertySpec propertySpec = builder.finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(specName);
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.getDefault()).isEqualTo(newDefaultValue);
       // assertThat(possibleValues.getAllValues()).containsOnly(initialDefaultValue, newDefaultValue);
        assertThat(possibleValues.isExhaustive()).isFalse();
    }

    @Test
    public void testStringPropertyWithDefaultAndOtherValues () {
        String specName = "exampleStringProperty";
        String defaultValue = "DEFAULT";
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";

        // Business methods
        PropertySpec propertySpec =
                new PropertySpecBuilderImpl<>(new StringFactory())
                        .setNameAndDescription(NameAndDescription.stringBased(specName, specName, null))
                        .setDefaultValue(defaultValue)
                        .addValues(otherValue1, otherValue2)
                        .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(specName);
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.getDefault()).isEqualTo(defaultValue);
        assertThat((List<String>) possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.isExhaustive()).isFalse();
    }

    @Test
    public void testStringPropertyWithValuesButNoDefault () {
        String specName = "exampleStringProperty";
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";

        // Business methods
        PropertySpec propertySpec =
                new PropertySpecBuilderImpl<>(new StringFactory())
                        .setNameAndDescription(NameAndDescription.stringBased(specName, specName, null))
                        .addValues(otherValue1, otherValue2)
                        .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(specName);
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.getDefault()).isNull();
        assertThat((List<String>) possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.isExhaustive()).isFalse();
    }

    @Test
    public void testAddStringPropertyValuesInSeparateCalls () {
        String specName = "exampleStringProperty";
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";

        // Business methods
        PropertySpec propertySpec =
                new PropertySpecBuilderImpl<>(new StringFactory())
                        .setNameAndDescription(NameAndDescription.stringBased(specName, specName, null))
                        .addValues(otherValue1)
                        .addValues(otherValue2)
                        .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(specName);
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.getDefault()).isNull();
        assertThat((List<String>) possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.isExhaustive()).isFalse();
    }

    @Test
    public void testStringPropertyWithExhaustiveValues () {
        String specName = "exampleStringProperty";
        String defaultValue = "DEFAULT";
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";

        // Business methods
        PropertySpec propertySpec =
                new PropertySpecBuilderImpl<>(new StringFactory())
                        .setNameAndDescription(NameAndDescription.stringBased(specName, specName, null))
                        .setDefaultValue(defaultValue)
                        .addValues(otherValue1, otherValue2)
                        .markExhaustive()
                        .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(specName);
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.getDefault()).isEqualTo(defaultValue);
        assertThat((List<String>) possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.isExhaustive()).isTrue();
    }

    @Test
    public void testStringPropertyWithExhaustiveMarkerBeforeAddingValues () {
        String specName = "exampleStringProperty";
        String defaultValue = "DEFAULT";
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";

        // Business methods
        PropertySpec propertySpec =
                new PropertySpecBuilderImpl<>(new StringFactory())
                        .setNameAndDescription(NameAndDescription.stringBased(specName, specName, null))
                        .markExhaustive()
                        .setDefaultValue(defaultValue)
                        .addValues(otherValue1, otherValue2)
                        .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(specName);
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.getDefault()).isEqualTo(defaultValue);
        assertThat((List<String>) possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.isExhaustive()).isTrue();
    }

    @Test
    public void testFinishAgainReturnsTheSamePropertySpec () {
        String specName = "exampleStringProperty";
        PropertySpecBuilder builder = new PropertySpecBuilderImpl<>(new StringFactory())
                .setNameAndDescription(NameAndDescription.stringBased(specName, specName, null));
        PropertySpec propertySpec1 = builder.finish();

        // Business methods
        PropertySpec propertySpec2 = builder.finish();

        // Asserts
        assertThat(propertySpec1).isSameAs(propertySpec2);
    }

    @Test(expected = IllegalStateException.class)
    public void testCannotSetNameAfterFinish () {
        String specName = "exampleStringProperty";
        PropertySpecBuilderImpl<String> builder = new PropertySpecBuilderImpl<>(new StringFactory()).setNameAndDescription(NameAndDescription.stringBased(specName, specName, null));
        builder.finish();

        // Business methods
        builder.setNameAndDescription(NameAndDescription.stringBased("otherName", null, null));

        // Expected IllegalStateException because the PropertySpec is already built
    }

    @Test(expected = IllegalStateException.class)
    public void testCannotSetDefaultAfterFinish () {
        String specName = "exampleStringProperty";
        String defaultValue = "DEFAULT";
        PropertySpecBuilderImpl<String> builder = new PropertySpecBuilderImpl<>(new StringFactory()).setNameAndDescription(NameAndDescription.stringBased(specName, specName, null));
        builder.finish();

        // Business methods
        builder.setDefaultValue(defaultValue);

        // Expected IllegalStateException because the PropertySpec is already built
    }

    @Test(expected = IllegalStateException.class)
    public void testCannotAddValuesAfterFinish () {
        String specName = "exampleStringProperty";
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";
        PropertySpecBuilderImpl<String> builder = new PropertySpecBuilderImpl<>(new StringFactory()).setNameAndDescription(NameAndDescription.stringBased(specName, specName, null));
        builder.finish();

        // Business methods
        builder.addValues(otherValue1, otherValue2);

        // Expected IllegalStateException because the PropertySpec is already built
    }

    @Test(expected = IllegalStateException.class)
    public void testCannotMarkExhaustiveAfterFinish () {
        String specName = "exampleStringProperty";
        PropertySpecBuilderImpl<String> builder = new PropertySpecBuilderImpl<>(new StringFactory()).setNameAndDescription(NameAndDescription.stringBased(specName, specName, null));
        builder.finish();

        // Business methods
        builder.markExhaustive();

        // Expected IllegalStateException because the PropertySpec is already built
    }

}