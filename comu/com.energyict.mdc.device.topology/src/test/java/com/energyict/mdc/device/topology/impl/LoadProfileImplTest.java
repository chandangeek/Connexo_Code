package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
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

import java.util.Arrays;
import java.util.Optional;

import org.junit.*;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test aspects of {@link com.energyict.mdc.device.data.LoadProfile}s that rely on the device topology.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-09 (15:09)
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

    private LoadProfile getReloadedLoadProfile(Device device) {
        Device reloadedDevice = getReloadedDevice(device);
        return reloadedDevice.getLoadProfiles().get(0);
    }

    @Test
    @Transactional
    public void isVirtualLoadProfileTest() {
        Device masterWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles", "dwl");
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
        configurationWithLoadProfileAndChannel.newChannelSpec(channelTypeForRegisterType1, phenomenon1, loadProfileSpecBuilder);
        configurationWithLoadProfileAndChannel.newChannelSpec(channelTypeForRegisterType2, phenomenon2, loadProfileSpecBuilder);
        DeviceConfiguration deviceConfiguration = configurationWithLoadProfileAndChannel.add();
        slaveDeviceType.save();
        deviceConfiguration.activate();

        Device slaveWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "slave", MRID);
        inMemoryPersistence.getTopologyService().setPhysicalGateway(slaveWithLoadProfile, masterWithLoadProfile);
        slaveWithLoadProfile.save();
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
        Device masterWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithLoadProfileAndChannels, "DeviceWithLoadProfiles", "M");
        masterWithLoadProfile.save();

        LoadProfileType slaveLoadProfileType = inMemoryPersistence.getMasterDataService().newLoadProfileType("SlaveType", ObisCode.fromString("0.x.24.3.0.255"), interval);
        ChannelType channelTypeForRegisterType = slaveLoadProfileType.createChannelTypeForRegisterType(registerType1);
        slaveLoadProfileType.save();
        DeviceProtocolPluggableClass slaveDeviceProtocolPluggableClass = createSlaveDeviceProtocol();

        DeviceType slaveDeviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("SlaveDeviceType", slaveDeviceProtocolPluggableClass);
        slaveDeviceType.addLoadProfileType(slaveLoadProfileType);
        DeviceType.DeviceConfigurationBuilder configurationWithLoadProfileAndChannel = slaveDeviceType.newConfiguration("SlaveConfig");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationWithLoadProfileAndChannel.newLoadProfileSpec(slaveLoadProfileType);
        configurationWithLoadProfileAndChannel.newChannelSpec(channelTypeForRegisterType, phenomenon1, loadProfileSpecBuilder);
        DeviceConfiguration deviceConfiguration = configurationWithLoadProfileAndChannel.add();
        slaveDeviceType.save();
        deviceConfiguration.activate();

        Device slaveWithLoadProfile = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "slave", "S");
        inMemoryPersistence.getTopologyService().setPhysicalGateway(slaveWithLoadProfile, masterWithLoadProfile);
        slaveWithLoadProfile.save();

        LoadProfile reloadedLoadProfile = getReloadedLoadProfile(slaveWithLoadProfile);
        assertThat(reloadedLoadProfile.isVirtualLoadProfile()).isFalse();
    }

}