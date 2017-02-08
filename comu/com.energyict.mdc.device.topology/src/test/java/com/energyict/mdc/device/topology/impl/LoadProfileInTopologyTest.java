/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test aspects of {@link com.energyict.mdc.device.data.LoadProfile}s that rely on the device topology.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-09 (15:09)
 */
public class LoadProfileInTopologyTest extends PersistenceTestWithMockedDeviceProtocol {

    private static final ObisCode loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");
    private static final String MRID = "MyUniqueMRID";
    private final TimeDuration interval = TimeDuration.minutes(15);
    private final Unit unit1 = Unit.get("kWh");
    private final Unit unit2 = Unit.get("MWh");

    private ReadingType readingType1;
    private ReadingType readingType2;
    private ObisCode obisCode1;
    private ObisCode obisCode2;
    private DeviceConfiguration deviceConfigurationWithLoadProfileAndChannels;
    private RegisterType registerType1;
    private RegisterType registerType2;
    private LoadProfileType loadProfileType;
    private BigDecimal overflow = BigDecimal.valueOf(999999999L);

    @Before
    public void initBefore() {
        this.setupReadingTypes();
        this.deviceConfigurationWithLoadProfileAndChannels = this.createDeviceConfigurationWithLoadProfileSpecAndTwoChannelSpecsSpecs();
    }

    private void setupReadingTypes() {
        String code = ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
        this.readingType1 = inMemoryPersistence.getMeteringService().getReadingType(code).get();
        this.obisCode1 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeInformationFor(readingType1).getObisCode();
        String code2 = ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.REVERSE)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
        this.readingType2 = inMemoryPersistence.getMeteringService().getReadingType(code2).get();
        this.obisCode2 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeInformationFor(readingType2).getObisCode();
    }

    private DeviceConfiguration createDeviceConfigurationWithLoadProfileSpecAndTwoChannelSpecsSpecs() {
        this.registerType1 = this.createRegisterTypeIfMissing("RegisterType1", this.obisCode1, this.unit1, this.readingType1, 0);
        this.registerType2 = this.createRegisterTypeIfMissing("RegisterType2", this.obisCode2, this.unit2, this.readingType2, 0);
        loadProfileType = inMemoryPersistence.getMasterDataService().newLoadProfileType("LoadProfileType", loadProfileObisCode, interval, Arrays.asList(registerType1, registerType2));
        ChannelType channelTypeForRegisterType1 = loadProfileType.findChannelType(registerType1).get();
        ChannelType channelTypeForRegisterType2 = loadProfileType.findChannelType(registerType2).get();
        loadProfileType.save();
        deviceType.addLoadProfileType(loadProfileType);
        DeviceType.DeviceConfigurationBuilder configurationWithLoadProfileAndChannel = deviceType.newConfiguration("ConfigurationWithLoadProfileAndChannel");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationWithLoadProfileAndChannel.newLoadProfileSpec(loadProfileType);
        configurationWithLoadProfileAndChannel.newChannelSpec(channelTypeForRegisterType1, loadProfileSpecBuilder).overflow(overflow).nbrOfFractionDigits(3);
        configurationWithLoadProfileAndChannel.newChannelSpec(channelTypeForRegisterType2, loadProfileSpecBuilder).overflow(overflow).nbrOfFractionDigits(3);
        DeviceConfiguration deviceConfiguration = configurationWithLoadProfileAndChannel.add();
        deviceConfiguration.activate();
        return deviceConfiguration;
    }

    private RegisterType createRegisterTypeIfMissing(String name, ObisCode obisCode, Unit unit, ReadingType readingType, int timeOfUse) {
        Optional<RegisterType> xRegisterType = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType);
        RegisterType registerType;
        if (xRegisterType.isPresent()) {
            registerType = xRegisterType.get();
        } else {
            registerType = inMemoryPersistence.getMasterDataService().newRegisterType(readingType, obisCode);
            registerType.save();
        }
        return registerType;
    }

    private LoadProfile getReloadedLoadProfile(Device device) {
        Device reloadedDevice = getReloadedDevice(device);
        return reloadedDevice.getLoadProfiles().get(0);
    }

    @Test
    @Transactional
    public void isVirtualLoadProfileTest() {
        Device masterWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles", "dwl", Instant.now());
        masterWithLoadProfile.save();

        Device slave = createSlaveDeviceWithSameLoadProfileType(masterWithLoadProfile);

        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(slave);
        assertThat(reloadedLoadProfile.isVirtualLoadProfile()).isTrue();
    }

    private Device createSlaveDeviceWithSameLoadProfileType(Device masterWithLoadProfile) {
        DeviceProtocolPluggableClass slaveDeviceProtocolPluggableClass = createSlaveDeviceProtocol();

        DeviceType slaveDeviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("SlaveDeviceType", slaveDeviceProtocolPluggableClass);
        slaveDeviceType.addLoadProfileType(loadProfileType);
        ChannelType channelTypeForRegisterType1 = loadProfileType.getChannelTypes().get(0);
        ChannelType channelTypeForRegisterType2 = loadProfileType.getChannelTypes().get(1);
        DeviceType.DeviceConfigurationBuilder configurationWithLoadProfileAndChannel = slaveDeviceType.newConfiguration("SlaveConfig");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationWithLoadProfileAndChannel.newLoadProfileSpec(loadProfileType);
        configurationWithLoadProfileAndChannel.newChannelSpec(channelTypeForRegisterType1, loadProfileSpecBuilder).overflow(overflow).nbrOfFractionDigits(3);
        configurationWithLoadProfileAndChannel.newChannelSpec(channelTypeForRegisterType2, loadProfileSpecBuilder).overflow(overflow).nbrOfFractionDigits(3);
        DeviceConfiguration deviceConfiguration = configurationWithLoadProfileAndChannel.add();
        deviceConfiguration.activate();

        Device slaveWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "slave", MRID, Instant.now());
        slaveWithLoadProfile.save();
        inMemoryPersistence.getTopologyService().setPhysicalGateway(slaveWithLoadProfile, masterWithLoadProfile);
        return slaveWithLoadProfile;
    }

    private DeviceProtocolPluggableClass createSlaveDeviceProtocol() {
        long slavePluggableClassId = 654;
        DeviceProtocol slaveDeviceProtocol = mock(DeviceProtocol.class);
        when(slaveDeviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_SLAVE));
        DeviceProtocolPluggableClass slaveDeviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(slaveDeviceProtocolPluggableClass.getId()).thenReturn(slavePluggableClassId);
        when(slaveDeviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(slaveDeviceProtocol);
        when(inMemoryPersistence.getProtocolPluggableService().findDeviceProtocolPluggableClass(slavePluggableClassId)).thenReturn(Optional.of(slaveDeviceProtocolPluggableClass));
        return slaveDeviceProtocolPluggableClass;
    }

    @Test
    @Transactional
    public void isNotVirtualLoadProfileBecauseSlaveHasLoadProfileTypeWithWildCardBFieldTest() {
        Device masterWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles", "M", Instant.now());
        masterWithLoadProfile.save();

        LoadProfileType slaveLoadProfileType = inMemoryPersistence.getMasterDataService().newLoadProfileType("SlaveType", ObisCode.fromString("0.x.24.3.0.255"), interval, Arrays.asList(registerType1));
        ChannelType channelTypeForRegisterType = slaveLoadProfileType.findChannelType(registerType1).get();
        slaveLoadProfileType.save();
        DeviceProtocolPluggableClass slaveDeviceProtocolPluggableClass = createSlaveDeviceProtocol();

        DeviceType slaveDeviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("SlaveDeviceType", slaveDeviceProtocolPluggableClass);
        slaveDeviceType.addLoadProfileType(slaveLoadProfileType);
        DeviceType.DeviceConfigurationBuilder configurationWithLoadProfileAndChannel = slaveDeviceType.newConfiguration("SlaveConfig");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationWithLoadProfileAndChannel.newLoadProfileSpec(slaveLoadProfileType);
        configurationWithLoadProfileAndChannel.newChannelSpec(channelTypeForRegisterType, loadProfileSpecBuilder).overflow(overflow).nbrOfFractionDigits(3);
        DeviceConfiguration deviceConfiguration = configurationWithLoadProfileAndChannel.add();
        deviceConfiguration.activate();

        Device slaveWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "slave", "S", Instant.now());
        slaveWithLoadProfile.save();
        inMemoryPersistence.getTopologyService().setPhysicalGateway(slaveWithLoadProfile, masterWithLoadProfile);

        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(slaveWithLoadProfile);
        assertThat(reloadedLoadProfile.isVirtualLoadProfile()).isFalse();
    }

}