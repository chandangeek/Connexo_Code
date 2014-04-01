package com.energyict.mdc.device.data;

/**
 * Copyrights EnergyICT
 * Date: 3/13/14
 * Time: 11:41 AM
 */
public interface DeviceProtocolProperty {

    /**
     * Gets the name of this PropertySpec.
     *
     * @return The name
     */
    public String getName();

    /**
     * Represents the stringValue of the Property
     *
     * @return the string value of the Property
     */
    public String getPropertyValue();

    public void setValue(String value);

    public void update();

}
