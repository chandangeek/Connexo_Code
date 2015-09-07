package com.energyict.mdc.device.config;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.HasName;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * DeviceType defines the basic common attributes of a
 * physical (or virtual) device type.
 * Each physical device (Device) is an instance referring to
 * a specific DeviceType.
 *
 * @author Karel
 */
@ProviderType
public interface DeviceType extends HasId, HasName {

    public void setName (String newName);

    /**
     * Returns a description for this DeviceType.
     *
     * @return description
     */
    public String getDescription();

    public void setDescription(String newDescription);

    /**
     * Gets the {@link DeviceLifeCycle} that is currently in use
     * by all devices of this type.
     *
     * @return The current DeviceLifeCycle
     */
    public DeviceLifeCycle getDeviceLifeCycle();

    /**
     * Gets the {@link DeviceLifeCycle} that was in use
     * by all devices of this type at the specified point in time.
     * May return an empty optional when the point in time
     * is before the creation time of this DeviceType.
     *
     * @param when The point in time
     * @return The DeviceLifeCycle
     */
    public Optional<DeviceLifeCycle> getDeviceLifeCycle(Instant when);

    /**
     * Gets the List of {@link DeviceLifeCycleChangeEvent}s for this DeviceType.
     *
     * @return The List of DeviceLifeCycleChangeEvent
     */
    public List<DeviceLifeCycleChangeEvent> getDeviceLifeCycleChangeEvents();

    /**
     * Returns true if this device type's protocol supports messaging.
     *
     * @return true if this device type's protocol supports messaging
     */
    public boolean supportsMessaging();

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

    public List<RegisterType> getRegisterTypes();

    public void addRegisterType(RegisterType registerType);

    public void removeRegisterType(RegisterType registerType);

    public List<LoadProfileType> getLoadProfileTypes();

    public void addLoadProfileType (LoadProfileType loadProfileType);

    public void removeLoadProfileType (LoadProfileType loadProfileType);

    public DeviceUsageType getDeviceUsageType();

    public void setDeviceUsageType(DeviceUsageType deviceUsageType);

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

    public void removeConfiguration(DeviceConfiguration deviceConfigurationToDelete);

    public void save ();

    public void delete ();

    public boolean canActAsGateway();

    public boolean isDirectlyAddressable();

    long getVersion();

    List<DeviceConfigConflictMapping> getDeviceConfigConflictMappings();

    public interface DeviceConfigurationBuilder {

        /**
         * Returns a builder for a new {@link ChannelSpec} in the
         * {@link DeviceConfiguration} that is being built by this DeviceConfigurationBuilder.
         * Note that there is no need to call the add method as that
         * will be done by the {@link DeviceConfigurationBuilder#add()} method.
         *
         * @param channelType The ChannelType
         * @param loadProfileSpec The LoadProfileSpec
         * @return The builder
         * @see DeviceConfiguration#createChannelSpec(com.energyict.mdc.masterdata.ChannelType, LoadProfileSpec)
         * @see #add()
         */
        public ChannelSpec.ChannelSpecBuilder newChannelSpec(ChannelType channelType, LoadProfileSpec loadProfileSpec);

        /**
         * Returns a builder for a new {@link ChannelSpec} whose {@link LoadProfileSpec}
         * is also under construction in the {@link DeviceConfiguration}
         * that is being built by this DeviceConfigurationBuilder.
         * Note that there is no need to call the add method as that
         * will be done by the {@link DeviceConfigurationBuilder#add()} method.
         *
         * @param channelType The ChannelType
         * @param loadProfileSpecBuilder The LoadProfileSpecBuilder
         * @return The builder
         * @see DeviceConfiguration#createChannelSpec(com.energyict.mdc.masterdata.ChannelType, LoadProfileSpec)
         * @see #add()
         */
        public ChannelSpec.ChannelSpecBuilder newChannelSpec(ChannelType channelType, LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder);

        /**
         * Returns a builder for a new {@link RegisterSpec} in the
         * {@link DeviceConfiguration} that is being built by this DeviceConfigurationBuilder.
         * Note that there is no need to call the add method as that
         * will be done by the {@link DeviceConfigurationBuilder#add()} method.
         *
         * @param registerType The RegisterType
         * @return The builder
         * @see DeviceConfiguration#createNumericalRegisterSpec(RegisterType)
         * @see #add()
         */
        public NumericalRegisterSpec.Builder newNumericalRegisterSpec(RegisterType registerType);

        /**
         * Returns a builder for a new {@link RegisterSpec} in the
         * {@link DeviceConfiguration} that is being built by this DeviceConfigurationBuilder.
         * Note that there is no need to call the add method as that
         * will be done by the {@link DeviceConfigurationBuilder#add()} method.
         *
         * @param registerType The RegisterType
         * @return The builder
         * @see DeviceConfiguration#createTextualRegisterSpec(RegisterType)
         * @see #add()
         */
        public TextualRegisterSpec.Builder newTextualRegisterSpec(RegisterType registerType);

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
        DeviceConfigurationBuilder canActAsGateway(boolean canActAsGateway);
        DeviceConfigurationBuilder isDirectlyAddressable(boolean canBeDirectlyAddressed);
        DeviceConfigurationBuilder gatewayType(GatewayType gatewayType);
    }

}