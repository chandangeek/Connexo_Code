package com.energyict.mdc.device.config;

/**
 * Copyrights EnergyICT
 * Date: 10/02/14
 * Time: 09:25
 */
public interface DeviceCommunicationConfigurationFactory {

    /**
     * Finds the one and only {@link DeviceCommunicationConfiguration} that
     * relates to the specified {@link DeviceConfiguration}.
     *
     * @param deviceConfiguration The DeviceConfiguration
     * @return The DeviceCommunicationConfiguration or <code>null</code> if no such DeviceCommunicationConfiguration exists
     */
    public DeviceCommunicationConfiguration findFor (DeviceConfiguration deviceConfiguration);
}
