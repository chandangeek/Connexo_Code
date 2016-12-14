package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

/**
 * Models configuration information that was collected from a device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (15:43)
 */
public interface CollectedConfigurationInformation extends CollectedData {

    /**
     * Gets the {@link DeviceIdentifier} that uniquely identifies
     * the device for which configuration information was collected.
     *
     * @return The DeviceIdentifier
     */
    DeviceIdentifier getDeviceIdentifier();

    /**
     * Gets the extension of the file that should be used by preference
     * as it is an indication of the type of information.
     *
     * @return The file extension
     */
    String getFileExtension();

    /**
     * Gets the raw bytes that constitute the configuration information.
     *
     * @return The raw bytes
     */
    byte[] getContents();

}