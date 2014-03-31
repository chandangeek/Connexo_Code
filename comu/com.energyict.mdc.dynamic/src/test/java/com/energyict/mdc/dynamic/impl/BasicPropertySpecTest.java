package com.energyict.mdc.dynamic.impl;

import com.energyict.mdc.common.InvalidValueException;
import com.energyict.mdc.common.ValueRequiredException;
import com.energyict.mdc.dynamic.OptionalPropertySpecFactory;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (11:53)
 */
public class BasicPropertySpecTest extends AbstractPropertySpecTest {

    @Test
    public void testStringIsNotAReference () {
        PropertySpec propertySpec = RequiredPropertySpecFactory.newInstance().stringPropertySpec("String is not a reference");

        // Business method
        assertThat(propertySpec.isReference()).isFalse();
    }

    @Test
    public void testBusinessObjectIsAReference () {
        PropertySpec propertySpec = this.newTestBusinessObjectPropertySpec(false);

        // Business method
        assertThat(propertySpec.isReference()).isTrue();
    }

    @Test
    public void testValidStringValue () throws InvalidValueException {
        PropertySpec<String> propertySpec = RequiredPropertySpecFactory.newInstance().stringPropertySpec("Valid String value");

        // Business method
        assertThat(propertySpec.validateValue("a String")).isTrue();
    }

    @Test
    public void testValidTestBusinessObjectValue () throws InvalidValueException {
        PropertySpec<TestBusinessObject> propertySpec = this.newTestBusinessObjectPropertySpec(false);
        TestBusinessObject testBusinessObject = this.newPersistentTestBusinessObject();

        // Business method
        assertThat(propertySpec.validateValue(testBusinessObject)).isTrue();
    }

    @Test(expected = ValueRequiredException.class)
    public void testNullIsNotValidForRequiredStringAttribute () throws InvalidValueException {
        PropertySpec<String> propertySpec = RequiredPropertySpecFactory.newInstance().stringPropertySpec("Null is invalid for required String");

        // Business method
        propertySpec.validateValue(null);    // expecting this to throw a ValueRequiredException
    }

    @Test(expected = ValueRequiredException.class)
    public void testNullIsNotValidForRequiredTestBusinessObjectAttribute () throws InvalidValueException {
        PropertySpec<TestBusinessObject> propertySpec = this.newTestBusinessObjectPropertySpec(true);

        // Business method
        propertySpec.validateValue(null);    // expecting this to throw a ValueRequiredException
    }

    @Test(expected = InvalidValueException.class)
    public void testStringIsNotValidForRequiredTestBusinessObjectAttribute () throws InvalidValueException {
        PropertySpec propertySpec = this.newTestBusinessObjectPropertySpec(true);

        // Business method
        propertySpec.validateValue("a String");    // expecting this to throw a InvalidValueException
    }

    @Test
    public void testNullIsValidForOptionalStringAttribute () throws InvalidValueException {
        PropertySpec propertySpec = OptionalPropertySpecFactory.newInstance().stringPropertySpec("Null is valid for optional String");

        // Business method
        assertThat(propertySpec.validateValue(null)).isTrue();
    }

    @Test(expected = InvalidValueException.class)
    public void testTestBusinessObjectIsNotValidForStringAttribute () throws InvalidValueException {
        PropertySpec propertySpec = RequiredPropertySpecFactory.newInstance().stringPropertySpec("TestBusinessObject is not valid for String");
        TestBusinessObject testbusinessobject = mock(TestBusinessObject.class);

        // Business method
        propertySpec.validateValue(testbusinessobject); // expecting an InvalidValueException
    }

    @Test
    public void testNullIsValidForOptionalTestBusinessObjectAttribute () throws InvalidValueException {
        PropertySpec<TestBusinessObject> propertySpec = this.newTestBusinessObjectPropertySpec(false);

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

    private BasicPropertySpec<TestBusinessObject> newTestBusinessObjectPropertySpec (boolean required) {
        return new ReferencePropertySpec<>("SomeBusinessObject", required, new TestBusinessObjectFactoryImpl());
    }


}