package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;

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
import org.fest.assertions.core.Condition;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.*;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the persistent {@link LoadProfileImpl} component.
 * <p>
 * Copyrights EnergyICT
 * Date: 3/18/14
 * Time: 9:36 AM
 */
public class LoadProfileImplTest extends PersistenceTestWithMockedDeviceProtocol {

    private static final ObisCode loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");
    private static final String MRID = "MyUniqueMRID";
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
    private RegisterType registerType1;
    private RegisterType registerType2;
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
        this.phenomenon1 = this.createPhenomenonIfMissing(this.unit1, LoadProfileImplTest.class.getSimpleName() + "1");
        this.unit2 = Unit.get("MWh");
        this.phenomenon2 = this.createPhenomenonIfMissing(this.unit2, LoadProfileImplTest.class.getSimpleName() + "2");
    }

    private Phenomenon createPhenomenonIfMissing(Unit unit, String name) {
        Optional<Phenomenon> phenomenonByUnit = inMemoryPersistence.getMasterDataService().findPhenomenonByUnit(unit);
        if (!phenomenonByUnit.isPresent()) {
            Phenomenon phenomenon = inMemoryPersistence.getMasterDataService().newPhenomenon(name, unit);
            phenomenon.save();
            return phenomenon;
        } else {
            return phenomenonByUnit.get();
        }
    }

    private DeviceConfiguration createDeviceConfigurationWithLoadProfileSpecAndTwoChannelSpecsSpecs() {
        this.registerType1 = this.createRegisterTypeIfMissing("RegisterType1", this.obisCode1, this.unit1, this.readingType1, 0);
        this.registerType2 = this.createRegisterTypeIfMissing("RegisterType2", this.obisCode2, this.unit2, this.readingType2, 0);
        loadProfileType = inMemoryPersistence.getMasterDataService().newLoadProfileType("LoadProfileType", loadProfileObisCode, interval);
        ChannelType channelTypeForRegisterType1 = loadProfileType.createChannelTypeForRegisterType(registerType1);
        ChannelType channelTypeForRegisterType2 = loadProfileType.createChannelTypeForRegisterType(registerType2);
        loadProfileType.save();
        deviceType.addLoadProfileType(loadProfileType);
        DeviceType.DeviceConfigurationBuilder configurationWithLoadProfileAndChannel = deviceType.newConfiguration("ConfigurationWithLoadProfileAndChannel");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationWithLoadProfileAndChannel.newLoadProfileSpec(loadProfileType);
        configurationWithLoadProfileAndChannel.newChannelSpec(channelTypeForRegisterType1, phenomenon1, loadProfileSpecBuilder);
        configurationWithLoadProfileAndChannel.newChannelSpec(channelTypeForRegisterType2, phenomenon2, loadProfileSpecBuilder);
        DeviceConfiguration deviceConfiguration = configurationWithLoadProfileAndChannel.add();
        deviceType.save();
        deviceConfiguration.activate();
        return deviceConfiguration;
    }

    private RegisterType createRegisterTypeIfMissing(String name, ObisCode obisCode, Unit unit, ReadingType readingType, int timeOfUse) {
        Optional<RegisterType> xRegisterType = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType);
        RegisterType registerType;
        if (xRegisterType.isPresent()) {
            registerType = xRegisterType.get();
        } else {
            registerType = inMemoryPersistence.getMasterDataService().newRegisterType(name, obisCode, unit, readingType, timeOfUse);
            registerType.save();
        }
        return registerType;
    }

    private Device createSimpleDeviceWithLoadProfiles() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceName", MRID);
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
        Device deviceWithoutLoadProfiles = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithoutLoadProfiles", MRID);
        deviceWithoutLoadProfiles.save();

        Device reloadedDevice = getReloadedDevice(deviceWithoutLoadProfiles);
        assertThat(reloadedDevice.getLoadProfiles()).isEmpty();
    }

    @Test
    @Transactional
    public void getChannelsTest() {
        Device deviceWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles", MRID);
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
    public void isNotVirtualLoadProfileTest() {
        Device masterWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles", MRID);
        masterWithLoadProfile.save();
        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(masterWithLoadProfile);
        assertThat(reloadedLoadProfile.isVirtualLoadProfile()).isFalse();
    }

    @Test
    @Transactional
    public void getIntervalTest() {
        Device deviceWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles", MRID);
        deviceWithLoadProfile.save();

        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(deviceWithLoadProfile);
        assertThat(reloadedLoadProfile.getInterval()).isEqualTo(interval);
    }

    @Test
    @Transactional
    public void getDeviceObisCodeTest() {
        Device deviceWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles", MRID);
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
        assertThat(reloadedLoadProfile.getChannels()).has(new Condition<List<Channel>>() {
            @Override
            public boolean matches(List<Channel> value) {
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

}