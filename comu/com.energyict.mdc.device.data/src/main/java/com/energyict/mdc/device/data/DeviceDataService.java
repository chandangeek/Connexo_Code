package com.energyict.mdc.device.data;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.protocol.api.device.BaseDevice;

import java.util.List;

/**
 * Provides services that relate to:
 * <ul>
 *     <li>{@link com.energyict.mdc.protocol.api.device.BaseDevice Devices}</li>
 *     <li>{@link com.energyict.mdc.protocol.api.device.BaseChannel Channels}</li>
 *     <li>{@link com.energyict.mdc.protocol.api.device.BaseRegister Registers}</li>
 *     <li>{@link com.energyict.mdc.protocol.api.device.LoadProfile LoadProfiles}</li>
 *     <li>{@link com.energyict.mdc.protocol.api.device.LogBook LogBooks}</li>
 * </ul>
 *
 * Copyrights EnergyICT
 * Date: 26/02/14
 * Time: 10:40
 */
public interface DeviceDataService {

    public static String COMPONENTNAME = "DDC";

    public Device newDevice(DeviceConfiguration deviceConfiguration, String name);

    public Device getPrototypeDeviceFor(DeviceConfiguration deviceConfiguration);

    public Device findDeviceById(long id);

    public Device findDeviceByExternalName(String externalName);

    public boolean deviceHasLogBookForLogBookSpec(Device device, LogBookSpec logBookSpec);

    public List<BaseDevice> findPhysicalConnectedDevicesFor(Device device);

    public List<BaseDevice> findCommunicationReferencingDevicesFor(Device device);
}
