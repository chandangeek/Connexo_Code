package com.energyict.mdc.protocol.api;

/**
 * Used in the collected DeviceTopology to indicate when a readout slave device was last seen by the gateway/DC.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 5/04/2016 - 11:17
 */
public class LastSeenDateInfo {

    /**
     * The name of the LastSeenDate general property
     */
    private String propertyName;

    /**
     * The value of the LastSeenDate general property
     */
    private Object propertyValue;

    public LastSeenDateInfo(String propertyName, Object propertyValue) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Object getPropertyValue() {
        return propertyValue;
    }
}