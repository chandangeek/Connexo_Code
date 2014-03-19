package com.energyict.mdc.dynamic.impl;

import com.energyict.mdc.common.InvalidValueException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link ReferencePropertySpec} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-03 (11:42)
 */
@RunWith(MockitoJUnitRunner.class)
public class ReferencePropertySpecTest extends AbstractPropertySpecTest {

    @Test
    public void testIsAReference () {
        ReferencePropertySpec propertySpec = this.newReferencePropertySpec(true);

        // Business Method and assert
        assertThat(propertySpec.isReference()).isTrue();
    }

    @Test
    public void testNullIsValidWhenOptional () throws InvalidValueException {
        ReferencePropertySpec<TestBusinessObject> propertySpec = this.newReferencePropertySpec(false);

        // Business Method and assert
        assertThat(propertySpec.validateValue(null)).isTrue();
    }

    @Test(expected = InvalidValueException.class)
    public void testNullIsNotValidWhenRequired () throws InvalidValueException {
        ReferencePropertySpec<TestBusinessObject> propertySpec = this.newReferencePropertySpec(true);

        // Business Method and assert
        assertThat(propertySpec.validateValue(null)).isTrue();
    }

    @Test
    public void testTestBusinessObjectIsValidRequiredValue () throws InvalidValueException {
        ReferencePropertySpec<TestBusinessObject> propertySpec = this.newReferencePropertySpec(true);
        TestBusinessObject value = this.newPersistentTestBusinessObject();

        // Business method
        assertThat(propertySpec.validateValue(value)).isTrue();
    }

    @Test
    public void testTestBusinessObjectIsValidOptionalValue () throws InvalidValueException {
        ReferencePropertySpec<TestBusinessObject> propertySpec = this.newReferencePropertySpec(false);
        TestBusinessObject value = this.newPersistentTestBusinessObject();

        // Business method
        assertThat(propertySpec.validateValue(value)).isTrue();
    }

    @Test(expected = InvalidValueException.class)
    public void testStringIsNotValidRequiredValue () throws InvalidValueException {
        ReferencePropertySpec propertySpec = this.newReferencePropertySpec(true);
        String string = "a String";

        // Business method
        propertySpec.validateValue(string); // expecting an InvalidValueException
    }

    @Test(expected = InvalidValueException.class)
    public void testStringIsNotValidOptionalValue () throws InvalidValueException {
        ReferencePropertySpec propertySpec = this.newReferencePropertySpec(false);
        String string = "a String";

        // Business method
        propertySpec.validateValue(string); // expecting an InvalidValueException
    }

    private ReferencePropertySpec<TestBusinessObject> newReferencePropertySpec (boolean required) {
        return new ReferencePropertySpec<>("SomeBusinessObject", required, new TestBusinessObjectFactoryImpl());
    }

}