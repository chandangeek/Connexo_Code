/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.configchange.DeviceConfigChangeInAction;
import com.energyict.mdc.device.data.impl.configchange.DeviceConfigChangeRequest;

import java.util.List;
import java.util.Optional;

/**
 * Adds behavior to {@link DeviceService} that is specific
 * to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-28 (11:24)
 */
public interface ServerDeviceService extends DeviceService {

    /**
     * Tests if there are {@link Device}s that were created
     * from the specified {@link DeviceConfiguration}.
     *
     * @param deviceConfiguration The DeviceConfiguration
     * @return <code>true</code> iff there is at least one Device created from the DeviceConfiguration
     */
    boolean hasDevices(DeviceConfiguration deviceConfiguration);

    /**
     * Tests if there are {@link Device}s that overrule properties
     * defined by the same dialect as the {@link ProtocolDialectConfigurationProperties}.
     *
     * @param configurationProperties The DeviceConfiguration
     * @return <code>true</code> iff there is at least one Device with overruling properties
     */
    boolean hasDevices(ProtocolDialectConfigurationProperties configurationProperties);

    /**
     * Tests if there are {@link Device}s that uses the given {@link AllowedCalendar}.
     *
     * @param allowedCalendar The AllowedCalendar
     * @return <code>true</code> iff there is at least one Device that uses the AllowedCalendar
     */
    boolean hasDevices(AllowedCalendar allowedCalendar);

    /**
     * Counts the number of {@link Device}s that rely on the value
     * of the dialect {@link PropertySpec} that is specified on the configuration level.
     * In other words, counts the devices that have <strong>NOT</strong> specified
     * a value for that same PropertySpec and would therefore run into problems
     * if the property were to be removed from the {@link ProtocolDialectConfigurationProperties}.
     *
     * @param configurationProperties The DeviceConfiguration
     * @param propertySpec The PropertySpec
     * @return <code>true</code> iff there is at least one Device with overruling properties
     */
    long countDevicesThatRelyOnRequiredProperty(ProtocolDialectConfigurationProperties configurationProperties, PropertySpec propertySpec);

    Query<Device> deviceQuery();

    /**
     * Checks if there is currently an active 'ChangeDeviceConfiguration' happening.
     * We do this by validation whether the origin or destination are part of a the business lock for deviceConfigChanges
     *
     * @param originDeviceConfiguration the origin DeviceConfiguration
     * @param destinationDeviceConfiguration the destination DeviceConfiguration
     * @return true if there is currently a changeDeviceConfiguration happening for either of the DeviceConfigurations
     */
    boolean hasActiveDeviceConfigChangesFor(DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration);

    Optional<DeviceConfigChangeRequest> findDeviceConfigChangeRequestById(long id);

    Optional<DeviceConfigChangeInAction> findDeviceConfigChangeInActionById(long id);

    /**
     * Finds a list of devices which have the same overridden OBIS code value for another register then defined by the provided registerspec
     *
     * @param registerSpec the registerSpec which OBIS code can have been updated
     * @return a list of devices which have an overridden value for the OBIS code, but not linked to provided registerpec
     */
    List<Device> findDeviceWithOverruledObisCodeForOtherThanRegisterSpec(RegisterSpec registerSpec);

    /**
     * Finds a list of devices which have the same overridden OBIS code value for another channel in the LoadProfile then defined by the provided channelspec
     *
     * @param channelSpec the ChannelSpec which OBIS code can have been updated
     * @return a list of devices which have an overridden value for the OBIS code, but not linked to provided ChannelSpec
     */
    List<Device> findDeviceWithOverruledObisCodeForOtherThanChannelSpec(ChannelSpec channelSpec);

    MultiplierType findDefaultMultiplierType();

    void clearMultiplierTypeCache();

    /**
     * Reloads and locks the device for update.
     * </br>
     * <b>Note that this is a blocking call</b>
     *
     * @param deviceId the ID of the device to lock
     * @return the reloaded and locked device
     */
    Device lockDevice(long deviceId);

}