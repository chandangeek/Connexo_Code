package com.energyict.mdc.device.data.exceptions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.DeviceProtocol;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

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
     * @param propertyValue the value of the property
     * @param thesaurus     the used thesaurus
     * @param messageSeed The MessageSeed
     * @return the newly created DeviceProtocolException
     */
    public static DeviceProtocolPropertyException propertySpecTypeDoesNotExist(String propertyValue, Thesaurus thesaurus, MessageSeed messageSeed) {
        return new DeviceProtocolPropertyException(thesaurus, messageSeed, propertyValue);
    }

    /**
     * Models the exceptional situation that occurs when an attempt is made to add a DeviceProperty
     * to a Device, which is NOT defined by the DeviceProtocol
     *
     * @param name           the name of the property
     * @param deviceProtocol the DeviceProtocol of the Device
     * @param device         the device which doesn't have the property
     * @param thesaurus      the used thesaurus
     * @param messageSeed The MessageSeed
     * @return the newly created DeviceProtocolException
     */
    public static DeviceProtocolPropertyException propertyDoesNotExistForDeviceProtocol(String name, DeviceProtocol deviceProtocol, Device device, Thesaurus thesaurus, MessageSeed messageSeed) {
        return new DeviceProtocolPropertyException(thesaurus, messageSeed, name, deviceProtocol.getClass().getSimpleName(), device.getName());
    }
}
