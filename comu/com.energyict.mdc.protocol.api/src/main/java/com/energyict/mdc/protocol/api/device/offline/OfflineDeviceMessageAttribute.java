package com.energyict.mdc.protocol.api.device.offline;

import com.energyict.mdc.protocol.api.dynamic.PropertySpec;

/**
 * Represents an Offline version of a DeviceMessageAttribute.
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/02/13
 * Time: 16:34
 */
public interface OfflineDeviceMessageAttribute {

    /**
     * The PropertySpec which models the DeviceMessageAttribute
     *
     * @return the propertySpec of the DeviceMessageAttribute
     */
    public PropertySpec getPropertySpec();

    /**
     * The name of this DeviceMessageAttribute
     *
     * @return the name of the DeviceMessageAttribute
     */
    public String getName();

    /**
     * The related object/value of the DeviceMessageAttribute
     *
     * @return this will contain the information to send or the action to perform on the Device
     */
    public String getDeviceMessageAttributeValue();

    /**
     * The OfflineDeviceMessage which owns this DeviceMessageAttribute
     *
     * @return the OfflineDeviceMessage which owns this DeviceMessageAttribute
     */
    public OfflineDeviceMessage getDeviceMessage();

}
