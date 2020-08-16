/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.common.masterdata.ChannelType;
import com.energyict.mdc.common.masterdata.LoadProfileType;
import com.energyict.mdc.common.masterdata.LogBookType;
import com.energyict.mdc.common.masterdata.RegisterType;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;

import aQute.bnd.annotation.ConsumerType;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.InputStream;
import java.nio.file.Path;
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
@ConsumerType
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@XmlAccessorType(XmlAccessType.NONE)
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
    @XmlAttribute
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
    @XmlAttribute
    boolean isLogicalSlave();

    /**
     * Returns the {@link DeviceProtocolPluggableClass} that will be used to
     * communicate with instances of this DeviceType.
     *
     * @return the DeviceProtocolPluggableClass
     */
    Optional<DeviceProtocolPluggableClass> getDeviceProtocolPluggableClass();

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

    AllowedCalendar addCalendar(Calendar calendar);

    AllowedCalendar addGhostCalendar(String name);

    void removeCalendar(AllowedCalendar calendar);

    List<LoadProfileType> getLoadProfileTypes();

    void addLoadProfileType(LoadProfileType loadProfileType);

    void addLoadProfileTypeCustomPropertySet(LoadProfileType loadProfileType, RegisteredCustomPropertySet registeredCustomPropertySet);

    Optional<RegisteredCustomPropertySet> getLoadProfileTypeCustomPropertySet(LoadProfileType loadProfileType);

    void removeLoadProfileType(LoadProfileType loadProfileType);

    DeviceUsageType getDeviceUsageType();

    void setDeviceUsageType(DeviceUsageType deviceUsageType);

    List<DeviceConfiguration> getConfigurations();

    List<DeviceConfiguration> getConfigurationsWithObsolete();

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

    /**
     * Devicetype used for data logger slave devices Yes/No
     * @return true is this device type is for data logger slave devices, false if not
     */
    boolean isDataloggerSlave();

    /**
     * Device type used for submeter elements (= virtual slaves of a regular device for 'ad hoc' configuration of datasources
     * @return true is this device type is used for submeter elements within a regular device, for if not
     */
    boolean isMultiElementSlave();

    void setDeviceTypePurpose(DeviceTypePurpose deviceTypePurpose);

    List<DeviceConfigConflictMapping> getDeviceConfigConflictMappings();

    /**
     * Tests if this file management is enabled for this DeviceType.
     * If it is then you can add {@link DeviceMessageFile}s
     * that can later be sent to devices of this type.
     *
     * @return A flag that indicates if file management is enabled for this DeviceType
     */
    boolean isFileManagementEnabled();

    /**
     * Enables file management or does nothing when file management is already enabled.
     */
    void enableFileManagement();

    /**
     * Disables file management or does nothing when file management is already disabled.
     * In addition, this will delete all {@link DeviceMessageFile}s
     * that have been added to this DeviceType before.
     */
    void disableFileManagement();

    byte[] getDeviceIcon();

    void setDeviceIcon(InputStream inputStream);

    void removeDeviceIcon();

    /**
     * The List of {@link DeviceMessageFile}s that have been added
     * to this DeviceType. Note that when file management is not enabled
     * on this DeviceType, this List will always be empty.
     *
     * @return The list of DeviceMessageFile
     */
    List<DeviceMessageFile> getDeviceMessageFiles();

    /**
     * Adds a new {@link DeviceMessageFile} to this DeviceType.
     * The filename part of the Path will be used as the name
     * of the newly created DeviceMessageFile.
     * Note that the name of a DeviceMessageFile needs to be unique.
     * In other words, no two DeviceMessageFile with the same name
     * can be added to the same DeviceType.
     *
     * @param path The path
     * @throws DuplicateDeviceMessageFileException Thrown if the name part of the Path conflicts with the name of another DeviceMessageFile that was added before
     */
    DeviceMessageFile addDeviceMessageFile(Path path);

    DeviceMessageFile addDeviceMessageFile(InputStream inputStream, String fileName);

    void removeDeviceMessageFile(DeviceMessageFile obsolete);

    void update();


    /**
     * @return a list of all known security accessor types linked to this device type.
     * Use getDeviceSecurityAccessorType() instead
     */
    @Deprecated
    List<SecurityAccessorType> getSecurityAccessorTypes();

    /**
     * Returns a list of all known security accessor types linked to this device type.
     * @return
     */
    List<DeviceSecurityAccessorType> getDeviceSecurityAccessorType();

    /**
     *
     * @param keyAccessorType
     * @return DeviceSecurityAccessorType that is defined (including wrapper se accessor). If not defined empty
     */
    Optional<SecurityAccessorType> getWrappingSecurityAccessorType(SecurityAccessorType keyAccessorType);

    /**
     * Adds given security accessor types to this device type if not linked already.
     * @param securityAccessorTypes
     * @return {@code true} if device type is updated with new links to security accessor types, {@code false} otherwise.
     */
    boolean addDeviceSecurityAccessorType(DeviceSecurityAccessorType... deviceSecurityAccessorTypes);

    /**
     * Removes the SecurityAccessorType from the DeviceType.
     * @param securityAccessorType
     * @return {@code true} if a link to security accessor type is removed from device type, {@code false} otherwise.
     */
    boolean removeDeviceSecurityAccessorType(DeviceSecurityAccessorType securityAccessorType);

    void setWrappingSecurityAccessor(DeviceSecurityAccessorType deviceSecurityAccessorType, Optional<SecurityAccessorType> wrappingSecurityAccessor);

    Optional<String> getDefaultKeyOfSecurityAccessorType(SecurityAccessorType securityAccessorType);

    void updateDefaultKeyOfSecurityAccessorType(SecurityAccessorType securityAccessorType, String value);

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    public String getXmlType();

    public void setXmlType(String ignore);

    interface DeviceTypeBuilder {
        DeviceTypeBuilder withRegisterTypes(List<RegisterType> registerTypes);

        DeviceTypeBuilder withLoadProfileTypes(List<LoadProfileType> loadProfileTypes);

        DeviceTypeBuilder withLogBookTypes(List<LogBookType> logBookTypes);

        DeviceTypeBuilder setDescription(String description);

        /**
         * Enables file management for the DeviceType that is being built.
         * Note that file management is disabled by default.
         *
         * @return The DeviceTypeBuilder to support method chaining
         */
        DeviceTypeBuilder enableFileManagement();

        DeviceType create();
    }

    @ConsumerType
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
         * @see DeviceConfiguration#createChannelSpec(ChannelType, LoadProfileSpec)
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
         * @see DeviceConfiguration#createChannelSpec(ChannelType, LoadProfileSpec)
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

        DeviceConfigurationBuilder multiElementEnabled(boolean multiElementEnabled);

        DeviceConfigurationBuilder validateOnStore(boolean validateOnStore);
    }

    /**
     * Please use the {@link DeviceTypeBuilder} instead, via the {@link DeviceConfigurationService}
     */
    @Deprecated
    void save();


    /**
     * Returns a list of all known security accessor linked to this device type.
     * @return
     */
    List<SecurityAccessorTypeOnDeviceType> getSecurityAccessors();
}
