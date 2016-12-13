package com.energyict.mdc.upl.messages;

/**
 * Models an attribute of a {@link DeviceMessage}.
 */
public interface DeviceMessageAttribute {

    /**
     * Gets the name of this attribute
     */
    String getName();

    /**
     * Gets the value of this attribute, which is compatible
     * with the ValueFactory ValueDomain
     * of the PropertySpec specification.
     * It is the responsibility of the caller to know
     * the ValueDomain and to cast the value to the class
     * or interface that is compatible with that ValueDomain.
     *
     * @return The value of this attribute
     */
    Object getValue();

}