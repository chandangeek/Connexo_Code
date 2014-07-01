package com.elster.jupiter.properties;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.OptionalPropertySpecFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.RequiredPropertySpecFactory;
import com.elster.jupiter.properties.ValueRequiredException;

@RunWith(MockitoJUnitRunner.class)
public class BasicPropertySpecTest {

    @Test
    public void testStringIsNotAReference () {
        PropertySpec propertySpec = RequiredPropertySpecFactory.newInstance().stringPropertySpec("String is not a reference");

        // Business method
        assertThat(propertySpec.isReference()).isFalse();
    }

    @Test
    public void testValidStringValue () throws InvalidValueException {
        PropertySpec<String> propertySpec = RequiredPropertySpecFactory.newInstance().stringPropertySpec("Valid String value");

        // Business method
        assertThat(propertySpec.validateValue("a String")).isTrue();
    }

    @Test(expected = ValueRequiredException.class)
    public void testNullIsNotValidForRequiredStringAttribute () throws InvalidValueException {
        PropertySpec<String> propertySpec = RequiredPropertySpecFactory.newInstance().stringPropertySpec("Null is invalid for required String");

        // Business method
        propertySpec.validateValue(null);    // expecting this to throw a ValueRequiredException
    }

    @Test
    public void testNullIsValidForOptionalStringAttribute () throws InvalidValueException {
        PropertySpec propertySpec = OptionalPropertySpecFactory.newInstance().stringPropertySpec("Null is valid for optional String");

        // Business method
        assertThat(propertySpec.validateValue(null)).isTrue();
    }

    @Test
    public void testEqualsToSameObject () {
        PropertySpec propertySpec = RequiredPropertySpecFactory.newInstance().stringPropertySpec("Equality for same object");
        assertThat(propertySpec.equals(propertySpec)).isTrue();
    }

    @Test
    public void testEqualsToSimilarObject () {
        String name = "Same name yiels true in equals method";
        PropertySpec propertySpec1 = RequiredPropertySpecFactory.newInstance().stringPropertySpec(name);
        PropertySpec propertySpec2 = RequiredPropertySpecFactory.newInstance().stringPropertySpec(name);
        assertThat(propertySpec1.equals(propertySpec2)).isTrue();
    }

    @Test
    public void testEqualsIsCommutative () {
        String name = "a.equals.b also b.equals.a";
        PropertySpec propertySpec1 = RequiredPropertySpecFactory.newInstance().stringPropertySpec(name);
        PropertySpec propertySpec2 = RequiredPropertySpecFactory.newInstance().stringPropertySpec(name);
        assertThat(propertySpec1.equals(propertySpec2)).isTrue();
        assertThat(propertySpec2.equals(propertySpec1)).isTrue();
    }

    @Test
    public void testEqualsIsTransitive () {
        String name = "a.equals.b and b.equals.c equals a.equals.c";
        PropertySpec propertySpec1 = RequiredPropertySpecFactory.newInstance().stringPropertySpec(name);
        PropertySpec propertySpec2 = RequiredPropertySpecFactory.newInstance().stringPropertySpec(name);
        PropertySpec propertySpec3 = RequiredPropertySpecFactory.newInstance().stringPropertySpec(name);
        assertThat(propertySpec1.equals(propertySpec2)).isTrue();
        assertThat(propertySpec2.equals(propertySpec3)).isTrue();
        assertThat(propertySpec1.equals(propertySpec3)).isTrue();
    }

    @Test
    public void testEqualsReturnsSameHashCode () {
        String name = "a.equals.b yiels the same hashCode";
        PropertySpec propertySpec1 = RequiredPropertySpecFactory.newInstance().stringPropertySpec(name);
        PropertySpec propertySpec2 = RequiredPropertySpecFactory.newInstance().stringPropertySpec(name);
        int hashCode1 = propertySpec1.hashCode();
        int hashCode2 = propertySpec2.hashCode();
        assertThat(hashCode1).as("Equal objects should have the same hashCode").isEqualTo(hashCode2);
    }

    @Test
    public void testNotEqualsToString () {
        String name = "BasicPropertySpec is never equals to a String";
        PropertySpec propertySpec = RequiredPropertySpecFactory.newInstance().stringPropertySpec(name);
        assertThat(propertySpec.equals(name)).isFalse();
    }

    @Test
    public void testNotEqualsToNull () {
        String name = "BasicPropertySpec is never equals to null value";
        PropertySpec propertySpec = RequiredPropertySpecFactory.newInstance().stringPropertySpec(name);
        assertThat(propertySpec.equals(null)).isFalse();
    }
}