package com.energyict.mdc.protocol.api.device.offline;

import com.elster.jupiter.properties.PropertySpec;

/**
 * Represents an Offline version of a DeviceMessageAttribute.
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/02/13
 * Time: 16:34
 */
public interface OfflineDeviceMessageAttribute extends com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute {

    /**
     * The PropertySpec which models the DeviceMessageAttribute.
     *
     * @return the propertySpec of the DeviceMessageAttribute
     */
    PropertySpec getPropertySpec();

}