package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.scheduling.model.ComSchedule;

import java.util.List;
import java.util.Optional;

/**
 * Provides services that relate to {@link Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (14:27)
 */
@ProviderType
public interface DeviceService {

    /**
     * Creates a new Device based on the given name and DeviceConfiguration
     *
     * @param deviceConfiguration the deviceConfiguration which models the device
     * @param name                the name which should be used for the device
     * @param mRID                the new Device's master resource identifier
     * @return the newly created Device
     */
    public Device newDevice(DeviceConfiguration deviceConfiguration, String name, String mRID);

    /**
     * Creates a new Device based on the given name, DeviceConfiguration and batch.
     * If batch with specified name doesn't exist then new one will be created.
     *
     * @param deviceConfiguration the deviceConfiguration which models the device
     * @param name                the name which should be used for the device
     * @param mRID                the new Device's master resource identifier
     * @param batch               the name of batch
     * @return the newly created Device
     */
    public Device newDevice(DeviceConfiguration deviceConfiguration, String name, String mRID, String batch);

    /**
     * Finds the Device based on his unique ID.
     *
     * @param id the unique ID of the device
     * @return the requested Device or null if none was found
     */
    public Optional<Device> findDeviceById(long id);

    /**
     * Finds and locks the Device based on his unique ID and VERSION.
     *
     * @param id      the unique ID of the device
     * @param version the version of the device
     * @return the requested Device or null if none was found
     */
    public Optional<Device> findAndLockDeviceByIdAndVersion(long id, long version);

    public Optional<Device> findAndLockDeviceBymRIDAndVersion(String mrid, long version);

    /**
     * Finds the Device based on his unique External name.
     *
     * @param mrId the unique Identifier of the device
     * @return the requested Device or null if none was found
     */
    public Optional<Device> findByUniqueMrid(String mrId);

    /**
     * Finds the Devices (multiple are possible) based on the given serialNumber.
     *
     * @param serialNumber the serialNumber of the device
     * @return a list of Devices which have the given serialNumber
     */
    public List<Device> findDevicesBySerialNumber(String serialNumber);

    /**
     * Finds all the devices in the system with the specific condition.
     *
     * @return a list of all devices with the specific condition in the system
     */
    public Finder<Device> findAllDevices(Condition condition);

    /**
     * Returns true if the ComSchedule has been linked to a device.
     */
    public boolean isLinkedToDevices(ComSchedule comSchedule);

    public Finder<Device> findDevicesByDeviceConfiguration(DeviceConfiguration deviceConfiguration);

    /**
     * Finds all devices which have a property (general Attribute) with the given name and value.
     *
     * @param propertySpecName  the name of the property
     * @param propertySpecValue the value of the property
     * @return a list of all devices matching the given criteria
     */
    public List<Device> findDevicesByPropertySpecValue(String propertySpecName, String propertySpecValue);

    /**
     * Finds all devices which have a connectionTask of the given ConnectionType and a property matching the given arguments.
     *
     * @param connectionTypeClass the type of the connectionTask
     * @param propertyName        the name of the property
     * @param propertyValue       the value of the property
     * @return a list of all devices matching the given criteria
     */
    public List<Device> findDevicesByConnectionTypeAndProperty(Class<? extends ConnectionType> connectionTypeClass, String propertyName, String propertyValue);

    /**
     * Change the DeviceConfiguration of the device to the provided destinationDeviceConfiguration.
     * <b>NOTE:</b> Make sure you don't create your own transaction. This will be performed during the execution
     * of this method. Multiple transactions are required to perform Business Locks.
     *
     * @param device                         the Device(s) to change their configuration
     * @param destinationDeviceConfigId      the ID fo the DestinationDeviceConfig
     * @param destinationDeviceConfigVersion the version to check
     * @return the given device with the new configuration applied
     */
    public Device changeDeviceConfigurationForSingleDevice(Device device, long destinationDeviceConfigId, long destinationDeviceConfigVersion);

    /**
     * Change the DeviceConfiguration for the given set of Devices to the provided destinationDeviceConfiguration.
     * The action will be queued and the processing is asynchronously.
     *
     * @param destinationDeviceConfiguration the configuration which should be applied
     * @param device                         the Devices to change their configuration
     */
    public void changeDeviceConfigurationForDevices(DeviceConfiguration destinationDeviceConfiguration, Device... device);

}