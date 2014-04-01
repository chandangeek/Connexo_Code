package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.DeviceProtocol;

/**
 * Models the exceptional situation that occurs when an attempt is made to try
 * create a DeviceProtocolProperty but something is wrong ...
 * <p/>
 * Copyrights EnergyICT
 * Date: 24/03/14
 * Time: 14:08
 */
public class DeviceProtocolPropertyException extends LocalizedException {

    private DeviceProtocolPropertyException(Thesaurus thesaurus, MessageSeed messageSeed, Object... arguments) {
        super(thesaurus, messageSeed, arguments);
    }

    /**
     * Models the situation that occurs when an attempt is made to create a DeviceProtocolProperty,
     * but the 'linked' infotype does not exist.
     *
     * @param thesaurus     the used thesaurus
     * @param propertyValue the value of the property
     * @return the newly created DeviceProtocolException
     */
    public static DeviceProtocolPropertyException infoTypeDoesNotExist(Thesaurus thesaurus, String propertyValue) {
        return new DeviceProtocolPropertyException(thesaurus, MessageSeeds.DEVICE_PROPERTY_INFO_TYPE_DOENST_EXIST, propertyValue);
    }

    /**
     * Models the exceptional situation that occurs when an attempt is made to add a DeviceProperty
     * to a Device, which is NOT defined by the DeviceProtocol
     *
     * @param thesaurus      the used thesaurus
     * @param name           the name of the property
     * @param deviceProtocol the DeviceProtocol of the Device
     * @param device         the device which doesn't have the property
     * @return the newly created DeviceProtocolException
     */
    public static DeviceProtocolPropertyException propertyDoesNotExistForDeviceProtocol(Thesaurus thesaurus, String name, DeviceProtocol deviceProtocol, Device device) {
        return new DeviceProtocolPropertyException(thesaurus, MessageSeeds.DEVICE_PROPERTY_NOT_ON_DEVICE_PROTOCOL, name, deviceProtocol.getClass().getSimpleName(), device.getName());
    }
}
