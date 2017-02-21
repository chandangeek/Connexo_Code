/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class LoadProfileImplTest extends PersistenceTestWithMockedDeviceProtocol {

    private static final ObisCode loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");
    private static final String MRID = "MyUniqueMRID";
    private final TimeDuration interval = TimeDuration.minutes(15);
    private final Unit unit1 = Unit.get("kWh");
    private final Unit unit2 = Unit.get("MWh");
    private final BigDecimal overflow = BigDecimal.TEN;
    private final Instant januaryTenth = Instant.ofEpochSecond(1452384000L);

    private ReadingType rt_bulkActiveEnergySecondary;
    private ReadingType rt_deltaActiveEnergyPrimary15Min;
    private ReadingType rt_bulkReactiveEnergySecondary;
    private ObisCode obisCode1;
    private ObisCode obisCode2;
    private ObisCode overruledObisCode = ObisCode.fromString("0.0.0.0.0.0");
    private DeviceConfiguration deviceConfigurationWithLoadProfileAndChannels;
    private RegisterType registerType1;
    private RegisterType registerType2;
    private LoadProfileType loadProfileType;
    private final String bulkActiveEnergySecondaryMrid = ReadingTypeCodeBuilder
            .of(Commodity.ELECTRICITY_SECONDARY_METERED)
            .accumulate(Accumulation.BULKQUANTITY)
            .flow(FlowDirection.FORWARD)
            .measure(MeasurementKind.ENERGY)
            .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
    private final String deltaActiveEnergyPrimary15MinMrid = ReadingTypeCodeBuilder
            .of(Commodity.ELECTRICITY_PRIMARY_METERED)
            .period(TimeAttribute.MINUTE15)
            .accumulate(Accumulation.DELTADELTA)
            .flow(FlowDirection.FORWARD)
            .measure(MeasurementKind.ENERGY)
            .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
    private final String deltaActiveEnergySecondary15MinMrid = ReadingTypeCodeBuilder
            .of(Commodity.ELECTRICITY_SECONDARY_METERED)
            .period(TimeAttribute.MINUTE15)
            .accumulate(Accumulation.DELTADELTA)
            .flow(FlowDirection.FORWARD)
            .measure(MeasurementKind.ENERGY)
            .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
    private final String bulkReactiveEnergySecondaryMrid = ReadingTypeCodeBuilder
            .of(Commodity.ELECTRICITY_SECONDARY_METERED)
            .accumulate(Accumulation.BULKQUANTITY)
            .flow(FlowDirection.REVERSE)
            .measure(MeasurementKind.ENERGY)
            .in(MetricMultiplier.MEGA, ReadingTypeUnit.WATTHOUR).code();

    @Before
    public void initBefore() {
        this.setupReadingTypes();
        deviceConfigurationWithLoadProfileAndChannels = createDeviceConfigurationWithLoadProfileSpecAndTwoChannelSpecsSpecs();
    }

    @After
    public void reset() {
        when(inMemoryPersistence.getClock().instant()).thenReturn(januaryFirst);
        // MultiplierType is a cached object - make sure the cache is cleared after each test
        inMemoryPersistence.getDataModel().getInstance(OrmService.class).invalidateCache("MTR", "MTR_MULTIPLIERTYPE");
    }

    private void setupReadingTypes() {
        this.rt_bulkActiveEnergySecondary = inMemoryPersistence.getMeteringService().getReadingType(bulkActiveEnergySecondaryMrid).get();
        this.obisCode1 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeInformationFor(rt_bulkActiveEnergySecondary).getObisCode();
        this.rt_bulkReactiveEnergySecondary = inMemoryPersistence.getMeteringService().getReadingType(bulkReactiveEnergySecondaryMrid).get();
        this.obisCode2 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeInformationFor(rt_bulkReactiveEnergySecondary).getObisCode();
        this.rt_deltaActiveEnergyPrimary15Min = inMemoryPersistence.getMeteringService().getReadingType(deltaActiveEnergyPrimary15MinMrid).get();
    }

    private DeviceConfiguration createDeviceConfigurationWithLoadProfileSpecAndTwoChannelSpecsSpecs() {
        this.registerType1 = this.createRegisterTypeIfMissing(this.obisCode1, this.rt_bulkActiveEnergySecondary);
        this.registerType2 = this.createRegisterTypeIfMissing(this.obisCode2, this.rt_bulkReactiveEnergySecondary);
        loadProfileType = inMemoryPersistence.getMasterDataService().newLoadProfileType("LoadProfileType", loadProfileObisCode, interval, Arrays.asList(registerType1, registerType2));
        ChannelType channelTypeForRegisterType1 = loadProfileType.findChannelType(registerType1).get();
        ChannelType channelTypeForRegisterType2 = loadProfileType.findChannelType(registerType2).get();
        loadProfileType.save();
        deviceType.addLoadProfileType(loadProfileType);
        DeviceType.DeviceConfigurationBuilder configurationWithLoadProfileAndChannel = deviceType.newConfiguration("ConfigurationWithLoadProfileAndChannel");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationWithLoadProfileAndChannel.newLoadProfileSpec(loadProfileType);
        configurationWithLoadProfileAndChannel.newChannelSpec(channelTypeForRegisterType1, loadProfileSpecBuilder).overflow(overflow).nbrOfFractionDigits(2);
        configurationWithLoadProfileAndChannel.newChannelSpec(channelTypeForRegisterType2, loadProfileSpecBuilder).overruledObisCode(overruledObisCode).overflow(overflow).nbrOfFractionDigits(2);
        DeviceConfiguration deviceConfiguration = configurationWithLoadProfileAndChannel.add();
        deviceConfiguration.activate();
        return deviceConfiguration;
    }

    private RegisterType createRegisterTypeIfMissing(ObisCode obisCode, ReadingType readingType) {
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

    private Device createSimpleDeviceWithLoadProfiles() {
        return inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceName", MRID, januaryFirst);
    }

    private LoadProfile getReloadedLoadProfile(Device device) {
        Device reloadedDevice = getReloadedDevice(device);
        return reloadedDevice.getLoadProfiles().get(0);
    }

    @Test
    @Transactional
    public void createWithNoLoadProfileSpecsTest() {
        Device deviceWithoutLoadProfiles = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "DeviceWithoutLoadProfiles", MRID, januaryFirst);

        Device reloadedDevice = getReloadedDevice(deviceWithoutLoadProfiles);
        assertThat(reloadedDevice.getLoadProfiles()).isEmpty();
    }

    @Test
    @Transactional
    public void getChannelsTest() {
        Device deviceWithLoadProfile = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles", MRID, januaryFirst);
        deviceWithLoadProfile.save();

        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(deviceWithLoadProfile);
        assertThat(reloadedLoadProfile.getChannels()).hasSize(2);
        assertThat(reloadedLoadProfile.getChannels()).has(new Condition<List<? extends Channel>>() {
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
    public void isNotVirtualLoadProfileTest() {
        Device masterWithLoadProfile = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles", MRID, januaryFirst);

        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(masterWithLoadProfile);
        assertThat(reloadedLoadProfile.isVirtualLoadProfile()).isFalse();
    }

    @Test
    @Transactional
    public void getIntervalTest() {
        Device deviceWithLoadProfile = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles", MRID, januaryFirst);

        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(deviceWithLoadProfile);
        assertThat(reloadedLoadProfile.getInterval()).isEqualTo(interval);
    }

    @Test
    @Transactional
    public void getDeviceObisCodeTest() {
        Device deviceWithLoadProfile = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles", MRID, januaryFirst);

        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(deviceWithLoadProfile);
        assertThat(reloadedLoadProfile.getDeviceObisCode()).isEqualTo(loadProfileObisCode);

        List<Channel> channels = reloadedLoadProfile.getChannels();
        assertThat(channels).hasSize(2);
        assertThat(channels.get(0).getObisCode()).isEqualTo(obisCode1);
        assertThat(channels.get(1).getObisCode()).isEqualTo(overruledObisCode);
    }

    @Test
    @Transactional
    public void createWithoutViolationsTest() {
        Device device = createSimpleDeviceWithLoadProfiles();

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getLoadProfiles()).hasSize(1);
        assertThat(reloadedDevice.getLoadProfiles().get(0).getDeviceObisCode()).isEqualTo(loadProfileObisCode);
    }

    @Test
    @Transactional
    public void lastReadingEmptyOnCreationTest() {
        Device device = createSimpleDeviceWithLoadProfiles();

        LoadProfile loadProfile = getReloadedLoadProfile(device);
        assertThat(loadProfile.getLastReading().isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void updateLastReadingTest() {
        Device device = createSimpleDeviceWithLoadProfiles();
        Instant newLastReading = Instant.ofEpochMilli(123546);
        LoadProfile loadProfile = getReloadedLoadProfile(device);
        LoadProfile.LoadProfileUpdater loadProfileUpdater = device.getLoadProfileUpdaterFor(loadProfile);
        loadProfileUpdater.setLastReading(newLastReading);
        loadProfileUpdater.update();
        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(device);
        assertThat(reloadedLoadProfile.getLastReading().isPresent()).isTrue();
        assertThat(reloadedLoadProfile.getLastReading().get()).isEqualTo(newLastReading);
    }

    @Test
    @Transactional
    public void updateLastReadingIfLaterTest() {
        Device device = createSimpleDeviceWithLoadProfiles();
        Instant oldLastReading = Instant.ofEpochMilli(123);
        Instant newLastReading = Instant.ofEpochMilli(123546);
        LoadProfile loadProfile = getReloadedLoadProfile(device);
        LoadProfile.LoadProfileUpdater loadProfileUpdater = device.getLoadProfileUpdaterFor(loadProfile);
        loadProfileUpdater.setLastReading(oldLastReading);
        loadProfileUpdater.update();
        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(device);
        LoadProfile.LoadProfileUpdater loadProfileUpdater2 = device.getLoadProfileUpdaterFor(reloadedLoadProfile);
        loadProfileUpdater2.setLastReadingIfLater(newLastReading);
        loadProfileUpdater2.update();
        LoadProfile finalReloadedLoadProfile = getReloadedLoadProfile(device);
        assertThat(finalReloadedLoadProfile.getLastReading().isPresent()).isTrue();
        assertThat(finalReloadedLoadProfile.getLastReading().get()).isEqualTo(newLastReading);
    }

    @Test
    @Transactional
    public void updateLastReadingIfNotLaterTest() {
        Device device = createSimpleDeviceWithLoadProfiles();
        Instant oldLastReading = Instant.ofEpochMilli(999999999);
        Instant newLastReading = Instant.ofEpochMilli(123546);
        LoadProfile loadProfile = getReloadedLoadProfile(device);
        LoadProfile.LoadProfileUpdater loadProfileUpdater = device.getLoadProfileUpdaterFor(loadProfile);
        loadProfileUpdater.setLastReading(oldLastReading);
        loadProfileUpdater.update();
        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(device);
        LoadProfile.LoadProfileUpdater loadProfileUpdater2 = device.getLoadProfileUpdaterFor(reloadedLoadProfile);
        loadProfileUpdater2.setLastReadingIfLater(newLastReading);
        loadProfileUpdater2.update();
        LoadProfile finalReloadedLoadProfile = getReloadedLoadProfile(device);
        assertThat(finalReloadedLoadProfile.getLastReading().isPresent()).isTrue();
        assertThat(finalReloadedLoadProfile.getLastReading().get()).isEqualTo(oldLastReading);
    }

    @Test
    @Transactional
    public void channelTest() {
        final Device device = createSimpleDeviceWithLoadProfiles();
        final LoadProfile reloadedLoadProfile = getReloadedLoadProfile(device);
        assertThat(reloadedLoadProfile.getChannels()).has(new Condition<List<? extends Channel>>() {
            @Override
            public boolean matches(List<? extends Channel> value) {
                int count = 0;
                for (Channel channel : value) {
                    if (channel.getRegisterTypeObisCode().equals(obisCode1)) {
                        count |= 0b0001;
                        assertThat(channel.getUnit()).isEqualTo(unit1);
                    }
                    if (channel.getRegisterTypeObisCode().equals(obisCode2)) {
                        count |= 0b0010;
                        assertThat(channel.getUnit()).isEqualTo(unit2);
                    }
                    assertThat(channel.getIntervalInSeconds()).isEqualTo(interval.getSeconds());
                    assertThat(channel.getDevice().getId()).isEqualTo(device.getId());
                    assertThat(channel.getLoadProfile()).isEqualTo(reloadedLoadProfile);
                }
                return count == 0b0011;
            }
        });
    }

    @Test
    @Transactional
    public void verifyLoadProfileIsDeletedAfterDeviceIsDeletedTest() {
        Device device = createSimpleDeviceWithLoadProfiles();

        Device reloadedDevice = getReloadedDevice(device);
        reloadedDevice.delete();

        assertThat(inMemoryPersistence.getDataModel().mapper(LoadProfile.class).find()).isEmpty();
    }

    @Test
    @Transactional
    public void createLoadProfilesWithMultipliedConfiguredChannelsNoMultiplierYetOnDeviceTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithOneChannelConfiguredToMultiply();
        when(inMemoryPersistence.getClock().instant()).thenReturn(januaryFirst);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "createLoadProfilesWithMultipliedConfiguredChannelsNoMultiplierYetOnDeviceTest", MRID, januaryFirst);
        final LoadProfile reloadedLoadProfile = getReloadedLoadProfile(device);
        when(inMemoryPersistence.getClock().instant()).thenReturn(januaryTenth);

        assertThat(reloadedLoadProfile.getChannels()).haveExactly(1, new Condition<Channel>(){
            @Override
            public boolean matches(Channel channel) {
                return channel.getCalculatedReadingType(inMemoryPersistence.getClock().instant()).isPresent()
                        && channel.getCalculatedReadingType(inMemoryPersistence.getClock().instant()).get().getMRID().equals(deltaActiveEnergyPrimary15MinMrid); // we always use the calculated readingtype when the user defined to use it on config level
            }
        });
    }

    @Test
    @Transactional
    public void createLoadProfilesWithMultipliedConfiguredChannelsAndMultiplierOnDeviceTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithOneChannelConfiguredToMultiply();
        when(inMemoryPersistence.getClock().instant()).thenReturn(januaryFirst);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "createLoadProfilesWithMultipliedConfiguredChannelsAndMultiplierOnDeviceTest", MRID, januaryFirst);
        device.setMultiplier(BigDecimal.valueOf(7L));
        device.save();
        final LoadProfile reloadedLoadProfile = getReloadedLoadProfile(device);

        when(inMemoryPersistence.getClock().instant()).thenReturn(januaryTenth);

        assertThat(reloadedLoadProfile.getChannels()).haveExactly(1, new Condition<Channel>(){
            @Override
            public boolean matches(Channel channel) {
                return channel.getCalculatedReadingType(inMemoryPersistence.getClock().instant()).isPresent()
                        && channel.getCalculatedReadingType(inMemoryPersistence.getClock().instant()).get().getMRID().equals(deltaActiveEnergyPrimary15MinMrid);
            }
        });
    }

    @Test
    @Transactional
    public void getMultiplierOfChannelWhenNoMultiplierOnDeviceTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithOneChannelConfiguredToMultiply();

        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "getMultiplierOfChannelWhenNoMultiplierOnDeviceTest", MRID, januaryFirst);
        final LoadProfile reloadedLoadProfile = getReloadedLoadProfile(device);

        assertThat(reloadedLoadProfile.getChannels()).haveExactly(1, new Condition<Channel>(){
            @Override
            public boolean matches(Channel channel) {
                return !channel.getMultiplier(januaryFirst).isPresent();
            }
        });
    }

    @Test
    @Transactional
    public void getMultiplierOfChannelWhenAMultiplierIsConfiguredOnDeviceTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithOneChannelConfiguredToMultiply();

        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "getMultiplierOfChannelWhenAMultiplierIsConfiguredOnDeviceTest", MRID, januaryFirst);

        BigDecimal multiplier = BigDecimal.valueOf(13L);
        device.setMultiplier(multiplier);
        device.save();

        final LoadProfile reloadedLoadProfile = getReloadedLoadProfile(device);

        assertThat(reloadedLoadProfile.getChannels()).haveExactly(1, new Condition<Channel>(){
            @Override
            public boolean matches(Channel channel) {
                return channel.getMultiplier(januaryTenth).isPresent() && channel.getMultiplier(januaryTenth).get().compareTo(multiplier) == 0;
            }
        });
        assertThat(reloadedLoadProfile.getChannels()).haveExactly(1, new Condition<Channel>(){
            @Override
            public boolean matches(Channel channel) {
                return !channel.getMultiplier(januaryTenth).isPresent();
            }
        });
    }

    private DeviceConfiguration createDeviceConfigWithOneChannelConfiguredToMultiply() {
        LoadProfileType lpWithMultipliedChannels = inMemoryPersistence.getMasterDataService().newLoadProfileType("LPWithMultipliedChannels", ObisCode.fromString("1.1.1.1.1.1"), interval, Arrays.asList(registerType1, registerType2));
        ChannelType channelTypeForRegisterType1 = lpWithMultipliedChannels.findChannelType(registerType1).get();
        ChannelType channelTypeForRegisterType2 = lpWithMultipliedChannels.findChannelType(registerType2).get();
        lpWithMultipliedChannels.save();
        deviceType.addLoadProfileType(lpWithMultipliedChannels);
        DeviceType.DeviceConfigurationBuilder configWithMultipliedChannelBuilder = deviceType.newConfiguration("ConfigWithMultipliedChannel");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configWithMultipliedChannelBuilder.newLoadProfileSpec(lpWithMultipliedChannels);
        configWithMultipliedChannelBuilder.newChannelSpec(channelTypeForRegisterType1, loadProfileSpecBuilder).overflow(overflow).nbrOfFractionDigits(2).useMultiplierWithCalculatedReadingType(rt_deltaActiveEnergyPrimary15Min);
        configWithMultipliedChannelBuilder.newChannelSpec(channelTypeForRegisterType2, loadProfileSpecBuilder).overflow(overflow).nbrOfFractionDigits(2);
        DeviceConfiguration deviceConfiguration = configWithMultipliedChannelBuilder.add();
        deviceConfiguration.activate();
        return deviceConfiguration;
    }

}