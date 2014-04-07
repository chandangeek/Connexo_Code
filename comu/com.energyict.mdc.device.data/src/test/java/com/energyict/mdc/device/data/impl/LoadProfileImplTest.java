package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import org.fest.assertions.core.Condition;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the persistent {@link com.energyict.mdc.device.data.impl.LoadProfileImpl} component
 *
 * Copyrights EnergyICT
 * Date: 3/18/14
 * Time: 9:36 AM
 */
public class LoadProfileImplTest extends PersistenceTestWithMockedDeviceProtocol {

    private static final ObisCode loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");
    private final TimeDuration interval = TimeDuration.minutes(15);

    private ReadingType readingType1;
    private ReadingType readingType2;
    private Phenomenon phenomenon1;
    private Phenomenon phenomenon2;
    private ObisCode obisCode1;
    private ObisCode obisCode2;
    private Unit unit1;
    private Unit unit2;
    private DeviceConfiguration deviceConfigurationWithLoadProfileAndChannels;
    private RegisterMapping registerMapping1;
    private RegisterMapping registerMapping2;
    private LoadProfileType loadProfileType;

    @Before
    public void initBefore() {
        this.setupReadingTypes();
        this.setupPhenomena();
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

    private void setupPhenomena() {
        this.unit1 = Unit.get("kWh");
        this.phenomenon1 = inMemoryPersistence.getDeviceConfigurationService().newPhenomenon(DeviceImplTest.class.getSimpleName() + "1", unit1);
        this.phenomenon1.save();
        this.unit2 = Unit.get("MWh");
        this.phenomenon2 = inMemoryPersistence.getDeviceConfigurationService().newPhenomenon(DeviceImplTest.class.getSimpleName() + "2", unit2);
        this.phenomenon2.save();
    }

    private DeviceConfiguration createDeviceConfigurationWithLoadProfileSpecAndTwoChannelSpecsSpecs() {
        registerMapping1 = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping("RegisterMapping1", obisCode1, unit1, readingType1, 0);
        registerMapping1.save();
        registerMapping2 = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping("RegisterMapping2", obisCode2, unit2, readingType2, 0);
        registerMapping2.save();
        loadProfileType = inMemoryPersistence.getDeviceConfigurationService().newLoadProfileType("LoadProfileType", loadProfileObisCode, interval);
        loadProfileType.addRegisterMapping(registerMapping1);
        loadProfileType.addRegisterMapping(registerMapping2);
        loadProfileType.save();
        deviceType.addLoadProfileType(loadProfileType);
        DeviceType.DeviceConfigurationBuilder configurationWithLoadProfileAndChannel = deviceType.newConfiguration("ConfigurationWithLoadProfileAndChannel");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationWithLoadProfileAndChannel.newLoadProfileSpec(loadProfileType);
        configurationWithLoadProfileAndChannel.newChannelSpec(registerMapping1, phenomenon1, loadProfileSpecBuilder);
        configurationWithLoadProfileAndChannel.newChannelSpec(registerMapping2, phenomenon2, loadProfileSpecBuilder);
        DeviceConfiguration deviceConfiguration = configurationWithLoadProfileAndChannel.add();
        deviceType.save();
        return deviceConfiguration;
    }

    private Device createSimpleDeviceWithLoadProfiles() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceName");
        device.save();
        return device;
    }

    private LoadProfile getReloadedLoadProfile(Device device) {
        Device reloadedDevice = getReloadedDevice(device);
        return reloadedDevice.getLoadProfiles().get(0);
    }

    @Test
    @Transactional
    public void createWithNoLoadProfileSpecsTest() {
        Device deviceWithoutLoadProfiles = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithoutLoadProfiles");
        deviceWithoutLoadProfiles.save();

        Device reloadedDevice = getReloadedDevice(deviceWithoutLoadProfiles);
        assertThat(reloadedDevice.getLoadProfiles()).isEmpty();
    }

    @Test
    @Transactional
    public void getChannelsTest() {
        Device deviceWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles");
        deviceWithLoadProfile.save();

        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(deviceWithLoadProfile);
        assertThat(reloadedLoadProfile.getChannels()).hasSize(2);
        assertThat(reloadedLoadProfile.getChannels()).has(new Condition<List<Channel>>() {
            @Override
            public boolean matches(List<Channel> value) {
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
    public void getAllChannelsTestWithoutSlaveDevices() {
        Device deviceWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles");
        deviceWithLoadProfile.save();

        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(deviceWithLoadProfile);
        assertThat(reloadedLoadProfile.getAllChannels()).hasSize(2);
        assertThat(reloadedLoadProfile.getAllChannels()).has(new Condition<List<Channel>>() {
            @Override
            public boolean matches(List<Channel> value) {
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
    public void getAllChannelsTestWithASlaveDeviceTest() {

        final Device masterWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles");
        masterWithLoadProfile.save();
        final Device slave = createSlaveDeviceWithSameLoadProfileType(masterWithLoadProfile);

        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(masterWithLoadProfile);

        assertThat(reloadedLoadProfile.getAllChannels()).hasSize(4);
        assertThat(reloadedLoadProfile.getAllChannels()).has(new Condition<List<Channel>>() {
            @Override
            public boolean matches(List<Channel> value) {
                int masterChannels = 0;
                int slaveChannels = 0;
                int obisCode1Match = 0;
                int obisCode2Match = 0;
                for (Channel channel : value) {
                    if(channel.getDevice().getId() == masterWithLoadProfile.getId()){
                        masterChannels++;
                    }
                    if(channel.getDevice().getId() == slave.getId()){
                        slaveChannels++;
                    }
                    if(channel.getRegisterTypeObisCode().equals(obisCode1)){
                        obisCode1Match++;
                    }
                    if(channel.getRegisterTypeObisCode().equals(obisCode2)){
                        obisCode2Match++;
                    }
                }
                return masterChannels == 2 && slaveChannels == 2 && obisCode1Match == 2 && obisCode2Match == 2;
            }
        });
    }

    @Test
    @Transactional
    public void isNotVirtualLoadProfileTest() {
        Device masterWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles");
        masterWithLoadProfile.save();
        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(masterWithLoadProfile);
        assertThat(reloadedLoadProfile.isVirtualLoadProfile()).isFalse();
    }

    @Test
    @Transactional
    public void isVirtualLoadProfileTest() {
        Device masterWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles");
        masterWithLoadProfile.save();

        Device slave = createSlaveDeviceWithSameLoadProfileType(masterWithLoadProfile);

        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(slave);
        assertThat(reloadedLoadProfile.isVirtualLoadProfile()).isTrue();
    }

    @Test
    @Transactional
    public void isNotVirtualLoadProfileBecauseDeviceProtocolNotLogicalSlaveTest() {
        Device masterWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles");
        masterWithLoadProfile.save();
        Device slaveWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "Slave");
        slaveWithLoadProfile.setPhysicalGateway(masterWithLoadProfile);
        slaveWithLoadProfile.save();

        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(slaveWithLoadProfile);

        assertThat(reloadedLoadProfile.isVirtualLoadProfile()).isFalse();
    }

    private Device createSlaveDeviceWithSameLoadProfileType(Device masterWithLoadProfile) {
        DeviceProtocolPluggableClass slaveDeviceProtocolPluggableClass = createSlaveDeviceProtocol();

        DeviceType slaveDeviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("SlaveDeviceType", slaveDeviceProtocolPluggableClass);
        slaveDeviceType.addLoadProfileType(loadProfileType);
        DeviceType.DeviceConfigurationBuilder configurationWithLoadProfileAndChannel = slaveDeviceType.newConfiguration("SlaveConfig");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationWithLoadProfileAndChannel.newLoadProfileSpec(loadProfileType);
        configurationWithLoadProfileAndChannel.newChannelSpec(registerMapping1, phenomenon1, loadProfileSpecBuilder);
        configurationWithLoadProfileAndChannel.newChannelSpec(registerMapping2, phenomenon2, loadProfileSpecBuilder);
        DeviceConfiguration deviceConfiguration = configurationWithLoadProfileAndChannel.add();
        slaveDeviceType.save();

        Device slaveWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "slave");
        slaveWithLoadProfile.setPhysicalGateway(masterWithLoadProfile);
        slaveWithLoadProfile.save();
        return slaveWithLoadProfile;
    }

    @Test
    @Transactional
    public void isNotVirtualLoadProfileBecauseSlaveHasLoadProfileTypeWithWildCardBFieldTest() {
        Device masterWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles");
        masterWithLoadProfile.save();

        LoadProfileType slaveLoadProfileType = inMemoryPersistence.getDeviceConfigurationService().newLoadProfileType("SlaveType", ObisCode.fromString("0.x.24.3.0.255"), interval);
        slaveLoadProfileType.addRegisterMapping(registerMapping1);
        slaveLoadProfileType.save();
        DeviceProtocolPluggableClass slaveDeviceProtocolPluggableClass = createSlaveDeviceProtocol();

        DeviceType slaveDeviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("SlaveDeviceType", slaveDeviceProtocolPluggableClass);
        slaveDeviceType.addLoadProfileType(slaveLoadProfileType);
        DeviceType.DeviceConfigurationBuilder configurationWithLoadProfileAndChannel = slaveDeviceType.newConfiguration("SlaveConfig");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationWithLoadProfileAndChannel.newLoadProfileSpec(slaveLoadProfileType);
        configurationWithLoadProfileAndChannel.newChannelSpec(registerMapping1, phenomenon1, loadProfileSpecBuilder);
        DeviceConfiguration deviceConfiguration = configurationWithLoadProfileAndChannel.add();
        slaveDeviceType.save();

        Device slaveWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "slave");
        slaveWithLoadProfile.setPhysicalGateway(masterWithLoadProfile);
        slaveWithLoadProfile.save();

        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(slaveWithLoadProfile);
        assertThat(reloadedLoadProfile.isVirtualLoadProfile()).isFalse();
    }

    private DeviceProtocolPluggableClass createSlaveDeviceProtocol() {
        long slavePluggableClassId = 654;
        DeviceProtocol slaveDeviceProtocol = mock(DeviceProtocol.class);
        when(slaveDeviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_SLAVE));
        DeviceProtocolPluggableClass slaveDeviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(slaveDeviceProtocolPluggableClass.getId()).thenReturn(slavePluggableClassId);
        when(slaveDeviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(slaveDeviceProtocol);
        when(inMemoryPersistence.getProtocolPluggableService().findDeviceProtocolPluggableClass(slavePluggableClassId)).thenReturn(slaveDeviceProtocolPluggableClass);
        return slaveDeviceProtocolPluggableClass;
    }

    @Test
    @Transactional
    public void getIntervalTest() {
        Device deviceWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles");
        deviceWithLoadProfile.save();

        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(deviceWithLoadProfile);
        assertThat(reloadedLoadProfile.getInterval()).isEqualTo(interval);
    }

    @Test
    @Transactional
    public void getDeviceObisCodeTest() {
        Device deviceWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles");
        deviceWithLoadProfile.save();

        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(deviceWithLoadProfile);
        assertThat(loadProfileObisCode).isEqualTo(reloadedLoadProfile.getDeviceObisCode());
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
        assertThat(loadProfile.getLastReading()).isNull();
    }

    @Test
    @Transactional
    public void updateLastReadingTest() {
        Device device = createSimpleDeviceWithLoadProfiles();
        Date newLastReading = new Date(123546);
        LoadProfile loadProfile = getReloadedLoadProfile(device);
        LoadProfile.LoadProfileUpdater loadProfileUpdater = device.getLoadProfileUpdaterFor(loadProfile);
        loadProfileUpdater.setLastReading(newLastReading);
        loadProfileUpdater.update();
        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(device);
        assertThat(reloadedLoadProfile.getLastReading()).isEqualTo(newLastReading);
    }

    @Test
    @Transactional
    public void updateLastReadingIfLaterTest() {
        Device device = createSimpleDeviceWithLoadProfiles();
        Date oldLastReading = new Date(123);
        Date newLastReading = new Date(123546);
        LoadProfile loadProfile = getReloadedLoadProfile(device);
        LoadProfile.LoadProfileUpdater loadProfileUpdater = device.getLoadProfileUpdaterFor(loadProfile);
        loadProfileUpdater.setLastReading(oldLastReading);
        loadProfileUpdater.update();
        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(device);
        LoadProfile.LoadProfileUpdater loadProfileUpdater2 = device.getLoadProfileUpdaterFor(reloadedLoadProfile);
        loadProfileUpdater2.setLastReadingIfLater(newLastReading);
        loadProfileUpdater2.update();
        LoadProfile finalReloadedLoadProfile = getReloadedLoadProfile(device);
        assertThat(finalReloadedLoadProfile.getLastReading()).isEqualTo(newLastReading);
    }

    @Test
    @Transactional
    public void updateLastReadingIfNotLaterTest() {
        Device device = createSimpleDeviceWithLoadProfiles();
        Date oldLastReading = new Date(999999999);
        Date newLastReading = new Date(123546);
        LoadProfile loadProfile = getReloadedLoadProfile(device);
        LoadProfile.LoadProfileUpdater loadProfileUpdater = device.getLoadProfileUpdaterFor(loadProfile);
        loadProfileUpdater.setLastReading(oldLastReading);
        loadProfileUpdater.update();
        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(device);
        LoadProfile.LoadProfileUpdater loadProfileUpdater2 = device.getLoadProfileUpdaterFor(reloadedLoadProfile);
        loadProfileUpdater2.setLastReadingIfLater(newLastReading);
        loadProfileUpdater2.update();
        LoadProfile finalReloadedLoadProfile = getReloadedLoadProfile(device);
        assertThat(finalReloadedLoadProfile.getLastReading()).isEqualTo(oldLastReading);
    }

    @Test
    @Transactional
    public void channelTest() {
        final Device device = createSimpleDeviceWithLoadProfiles();
        final LoadProfile reloadedLoadProfile = getReloadedLoadProfile(device);
        assertThat(reloadedLoadProfile.getChannels()).has(new Condition<List<Channel>>() {
            @Override
            public boolean matches(List<Channel> value) {
                int count = 0;
                for (Channel channel : value) {
                    if(channel.getRegisterTypeObisCode().equals(obisCode1)){
                        count |= 0b0001;
                        assertThat(channel.getUnit()).isEqualTo(unit1);
                    }
                    if(channel.getRegisterTypeObisCode().equals(obisCode2)){
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

        assertThat(inMemoryPersistence.getDeviceService().getDataModel().mapper(LoadProfile.class).find()).isEmpty();
    }
}
