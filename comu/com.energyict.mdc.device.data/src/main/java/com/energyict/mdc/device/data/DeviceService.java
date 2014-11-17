package com.energyict.mdc.device.data;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.Range;

import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.impl.InfoType;
import com.energyict.mdc.scheduling.model.ComSchedule;

import java.time.Instant;
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
     * @param mRID                The new Device's master resource identifier
     * @return the newly created Device
     */
    public Device newDevice(DeviceConfiguration deviceConfiguration, String name, String mRID);

    public Device getPrototypeDeviceFor(DeviceConfiguration deviceConfiguration);

    /**
     * Finds the Device based on his unique ID.
     *
     * @param id the unique ID of the device
     * @return the requested Device or null if none was found
     */
    public Device findDeviceById(long id);

    /**
     * Finds the Device based on his unique External name.
     *
     * @param mrId the unique Identifier of the device
     * @return the requested Device or null if none was found
     */
    public Device findByUniqueMrid(String mrId);

    /**
     * Finds the devices which are physically connected to the given Device.
     *
     * @param device the 'master' device
     * @return a list of physically connected devices to the given device
     */
    public List<Device> findPhysicalConnectedDevicesFor(Device device);

    /**
     * Gets the {@link TopologyTimeline} of the devices that are
     * directly physically connected to the specified gateway.
     *
     * @param device The gateway
     * @return The TopologyTimeline
     * @see Device#getPhysicalGateway()
     */
    public TopologyTimeline getPysicalTopologyTimeline(Device device);

    /**
     * Gets the most recent additions to the {@link TopologyTimeline}
     * of the devices that are directly physically connected to the specified gateway.
     *
     * @param device The gateway
     * @param maximumNumberOfAdditions The maximum number of additions to the timeline
     * @return The TopologyTimeline
     * @see Device#getPhysicalGateway()
     */
    public TopologyTimeline getPhysicalTopologyTimelineAdditions(Device device, int maximumNumberOfAdditions);

    /**
     * Gets the {@link TopologyTimeline} of the devices that directly
     * use the specified device as the communication gateway.
     *
     * @param device The communication gateway
     * @return The TopologyTimeline
     * @see Device#getCommunicationGateway()
     */
    public TopologyTimeline getCommunicationTopologyTimeline(Device device);

    /**
     * Gets the most recent additions to the {@link TopologyTimeline}
     * of the devices that directly use the specified device as the communication gateway.
     *
     * @param device The communication gateway
     * @param maximumNumberOfAdditions The maximum number of additions to the timeline
     * @return The TopologyTimeline
     * @see Device#getCommunicationGateway()
     */
    public TopologyTimeline getCommunicationTopologyTimelineAdditions(Device device, int maximumNumberOfAdditions);

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
     * @param device    the device that arranges the communication
     * @param timestamp The timestamp on which the devices are linked for communication purposes
     * @return a list of devices which use the given device for communication purposes
     */
    public List<Device> findCommunicationReferencingDevicesFor(Device device, Instant timestamp);

    /**
     * Builds the {@link DeviceTopology communication topology}
     * for the specified Device and Interval.
     *
     * @param device the Device that arranges the communication
     * @param period The period in time during which the devices are linked for communication purposes
     * @return a list of devices which use the given device for communication purposes
     */
    public DeviceTopology buildCommunicationTopology(Device device, Range<Instant> period);

    /**
     * Builds the {@link DeviceTopology physical topology}
     * for the specified Device and Interval.
     *
     * @param device the Device
     * @param period The period in time during which the devices are physically linked
     * @return a list of devices which are all physically linked to the Device
     */
    public DeviceTopology buildPhysicalTopology(Device device, Range<Instant> period);

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
     * Returns true if the ComSchedule has been linked to a device.
     */
    public boolean isLinkedToDevices(ComSchedule comSchedule);

    public Finder<Device> findDevicesByDeviceConfiguration(DeviceConfiguration deviceConfiguration);

}