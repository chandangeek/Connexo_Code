package com.energyict.mdc.dynamic.impl;

import com.energyict.mdc.common.ApplicationContext;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.StringFactory;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link PropertySpecBuilderImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-20 (09:47)
 */
@RunWith(MockitoJUnitRunner.class)
public class PropertySpecBuilderTest {

    @Mock
    private ApplicationContext applicationContext;

    @Test
    public void testSimpleStringProperty () {
        String specName = "exampleStringProperty";

        // Business methods
        PropertySpec<String> propertySpec =
                PropertySpecBuilderImpl.
                        forClass(new StringFactory()).
                        name(specName).
                        finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(specName);
        assertThat(propertySpec.getPossibleValues()).isNull();
    }

    @Test
    public void testStringPropertyWithDefaultAndNoOtherValues () {
        String specName = "exampleStringProperty";
        String defaultValue = "DEFAULT";

        // Business methods
        PropertySpec<String> propertySpec =
                PropertySpecBuilderImpl.
                        forClass(new StringFactory()).
                        name(specName).
                        setDefaultValue(defaultValue).
                        finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(specName);
        PropertySpecPossibleValues<String> possibleValues = propertySpec.getPossibleValues();
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
        PropertySpecBuilder<String> builder = PropertySpecBuilderImpl.forClass(new StringFactory());
        builder.
                name(specName).
                setDefaultValue(initialDefaultValue);

        // Business methods
        builder.setDefaultValue(newDefaultValue);
        PropertySpec<String> propertySpec = builder.finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(specName);
        PropertySpecPossibleValues<String> possibleValues = propertySpec.getPossibleValues();
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
        PropertySpec<String> propertySpec =
                PropertySpecBuilderImpl.
                        forClass(new StringFactory()).
                        name(specName).
                        setDefaultValue(defaultValue).
                        addValues(otherValue1, otherValue2).
                        finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(specName);
        PropertySpecPossibleValues<String> possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.getDefault()).isEqualTo(defaultValue);
        assertThat(possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.isExhaustive()).isFalse();
    }

    @Test
    public void testStringPropertyWithValuesButNoDefault () {
        String specName = "exampleStringProperty";
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";

        // Business methods
        PropertySpec<String> propertySpec =
                PropertySpecBuilderImpl.
                        forClass(new StringFactory()).
                        name(specName).
                        addValues(otherValue1, otherValue2).
                        finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(specName);
        PropertySpecPossibleValues<String> possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.getDefault()).isNull();
        assertThat(possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.isExhaustive()).isFalse();
    }

    @Test
    public void testAddStringPropertyValuesInSeparateCalls () {
        String specName = "exampleStringProperty";
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";

        // Business methods
        PropertySpec<String> propertySpec =
                PropertySpecBuilderImpl.
                        forClass(new StringFactory()).
                        name(specName).
                        addValues(otherValue1).
                        addValues(otherValue2).
                        finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(specName);
        PropertySpecPossibleValues<String> possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.getDefault()).isNull();
        assertThat(possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.isExhaustive()).isFalse();
    }

    @Test
    public void testStringPropertyWithExhaustiveValues () {
        String specName = "exampleStringProperty";
        String defaultValue = "DEFAULT";
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";

        // Business methods
        PropertySpec<String> propertySpec =
                PropertySpecBuilderImpl.
                        forClass(new StringFactory()).
                        name(specName).
                        setDefaultValue(defaultValue).
                        addValues(otherValue1, otherValue2).
                        markExhaustive().
                        finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(specName);
        PropertySpecPossibleValues<String> possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.getDefault()).isEqualTo(defaultValue);
        assertThat(possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.isExhaustive()).isTrue();
    }

    @Test
    public void testStringPropertyWithExhaustiveMarkerBeforeAddingValues () {
        String specName = "exampleStringProperty";
        String defaultValue = "DEFAULT";
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";

        // Business methods
        PropertySpec<String> propertySpec =
                PropertySpecBuilderImpl.
                        forClass(new StringFactory()).
                        name(specName).
                        markExhaustive().
                        setDefaultValue(defaultValue).
                        addValues(otherValue1, otherValue2).
                        finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
        assertThat(propertySpec.getName()).isEqualTo(specName);
        PropertySpecPossibleValues<String> possibleValues = propertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.getDefault()).isEqualTo(defaultValue);
        assertThat(possibleValues.getAllValues()).containsOnly(otherValue1, otherValue2);
        assertThat(possibleValues.isExhaustive()).isTrue();
    }

    @Test
    public void testFinishAgainReturnsTheSamePropertySpec () {
        String specName = "exampleStringProperty";
        PropertySpecBuilder<String> builder = PropertySpecBuilderImpl.forClass(new StringFactory());
        builder.name(specName);
        PropertySpec<String> propertySpec1 = builder.finish();

        // Business methods
        PropertySpec<String> propertySpec2 = builder.finish();

        // Asserts
        assertThat(propertySpec1).isSameAs(propertySpec2);
    }

    @Test(expected = IllegalStateException.class)
    public void testCannotSetNameAfterFinish () {
        String specName = "exampleStringProperty";
        PropertySpecBuilder<String> builder = PropertySpecBuilderImpl.forClass(new StringFactory());
        builder.finish();

        // Business methods
        builder.name(specName);

        // Expected IllegalStateException because the PropertySpec is already built
    }

    @Test(expected = IllegalStateException.class)
    public void testCannotSetDefaultAfterFinish () {
        String specName = "exampleStringProperty";
        String defaultValue = "DEFAULT";
        PropertySpecBuilder<String> builder = PropertySpecBuilderImpl.forClass(new StringFactory());
        builder.name(specName).finish();

        // Business methods
        builder.setDefaultValue(defaultValue);

        // Expected IllegalStateException because the PropertySpec is already built
    }

    @Test(expected = IllegalStateException.class)
    public void testCannotAddValuesAfterFinish () {
        String specName = "exampleStringProperty";
        String otherValue1 = "OTHER1";
        String otherValue2 = "OTHER2";
        PropertySpecBuilder<String> builder = PropertySpecBuilderImpl.forClass(new StringFactory());
        builder.name(specName).finish();

        // Business methods
        builder.addValues(otherValue1, otherValue2);

        // Expected IllegalStateException because the PropertySpec is already built
    }

    @Test(expected = IllegalStateException.class)
    public void testCannotMarkExhaustiveAfterFinish () {
        String specName = "exampleStringProperty";
        PropertySpecBuilder<String> builder = PropertySpecBuilderImpl.forClass(new StringFactory());
        builder.name(specName).finish();

        // Business methods
        builder.markExhaustive();

        // Expected IllegalStateException because the PropertySpec is already built
    }

}