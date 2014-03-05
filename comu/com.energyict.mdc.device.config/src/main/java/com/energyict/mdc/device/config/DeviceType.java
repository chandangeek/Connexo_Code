package com.energyict.mdc.device.config;

import com.energyict.mdc.common.interval.Phenomenon;
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

    public void setDeviceProtocolPluggableClass(String deviceProtocolPluggableClassName);

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

    public void addCommunicationFunction(DeviceCommunicationFunction function);

    public void removeCommunicationFunction(DeviceCommunicationFunction function);

    public List<DeviceConfiguration> getConfigurations();

    /**
     * Returns a DeviceConfigurationBuilder that allows the caller
     * to start building a new {@link DeviceConfiguration} that will be
     * added to this DeviceType once the building process is complete.
     *
     * @param name The name for the new DeviceConfiguration
     * @return The DeviceConfigurationBuilder
     */
    public DeviceConfigurationBuilder newConfiguration (String name);

    public void save ();

    public void delete ();

    public interface DeviceConfigurationBuilder {

        /**
         * Returns a builder for a new {@link ChannelSpec} in the
         * {@link DeviceConfiguration} that is being built by this DeviceConfigurationBuilder.
         * Note that there is no need to call the add method as that
         * will be done by the {@link DeviceConfigurationBuilder#add()} method.
         *
         * @param registerMapping The RegisterMapping
         * @param phenomenon The Phenomenon
         * @param loadProfileSpec The LoadProfileSpec
         * @return The builder
         * @see DeviceConfiguration#createChannelSpec(RegisterMapping, Phenomenon, LoadProfileSpec)
         * @see #add()
         */
        public ChannelSpec.ChannelSpecBuilder newChannelSpec(RegisterMapping registerMapping, Phenomenon phenomenon, LoadProfileSpec loadProfileSpec);

        /**
         * Returns a builder for a new {@link ChannelSpec} whose {@link LoadProfileSpec}
         * is also under construction in the {@link DeviceConfiguration}
         * that is being built by this DeviceConfigurationBuilder.
         * Note that there is no need to call the add method as that
         * will be done by the {@link DeviceConfigurationBuilder#add()} method.
         *
         * @param registerMapping The RegisterMapping
         * @param phenomenon The Phenomenon
         * @param loadProfileSpecBuilder The LoadProfileSpecBuilder
         * @return The builder
         * @see DeviceConfiguration#createChannelSpec(RegisterMapping, Phenomenon, LoadProfileSpec)
         * @see #add()
         */
        public ChannelSpec.ChannelSpecBuilder newChannelSpec(RegisterMapping registerMapping, Phenomenon phenomenon, LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder);

        /**
         * Returns a builder for a new {@link RegisterSpec} in the
         * {@link DeviceConfiguration} that is being built by this DeviceConfigurationBuilder.
         * Note that there is no need to call the add method as that
         * will be done by the {@link DeviceConfigurationBuilder#add()} method.
         *
         * @param registerMapping The RegisterMapping
         * @return The builder
         * @see DeviceConfiguration#createRegisterSpec(RegisterMapping)
         * @see #add()
         */
        public RegisterSpec.RegisterSpecBuilder newRegisterSpec(RegisterMapping registerMapping);

        /**
         * Returns a builder for a new {@link LoadProfileSpec} in the
         * {@link DeviceConfiguration} that is being built by this DeviceConfigurationBuilder.
         * Note that there is no need to call the add method as that
         * will be done by the {@link DeviceConfigurationBuilder#add()} method.
         *
         * @param loadProfileType The LoadProfileType
         * @return The builder
         * @see DeviceConfiguration#createLoadProfileSpec(LoadProfileType)
         * @see #add()
         */
        public LoadProfileSpec.LoadProfileSpecBuilder newLoadProfileSpec(LoadProfileType loadProfileType);

        /**
         * Returns a builder for a new {@link LogBookSpec} in the
         * {@link DeviceConfiguration} that is being built by this DeviceConfigurationBuilder.
         * Note that there is no need to call the add method as that
         * will be done by the {@link DeviceConfigurationBuilder#add()} method.
         *
         * @param logBookType The LogBookType
         * @return The builder
         * @see DeviceConfiguration#createLogBookSpec(LogBookType)
         */
        public LogBookSpec.LogBookSpecBuilder newLogBookSpec(LogBookType logBookType);

        /**
         * Completes the building process, returning the {@link DeviceConfiguration}
         * after it was added to the DeviceType.
         * Note that any additional calls to this builder after this call
         * will throw an IllegalStateException.
         *
         * @return The DeviceConfiguration
         */
        public DeviceConfiguration add ();

        DeviceConfigurationBuilder description(String description);
    }

}