/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.LoadProfileSpec;
import com.energyict.mdc.common.device.config.RegisterSpec;
import com.energyict.mdc.common.masterdata.ChannelType;
import com.energyict.mdc.common.masterdata.LoadProfileType;
import com.energyict.mdc.common.masterdata.RegisterType;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;

import com.energyict.obis.ObisCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BaseLoadProfileTest extends DeviceConfigurationApplicationJerseyTest {
    public static final long OK_VERSION = 24L;
    public static final long BAD_VERSION = 17L;

    protected List<LoadProfileType> getLoadProfileTypes(int count) {
        List<LoadProfileType> loadProfileTypes = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            TimeDuration randomTimeDuration = getTimeDuration();
            loadProfileTypes.add(mockLoadProfileType(1000 + i, String.format("Load Profile Type %04d", i), randomTimeDuration,
                    new ObisCode(i, i, i, i, i, i), getChannelTypes(getRandomInt(4), randomTimeDuration)));
        }
        return loadProfileTypes;
    }

    protected List<LoadProfileSpec> getLoadProfileSpecs(int count) {
        List<LoadProfileSpec> loadProfileSpecs = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            loadProfileSpecs.add(mockLoadProfileSpec(1000 + i));
        }
        return loadProfileSpecs;
    }


    protected List<ChannelType> getChannelTypes(int count, TimeDuration interval) {
        List<ChannelType> channelTypes = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            channelTypes.add(mockChannelType(1000 + i, String.format("Channel type %04d", i), new ObisCode(i, i, i, i, i, i), interval));
        }
        return channelTypes;
    }


    protected int getRandomInt(int end) {
        return getRandomInt(0, end);
    }

    protected int getRandomInt(int start, int end) {
        int range = end - start;
        return (int) (start + new Random().nextDouble() * range);
    }

    protected TimeDuration getTimeDuration() {
        return new TimeDuration(5, TimeDuration.TimeUnit.MINUTES);
    }

    protected DeviceType mockDeviceType(String name, long id) {
        DeviceType deviceType = mock(DeviceType.class);
        RegisteredCustomPropertySet registeredCustomPropertySet = mockRegisteredCustomPropertySet();
        when(deviceType.getLoadProfileTypeCustomPropertySet(anyObject())).thenReturn(Optional.of(registeredCustomPropertySet));
        when(deviceType.getName()).thenReturn(name);
        when(deviceType.getId()).thenReturn(id);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceType.getVersion()).thenReturn(OK_VERSION);
        return deviceType;
    }

    protected DeviceType mockDeviceType(String name, long id, List<PropertySpec> specs) {
        DeviceType deviceType = mock(DeviceType.class);
        RegisteredCustomPropertySet registeredCustomPropertySet = mockRegisteredCustomPropertySet();
        when(deviceType.getLoadProfileTypeCustomPropertySet(anyObject())).thenReturn(Optional.of(registeredCustomPropertySet));
        when(deviceType.getName()).thenReturn(name);
        when(deviceType.getId()).thenReturn(id);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getPropertySpecs()).thenReturn(specs);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocolPluggableClass.getId()).thenReturn(7L);
        when(deviceProtocolPluggableClass.getName()).thenReturn("device protocol pluggeable class");
        return deviceType;
    }

    protected DeviceConfiguration mockDeviceConfiguration(long id){
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getName()).thenReturn("Device configuration " + id);
        when(deviceConfiguration.getId()).thenReturn(id);
        RegisterSpec registerSpec = mock(RegisterSpec.class);
        RegisterType registerType = mock(RegisterType.class);
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        when(registerType.getId()).thenReturn(101L);
        when(deviceConfiguration.getRegisterSpecs()).thenReturn(Arrays.asList(registerSpec));
        when(deviceConfiguration.getVersion()).thenReturn(OK_VERSION);
        when(deviceConfigurationService.findDeviceConfiguration(id)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(id, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(id, BAD_VERSION)).thenReturn(Optional.empty());
        return deviceConfiguration;
    }

    protected LoadProfileType mockLoadProfileType(long id, String name, TimeDuration interval, ObisCode obisCode, List<ChannelType> channelTypes) {
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getId()).thenReturn(id);
        when(loadProfileType.getName()).thenReturn(name);
        when(loadProfileType.interval()).thenReturn(interval.asTemporalAmount());
        when(loadProfileType.getObisCode()).thenReturn(obisCode);
        when(loadProfileType.getChannelTypes()).thenReturn(channelTypes);
        when(loadProfileType.getVersion()).thenReturn(OK_VERSION);
        return loadProfileType;
    }

    protected RegisterType mockRegisterType(long id, String name, ObisCode obisCode) {
        RegisterType registerType = mock(RegisterType.class);
        when(registerType.getId()).thenReturn(id);
        when(registerType.getObisCode()).thenReturn(obisCode);
        when(registerType.getTimeOfUse()).thenReturn(0);
        ReadingType readingType = mockReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72." + id);
        when(readingType.getAliasName()).thenReturn(name);
        when(registerType.getReadingType()).thenReturn(readingType);
        return registerType;
    }

    protected ChannelType mockChannelType(long id, String name, ObisCode obisCode, TimeDuration interval) {
        ChannelType channelType = mock(ChannelType.class);
        when(channelType.getId()).thenReturn(id);
        when(channelType.getObisCode()).thenReturn(obisCode);
        when(channelType.getTimeOfUse()).thenReturn(0);
        ReadingType readingType = mockReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72." + id);
        when(readingType.getAliasName()).thenReturn(name);
        when(channelType.getReadingType()).thenReturn(readingType);
        when(channelType.getInterval()).thenReturn(interval);
        RegisterType templateRegister = mockRegisterType(id, name, obisCode);
        when(channelType.getTemplateRegister()).thenReturn(templateRegister);
        return channelType;
    }

    protected LoadProfileSpec mockLoadProfileSpec(long id){
        LoadProfileSpec loadProfileSpec = mock(LoadProfileSpec.class);
        ObisCode obisCode = new ObisCode(0, 1, 2, 3, 4, 5);
        ObisCode overruledObisCode = new ObisCode(200,201,202,203,204,205);
        TimeDuration randomTimeDuration = getTimeDuration();
        LoadProfileType loadProfileType = mockLoadProfileType(id, "Load profile spec " + id, randomTimeDuration, obisCode, getChannelTypes(2, randomTimeDuration));
        when(loadProfileSpec.getId()).thenReturn(id);
        when(loadProfileSpec.getLoadProfileType()).thenReturn(loadProfileType);
        when(loadProfileSpec.getObisCode()).thenReturn(obisCode);
        when(loadProfileSpec.getDeviceObisCode()).thenReturn(overruledObisCode);
        when(loadProfileSpec.getInterval()).thenReturn(getTimeDuration());
        when(loadProfileSpec.getVersion()).thenReturn(OK_VERSION);
        when(deviceConfigurationService.findLoadProfileSpec(id)).thenReturn(Optional.of(loadProfileSpec));
        when(deviceConfigurationService.findAndLockLoadProfileSpecByIdAndVersion(id, OK_VERSION)).thenReturn(Optional.of(loadProfileSpec));
        when(deviceConfigurationService.findAndLockLoadProfileSpecByIdAndVersion(id, BAD_VERSION)).thenReturn(Optional.empty());
        return loadProfileSpec;
    }
}
