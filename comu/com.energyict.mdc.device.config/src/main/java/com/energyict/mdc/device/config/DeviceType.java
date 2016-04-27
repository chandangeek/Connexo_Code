package com.energyict.mdc.device.config;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import aQute.bnd.annotation.ProviderType;

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

    /**
     * Returns number that uniquely identifies this DeviceType.
     *
     * @return the id
     */
    long getId();

    /**
     * Returns the name that uniquely identifies this DeviceType.
     *
     * @return the name
     */
    String getName();

    void setName(String newName);

    long getVersion();

    /**
     * Returns a description for this DeviceType.
     *
     * @return description
     */
    String getDescription();

    void setDescription(String newDescription);

    /**
     * Gets the {@link DeviceLifeCycle} that is currently in use
     * by all devices of this type.
     *
     * @return The current DeviceLifeCycle
     */
    DeviceLifeCycle getDeviceLifeCycle();

    /**
     * Gets the {@link DeviceLifeCycle} that was in use
     * by all devices of this type at the specified point in time.
     * May return an empty optional when the point in time
     * is before the creation time of this DeviceType.
     *
     * @param when The point in time
     * @return The DeviceLifeCycle
     */
    Optional<DeviceLifeCycle> getDeviceLifeCycle(Instant when);

    /**
     * Gets the List of {@link DeviceLifeCycleChangeEvent}s for this DeviceType.
     *
     * @return The List of DeviceLifeCycleChangeEvent
     */
    List<DeviceLifeCycleChangeEvent> getDeviceLifeCycleChangeEvents();

    /**
     * Returns true if this device type's protocol supports messaging.
     *
     * @return true if this device type's protocol supports messaging
     */
    boolean supportsMessaging();

    /**
     * Returns true if devices of this type are 'dumb' and cannot capture their own load profiles
     * but need a proxy device to capture them, False otherwise.
     * The DeviceType is a logicalSlave is his DeviceProtocol has the single capability
     * {@link DeviceProtocolCapabilities#PROTOCOL_SLAVE}
     *
     * @return true if devices of this type are logical slaves
     */
    boolean isLogicalSlave();

    /**
     * Returns the {@link DeviceProtocolPluggableClass} that will be used to
     * communicate with instances of this DeviceType.
     *
     * @return the DeviceProtocolPluggableClass
     */
    DeviceProtocolPluggableClass getDeviceProtocolPluggableClass();

    void setDeviceProtocolPluggableClass(String deviceProtocolPluggableClassName);

    void setDeviceProtocolPluggableClass(DeviceProtocolPluggableClass deviceProtocolPluggableClass);

    List<LogBookType> getLogBookTypes();

    void addLogBookType(LogBookType logBookType);

    void removeLogBookType(LogBookType logBookType);

    List<RegisterType> getRegisterTypes();

    void addRegisterType(RegisterType registerType);

    void addRegisterTypeCustomPropertySet(RegisterType registerType, RegisteredCustomPropertySet registeredCustomPropertySet);

    Optional<RegisteredCustomPropertySet> getRegisterTypeTypeCustomPropertySet(RegisterType registerType);

    void removeRegisterType(RegisterType registerType);

    List<AllowedCalendar> getAllowedCalendars();

    void addAllowedCalendar(AllowedCalendar allowedCalendar);

    public boolean isTimeOfUseAllowed();

    public void setTimeOfUseAllowed(boolean timeOfUseAllowed);

    List<LoadProfileType> getLoadProfileTypes();

    void addLoadProfileType(LoadProfileType loadProfileType);

    void addLoadProfileTypeCustomPropertySet(LoadProfileType loadProfileType, RegisteredCustomPropertySet registeredCustomPropertySet);

    Optional<RegisteredCustomPropertySet> getLoadProfileTypeCustomPropertySet(LoadProfileType loadProfileType);

    void removeLoadProfileType(LoadProfileType loadProfileType);

    DeviceUsageType getDeviceUsageType();

    void setDeviceUsageType(DeviceUsageType deviceUsageType);

    List<DeviceConfiguration> getConfigurations();

    List<RegisteredCustomPropertySet> getCustomPropertySets();

    void addCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet);

    void removeCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet);

    /**
     * Returns a DeviceConfigurationBuilder that allows the caller
     * to start building a new {@link DeviceConfiguration} that will be
     * added to this DeviceType once the building process is complete.
     *
     * @param name The name for the new DeviceConfiguration
     * @return The DeviceConfigurationBuilder
     */
    DeviceConfigurationBuilder newConfiguration(String name);

    void removeConfiguration(DeviceConfiguration deviceConfigurationToDelete);

    void delete();

    boolean canActAsGateway();

    boolean isDirectlyAddressable();

    boolean isDataloggerSlave();

    void setDeviceTypePurpose(DeviceTypePurpose deviceTypePurpose);

    List<DeviceConfigConflictMapping> getDeviceConfigConflictMappings();

    void update();

    interface DeviceTypeBuilder {
        DeviceTypeBuilder withRegisterTypes(List<RegisterType> registerTypes);

        DeviceTypeBuilder withLoadProfileTypes(List<LoadProfileType> loadProfileTypes);

        DeviceTypeBuilder withLogBookTypes(List<LogBookType> logBookTypes);

        DeviceTypeBuilder setDescription(String description);

        DeviceType create();
    }

    interface DeviceConfigurationBuilder {

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
        ChannelSpec.ChannelSpecBuilder newChannelSpec(ChannelType channelType, LoadProfileSpec loadProfileSpec);

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
        ChannelSpec.ChannelSpecBuilder newChannelSpec(ChannelType channelType, LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder);

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
        NumericalRegisterSpec.Builder newNumericalRegisterSpec(RegisterType registerType);

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
        TextualRegisterSpec.Builder newTextualRegisterSpec(RegisterType registerType);

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
        LoadProfileSpec.LoadProfileSpecBuilder newLoadProfileSpec(LoadProfileType loadProfileType);

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
        LogBookSpec.LogBookSpecBuilder newLogBookSpec(LogBookType logBookType);

        /**
         * Completes the building process, returning the {@link DeviceConfiguration}
         * after it was added to the DeviceType.
         * Note that any additional calls to this builder after this call
         * will throw an IllegalStateException.
         *
         * @return The DeviceConfiguration
         */
        DeviceConfiguration add();

        DeviceConfigurationBuilder description(String description);

        DeviceConfigurationBuilder canActAsGateway(boolean canActAsGateway);

        DeviceConfigurationBuilder isDirectlyAddressable(boolean canBeDirectlyAddressed);

        DeviceConfigurationBuilder gatewayType(GatewayType gatewayType);

        DeviceConfigurationBuilder dataloggerEnabled(boolean dataloggerEnabled);
    }

    /**
     * Please use the {@link DeviceTypeBuilder} instead, via the {@link DeviceConfigurationService}
     */
    @Deprecated
    void save();
}