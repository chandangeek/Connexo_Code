package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;

/**
 * Copyrights EnergyICT
 * Date: 3/13/14
 * Time: 11:41 AM
 */
@ProviderType
public interface DeviceProtocolProperty {

    /**
     * Gets the name of this PropertySpec.
     *
     * @return The name
     */
    String getName();

    /**
     * Represents the stringValue of the Property
     *
     * @return the string value of the Property
     */
    String getPropertyValue();

    void setValue(String value);

    void update();

}