package com.energyict.mdc.device.config;

import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import java.util.List;
import java.util.Set;

/**
 * DeviceType defines the basic common attributes of a
 * physical (or virtual) device type.
 * Each physical device (Device) is an instance referrring to
 * a specific DeviceType.
 *
 * @author Karel
 */
public interface DeviceType {

    /**
     * Returns number that uniquely identifies this DeviceType.
     *
     * @return the id
     */
    public long getId();

    /**
     * Returns the name that uniquely identifies this DeviceType.
     *
     * @return the name
     */
    public String getName();

    public void setName (String newName);

    /**
     * Returns a description for this DeviceType.
     *
     * @return description
     */
    public String getDescription();

    public void setDescription(String newDescription);

    /**
     * Returns true if this device type's protocol supports messaging.
     *
     * @return true if this device type's protocol supports messaging
     */
    public boolean supportsMessaging();

    /**
     * Returns true if the channel journal needs to be used.
     *
     * @return true if the channel journal needs to be used
     */
    public boolean isChannelJournalUsed();

    /**
     * Returns true if devices of this type are 'dumb' and cannot capture their own load profiles
     * but need a proxy device to capture them, False otherwise.
     * The DeviceType is a logicalSlave is his DeviceProtocol has the single capability
     * {@link DeviceProtocolCapabilities#PROTOCOL_SLAVE}
     *
     * @return true if devices of this type are logical slaves
     */
    public boolean isLogicalSlave();

    /**
     * Returns the {@link DeviceProtocolPluggableClass} that will be used to
     * communicate with instances of this DeviceType.
     *
     * @return the DeviceProtocolPluggableClass
     */
    public DeviceProtocolPluggableClass getDeviceProtocolPluggableClass();

    public void setDeviceProtocolPluggableClass(DeviceProtocolPluggableClass deviceProtocolPluggableClass);

    public List<LogBookType> getLogBookTypes();

    public void addLogBookType (LogBookType logBookType);

    public void removeLogBookType (LogBookType logBookType);

    public List<RegisterMapping> getRegisterMappings();

    public void addRegisterMapping (RegisterMapping registerMapping);

    public void removeRegisterMapping (RegisterMapping registerMapping);

    public List<LoadProfileType> getLoadProfileTypes();

    public void addLoadProfileType (LoadProfileType loadProfileType);

    public void removeLoadProfileType (LoadProfileType loadProfileType);

    public DeviceUsageType getDeviceUsageType();

    public Set<DeviceCommunicationFunction> getCommunicationFunctions();

    public boolean hasCommunicationFunction(DeviceCommunicationFunction function);

    public void save ();

    public void delete ();

}