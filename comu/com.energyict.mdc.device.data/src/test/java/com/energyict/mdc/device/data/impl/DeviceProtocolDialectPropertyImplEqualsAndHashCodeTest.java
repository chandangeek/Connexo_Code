package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.pluggable.PluggableClass;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the equals and hashCode methods of the {@link DeviceProtocolDialectPropertyImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-21 (11:37)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceProtocolDialectPropertyImplEqualsAndHashCodeTest {

    private static final String STRING_VALUE = "DeviceProtocolDialectPropertyImplEqualsAndHashCodeTest";
    private static final String PROPERTY_NAME = "property";
    private static final String PROPERTY_VALUE = "value";
    private static final Interval ACTIVE_PERIOD = new Interval(null, new Date());

    @Mock
    private PluggableClass pluggableClass;

    @Test
    public void testNotEqualsToString () {
        String propertyName = STRING_VALUE;
        DeviceProtocolDialectPropertyImpl property = new DeviceProtocolDialectPropertyImpl(propertyName);
        assertThat(property.equals(STRING_VALUE)).isFalse();
    }

    @Test
    public void testNotEqualsToNull () {
        DeviceProtocolDialectPropertyImpl property = new DeviceProtocolDialectPropertyImpl(PROPERTY_NAME);
        assertThat(property.equals(null)).isFalse();
    }

    @Test
    public void testEqualsToSameMemoryObject () {
        DeviceProtocolDialectPropertyImpl property = new DeviceProtocolDialectPropertyImpl(PROPERTY_NAME);
        assertThat(property.equals(property)).isTrue();
    }

    @Test
    public void testEqualsToPropertyWithTheSameName () {
        DeviceProtocolDialectPropertyImpl property = new DeviceProtocolDialectPropertyImpl(PROPERTY_NAME);
        DeviceProtocolDialectPropertyImpl propertyWithTheSameName = new DeviceProtocolDialectPropertyImpl(PROPERTY_NAME);
        assertThat(property.equals(propertyWithTheSameName)).isTrue();
    }

    @Test
    public void testEqualsToIdenticalWithAllProperties () {
        DeviceProtocolDialectPropertyImpl property = new DeviceProtocolDialectPropertyImpl(PROPERTY_NAME, PROPERTY_VALUE, ACTIVE_PERIOD, this.pluggableClass, true);
        DeviceProtocolDialectPropertyImpl identical = new DeviceProtocolDialectPropertyImpl(PROPERTY_NAME, PROPERTY_VALUE, ACTIVE_PERIOD, this.pluggableClass, true);
        assertThat(property.equals(identical)).isTrue();
    }

    @Test
    public void testEqualsIsCommutative () {
        DeviceProtocolDialectPropertyImpl property = new DeviceProtocolDialectPropertyImpl(PROPERTY_NAME, PROPERTY_VALUE, ACTIVE_PERIOD, this.pluggableClass, true);
        DeviceProtocolDialectPropertyImpl identical = new DeviceProtocolDialectPropertyImpl(PROPERTY_NAME, PROPERTY_VALUE, ACTIVE_PERIOD, this.pluggableClass, true);
        assertThat(property.equals(identical)).isTrue();
        assertThat(identical.equals(property)).isTrue();
    }

    @Test
    public void testEqualsIsTransitive () {
        DeviceProtocolDialectPropertyImpl property1 = new DeviceProtocolDialectPropertyImpl(PROPERTY_NAME, PROPERTY_VALUE, ACTIVE_PERIOD, this.pluggableClass, true);
        DeviceProtocolDialectPropertyImpl property2 = new DeviceProtocolDialectPropertyImpl(PROPERTY_NAME, PROPERTY_VALUE, ACTIVE_PERIOD, this.pluggableClass, true);
        DeviceProtocolDialectPropertyImpl property3 = new DeviceProtocolDialectPropertyImpl(PROPERTY_NAME, PROPERTY_VALUE, ACTIVE_PERIOD, this.pluggableClass, true);
        assertThat(property1.equals(property2)).isTrue();
        assertThat(property2.equals(property3)).isTrue();
        assertThat(property1.equals(property3)).isTrue();
    }

    @Test
    public void testEqualObjectsHaveTheSameHashCode () {
        DeviceProtocolDialectPropertyImpl property = new DeviceProtocolDialectPropertyImpl(PROPERTY_NAME, PROPERTY_VALUE, ACTIVE_PERIOD, this.pluggableClass, true);
        DeviceProtocolDialectPropertyImpl identical = new DeviceProtocolDialectPropertyImpl(PROPERTY_NAME, PROPERTY_VALUE, ACTIVE_PERIOD, this.pluggableClass, true);
        assertThat(property.hashCode() == identical.hashCode()).isTrue();
    }

}