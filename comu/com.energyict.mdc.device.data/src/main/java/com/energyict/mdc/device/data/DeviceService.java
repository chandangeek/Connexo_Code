package com.energyict.mdc.device.data;

import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.conditions.Condition;

import java.util.List;

/**
 * Provides services that relate to {@link Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (14:27)
 */
public interface DeviceService {

    /**
     * Tests if there are {@link Device}s that were created
     * from the specified {@link DeviceConfiguration}.
     *
     * @param deviceConfiguration The DeviceConfiguration
     * @return <code>true</code> iff there is at least one Device created from the DeviceConfiguration
     */
    public boolean hasDevices(DeviceConfiguration deviceConfiguration);

    /**
     * Tests if there are {@link Device}s that overrule properties
     * defined by the same dialect as the {@link ProtocolDialectConfigurationProperties}.
     *
     * @param configurationProperties The DeviceConfiguration
     * @return <code>true</code> iff there is at least one Device with overruling properties
     */
    public boolean hasDevices(ProtocolDialectConfigurationProperties configurationProperties);

    /**
     * Tests if there are {@link Device}s that overrule the specified {@link PropertySpec}
     * defined by the same dialect as the {@link ProtocolDialectConfigurationProperties}.
     *
     * @param configurationProperties The DeviceConfiguration
     * @param propertySpec The PropertySpec
     * @return <code>true</code> iff there is at least one Device with overruling properties
     */
    public boolean hasDevices(ProtocolDialectConfigurationProperties configurationProperties, PropertySpec propertySpec);

    /**
     * Creates a new Device based on the given name and DeviceConfiguration
     *
     * @param deviceConfiguration the deviceConfiguration which models the device
     * @param name                the name which should be used for the device
     * @param mRID                The new Device's master resource identifier
     * @return the newly created Device
     */
    public Device newDevice(DeviceConfiguration deviceConfiguration, String name, String mRID);

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
    public Query<Device> deviceQuery();
    
}