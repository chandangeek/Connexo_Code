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
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the aspects of the {@link TopologyServiceImpl} component
 * that relate to {@link LoadProfile}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (17:23)
 */
public class TopologyServiceLoadProfileImplTest extends PersistenceTestWithMockedDeviceProtocol {

    // Look at LoadProfileImplTest for missing members as the code was already partially moved from there

    private static final ObisCode loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");
    private static final String MRID = "MyUniqueMRID";
    private final TimeDuration interval = TimeDuration.minutes(15);
    private final Unit unit1 = Unit.get("kWh");
    private final Unit unit2 = Unit.get("MWh");
    private final BigDecimal overflow = BigDecimal.valueOf(999999999L);

    private DeviceConfiguration deviceConfigurationWithLoadProfileAndChannels;
    private RegisterType registerType1;
    private RegisterType registerType2;
    private ReadingType readingType1;
    private ReadingType readingType2;
    private ObisCode obisCode1;
    private ObisCode obisCode2;
    private LoadProfileType loadProfileType;

    @Before
    public void initializeMocks() {
        super.initializeMocks();
        this.setupReadingTypes();
        deviceConfigurationWithLoadProfileAndChannels = createDeviceConfigurationWithLoadProfileSpecAndTwoChannelSpecsSpecs();
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
        }
        else {
            registerType = inMemoryPersistence.getMasterDataService().newRegisterType(readingType, obisCode);
            registerType.save();
        }
        return registerType;
    }

    @Test
    @Transactional
    public void getAllChannelsTestWithASlaveDeviceTest() {
        Device masterWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles", "dwl", Instant.now());
        masterWithLoadProfile.save();
        Device slave = createSlaveDeviceWithSameLoadProfileType(masterWithLoadProfile);
        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(masterWithLoadProfile);

        // Business method
        List<Channel> allChannels = getTopologyService().getAllChannels(reloadedLoadProfile);

        // Asserts
        assertThat(allChannels).hasSize(4);
        assertThat(allChannels).has(new Condition<List<? extends Channel>>() {
            @Override
            public boolean matches(List<? extends Channel> value) {
                int masterChannels = 0;
                int slaveChannels = 0;
                int obisCode1Match = 0;
                int obisCode2Match = 0;
                for (Channel channel : value) {
                    if (channel.getDevice().getId() == masterWithLoadProfile.getId()) {
                        masterChannels++;
                    }
                    if (channel.getDevice().getId() == slave.getId()) {
                        slaveChannels++;
                    }
                    if (channel.getRegisterTypeObisCode().equals(obisCode1)) {
                        obisCode1Match++;
                    }
                    if (channel.getRegisterTypeObisCode().equals(obisCode2)) {
                        obisCode2Match++;
                    }
                }
                return masterChannels == 2 && slaveChannels == 2 && obisCode1Match == 2 && obisCode2Match == 2;
            }
        });
    }

    @Test
    @Transactional
    public void getAllChannelsTestWithoutSlaveDevices() {
        Device deviceWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles", MRID, Instant.now());
        deviceWithLoadProfile.save();
        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(deviceWithLoadProfile);

        // Business method
        List<Channel> allChannels = getTopologyService().getAllChannels(reloadedLoadProfile);

        // Asserts
        assertThat(allChannels).hasSize(2);
        assertThat(allChannels).has(new Condition<List<? extends Channel>>() {
            @Override
            public boolean matches(List<? extends Channel> value) {
                boolean bothMatch = true;
                for (Channel channel : value) {
                    bothMatch &= (channel.getRegisterTypeObisCode().equals(obisCode1) || channel.getRegisterTypeObisCode().equals(obisCode2));
                }
                return bothMatch;
            }
        });
    }

    @Test
    @Transactional
    public void isNotVirtualLoadProfileBecauseDeviceProtocolNotLogicalSlaveTest() {
        Device masterWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles", "dwl", Instant.now());
        masterWithLoadProfile.save();
        Device slaveWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "Slave", "slave", Instant.now());
        slaveWithLoadProfile.save();
        getTopologyService().setPhysicalGateway(slaveWithLoadProfile, masterWithLoadProfile);

        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(slaveWithLoadProfile);

        assertThat(reloadedLoadProfile.isVirtualLoadProfile()).isFalse();
    }

    private TopologyService getTopologyService() {
        return inMemoryPersistence.getTopologyService();
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
        getTopologyService().setPhysicalGateway(slaveWithLoadProfile, masterWithLoadProfile);
        return slaveWithLoadProfile;
    }

    private LoadProfile getReloadedLoadProfile(Device device) {
        Device reloadedDevice = getReloadedDevice(device);
        return reloadedDevice.getLoadProfiles().get(0);
    }

}