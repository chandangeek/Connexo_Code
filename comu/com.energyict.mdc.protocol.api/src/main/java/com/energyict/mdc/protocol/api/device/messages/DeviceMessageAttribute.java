package com.energyict.mdc.protocol.api.device.messages;

import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueFactory;

/**
 * Models an attribute of a {@link DeviceMessage}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (17:14)
 */
public interface DeviceMessageAttribute {

    /**
     * Gets the {@link PropertySpec specification}
     * of this attribute.
     *
     * @return The DeviceMessageAttributeSpec
     */
    public PropertySpec getSpecification ();

    /**
     * Gets the owning {@link DeviceMessage}.
     *
     * @return The DeviceMessage
     */
    public DeviceMessage getMessage ();

    /**
     * Gets the name of this attribute, which is copied from
     * the {@link PropertySpec specification}.
     *
     * @return The name of this attribute
     * @see PropertySpec#getName()
     */
    public String getName ();

    /**
     * Tests if this attribute is required according to its specification.
     *
     * @return should always return true
     */
    public boolean isRequired ();

    /**
     * Gets the value of this attribute, which is compatible
     * with the {@link ValueFactory}
     * and the {@link ValueDomain}
     * of the {@link PropertySpec specification}.
     * It is the responsibility of the caller to know
     * the ValueDomain and to cast the value to the class
     * or interface that is compatible with that ValueDomain.
     *
     * @return The value of this attribute
     */
    public Object getValue ();

}