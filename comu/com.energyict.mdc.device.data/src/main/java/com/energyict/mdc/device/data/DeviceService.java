package com.energyict.mdc.device.data;

import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.impl.InfoType;
import com.energyict.mdc.scheduling.model.ComSchedule;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.Interval;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Provides services that relate to {@link Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (14:27)
 */
public interface DeviceService {

    /**
     * Tests if there are {@link Device} that were created
     * from the specified {@link DeviceConfiguration}.
     *
     * @param deviceConfiguration The DeviceConfiguration
     * @return <code>true</code> iff there is at least one Device created from the DeviceConfiguration
     */
    public boolean hasDevices(DeviceConfiguration deviceConfiguration);

    /**
     * Creates a new Device based on the given name and DeviceConfiguration
     *
     * @param deviceConfiguration the deviceConfiguration which models the device
     * @param name                the name which should be used for the device
     * @param mRID The new Device's master resource identifier
     * @return the newly created Device
     */
    public Device newDevice(DeviceConfiguration deviceConfiguration, String name, String mRID);

    public Device getPrototypeDeviceFor(DeviceConfiguration deviceConfiguration);

    /**
     * Finds the Device based on his unique ID
     *
     * @param id the unique ID of the device
     * @return the requested Device or null if none was found
     */
    public Device findDeviceById(long id);

    /**
     * Finds the Device based on his unique External name
     *
     * @param mrId the unique Identifier of the device
     * @return the requested Device or null if none was found
     */
    public Device findByUniqueMrid(String mrId);

    /**
     * Finds the devices which are physically connected to the given Device
     *
     * @param device the 'master' device
     * @return a list of physically connected devices to the given device
     */
    public List<Device> findPhysicalConnectedDevicesFor(Device device);

    /**
     * Finds the devices which are linked to the given device for communication purposes.
     *
     * @param device the device that arranges the communication
     * @return a list of devices which use the given device for communication purposes
     */
    public List<Device> findCommunicationReferencingDevicesFor(Device device);

    /**
     * Finds the devices which are linked on the specified timestamp
     * to the specified device for communication purposes.
     *
     * @param device the device that arranges the communication
     * @param timestamp The timestamp on which the devices are linked for communication purposes
     * @return a list of devices which use the given device for communication purposes
     */
    public List<Device> findCommunicationReferencingDevicesFor(Device device, Date timestamp);

    /**
     * Finds the devices which are linked on the specified timestamp
     * to the specified device for communication purposes.
     *
     * @param device the device that arranges the communication
     * @param interval The interval during which the devices are linked for communication purposes
     * @return a list of devices which use the given device for communication purposes
     */
    public List<CommunicationTopologyEntry> findCommunicationReferencingDevicesFor(Device device, Interval interval);

    /**
     * Finds the Devices (multiple are possible) based on the given serialNumber
     *
     * @param serialNumber the serialNumber of the device
     * @return a list of Devices which have the given serialNumber
     */
    public List<Device> findDevicesBySerialNumber(String serialNumber);

    /**
     * Finds all the devices in the system
     *
     * @return a list of all devices in the system
     */
    public List<Device> findAllDevices();

    /**
     * Finds all the devices in the system with the specific condition
     *
     * @return a list of all devices with the specific condition in the system
     */
    public Finder<Device> findAllDevices(Condition condition);

    /**
     * Finds all the devices which use the given TimeZone
     *
     * @param timeZone the timeZone
     * @return a list of Devices which use the given TimeZone
     */
    public List<Device> findDevicesByTimeZone(TimeZone timeZone);

    /**
     * Creates a new InfoType object based on the given name
     *
     * @param name the name for the InfoType object
     * @return the newly created infoType object
     */
    public InfoType newInfoType(String name);

    /**
     * Finds the infoType which has the given name
     *
     * @param name the name of the InfoType to find
     * @return the requested InfoType or null if none exists with that name
     */
    public InfoType findInfoType(String name);

    /**
     * Finds the infoType with the given unique ID
     *
     * @param infoTypeId the unique ID of the InfoType
     * @return the requested InfoType or null if none exists with that ID
     */
    public InfoType findInfoTypeById(long infoTypeId);

    /**
     * Returns true if the ComSchedule has been linked to a device.
     */
    public boolean isLinkedToDevices(ComSchedule comSchedule);

    public Finder<Device>  findDevicesByDeviceConfiguration(DeviceConfiguration deviceConfiguration);

}