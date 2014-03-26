package com.energyict.mdc.device.data;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.data.impl.InfoType;
import com.energyict.mdc.protocol.api.device.BaseDevice;

import java.util.List;
import java.util.TimeZone;

/**
 * Provides services that relate to:
 * <ul>
 *     <li>{@link com.energyict.mdc.protocol.api.device.BaseDevice Devices}</li>
 *     <li>{@link com.energyict.mdc.protocol.api.device.BaseChannel Channels}</li>
 *     <li>{@link com.energyict.mdc.protocol.api.device.BaseRegister Registers}</li>
 *     <li>{@link com.energyict.mdc.protocol.api.device.BaseLoadProfile LoadProfiles}</li>
 *     <li>{@link com.energyict.mdc.protocol.api.device.BaseLogBook LogBooks}</li>
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

    public List<BaseDevice<Channel, LoadProfile, Register>> findPhysicalConnectedDevicesFor(Device device);

    public List<BaseDevice<Channel, LoadProfile, Register>> findCommunicationReferencingDevicesFor(Device device);

    public LoadProfile findLoadProfileById(long id);

    public List<Device> findDevicesBySerialNumber(String serialNumber);

    public List<Device> findAllDevices();

    public List<Device> findDevicesByTimeZone(TimeZone timeZone);

    public InfoType newInfoType(String name);

    public InfoType findInfoType(String name);

    public InfoType findInfoTypeById(long infoTypeId);

    public LogBook findLogBookById(long id);

    public List<LogBook> findLogBooksByDevice(Device device);
}
