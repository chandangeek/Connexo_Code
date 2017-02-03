package com.energyict.mdc.upl.messages.legacy;

import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.Register;
import com.energyict.obis.ObisCode;

import java.util.Optional;

/**
 * Extracts information that pertains to {@link Device}s
 * from message related objects for the purpose
 * of formatting it as an old-style device message.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-30 (14:56)
 */
public interface DeviceExtractor {
    /**
     * Extracts the serial number from the {@link Device}.
     *
     * @param device The Device
     * @return The serial number
     */
    String serialNumber(Device device);

    String getDeviceProtocolPluggableClass(Device device);

    /**
     * Gets the value of the protocol property with the specified name.
     *
     * @param propertyName The name of the property
     * @param defaultValue The default value that will be returned in no value is specified on the Device
     * @param <T> The type of the property
     * @return The value of the property
     */
    <T> T protocolProperty(Device device, String propertyName, T defaultValue);

    /**
     * Extracts the {@link com.energyict.mdc.upl.meterdata.Register} with the specified {@link ObisCode}
     * from the {@link Device}.
     *
     * @param device The Device
     * @param obisCode The ObisCode
     * @return The Register or an empty Optional if the Device does not contain a register for the ObisCode
     */
    Optional<Register> register(Device device, ObisCode obisCode);
}