package com.energyict.mdc.protocol.api.device.offline;

import com.elster.jupiter.properties.PropertySpec;

/**
 * Represents an Offline version of a DeviceMessageAttribute.
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/02/13
 * Time: 16:34
 */
public interface OfflineDeviceMessageAttribute {

    /**
     * The PropertySpec which models the DeviceMessageAttribute.
     *
     * @return the propertySpec of the DeviceMessageAttribute
     */
    PropertySpec getPropertySpec();

    /**
     * The name of this DeviceMessageAttribute.
     *
     * @return the name of the DeviceMessageAttribute
     */
    String getName();

    /**
     * The related object/value of the DeviceMessageAttribute.
     *
     * @return this will contain the information to send or the action to perform on the Device
     */
    String getDeviceMessageAttributeValue();

}