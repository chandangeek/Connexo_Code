package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.DefaultSystemTimeZoneFactory;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceCacheFactory;
import com.energyict.mdc.device.data.DeviceDependant;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.exceptions.StillGatewayException;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import org.fest.assertions.core.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Fail.fail;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link com.energyict.mdc.device.data.impl.DeviceImpl} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 05/03/14
 * Time: 13:49
 */
public class DeviceImplTest extends PersistenceIntegrationTest {

    private static final String DEVICENAME = "deviceName";
    private static final ObisCode loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");
    private final TimeZone testDefaultTimeZone = TimeZone.getTimeZone("Canada/East-Saskatchewan");
    private final TimeDuration interval = TimeDuration.minutes(15);

    private TimeZone actualDefaultTimeZone;
    private ReadingType readingType1;
    private ReadingType readingType2;
    private Phenomenon phenomenon1;
    private Phenomenon phenomenon2;
    private ObisCode obisCode1;
    private ObisCode obisCode2;
    private Unit unit1;
    private Unit unit2;
    private LoadProfileType loadProfileType;

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Before
    public void saveTheDefaultTimeZone() {
        this.actualDefaultTimeZone = TimeZone.getDefault();
    }

    @Before
    public void setupMasterData() {
        this.setupReadingTypes();
        this.setupPhenomena();
    }

    @After
    public void restoreTheDefaultTimeZone() {
        TimeZone.setDefault(this.actualDefaultTimeZone);
    }

    @After
    public void cleanupDefaultSystemTimeZoneInUseFactoryOnEnvironment() {
        when(Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DefaultSystemTimeZoneFactory.class)).thenReturn(Collections.<DefaultSystemTimeZoneFactory>emptyList());
    }

    private Device createSimpleDevice() {
        return createSimpleDeviceWithName(DEVICENAME);
    }

    private Device createSimpleDeviceWithName(String name) {
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, name);
        device.save();
        return device;
    }

    private void createTestDefaultTimeZone() {
        TimeZone.setDefault(this.testDefaultTimeZone);
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

    @Test
    @Transactional
    public void successfulCreateTest() {
        Device device = createSimpleDevice();

        assertThat(device).isNotNull();
        assertThat(device.getName()).isEqualTo(DEVICENAME);
        assertThat(device.getSerialNumber()).isNullOrEmpty();
    }

    @Test
    @Transactional
    public void successfulReloadTest() {
        Device device = createSimpleDevice();

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice).isNotNull();
        assertThat(reloadedDevice.getName()).isEqualTo(DEVICENAME);
        assertThat(reloadedDevice.getSerialNumber()).isNullOrEmpty();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    public void createWithoutNameTest() {
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, null);
        device.save();
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    public void createWithEmptyNameTest() {
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "");
        device.save();
    }

    @Test
    @Transactional
    public void createWithSerialNumberTest() {
        String serialNumber = "MyTestSerialNumber";

        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME);
        device.setSerialNumber(serialNumber);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getSerialNumber()).isEqualTo(serialNumber);
    }

    @Test
    @Transactional
    public void updateWithSerialNumberTest() {
        String serialNumber = "MyUpdatedSerialNumber";
        Device simpleDevice = createSimpleDevice();

        simpleDevice.setSerialNumber(serialNumber);
        simpleDevice.save();

        Device reloadedDevice = getReloadedDevice(simpleDevice);

        assertThat(reloadedDevice.getSerialNumber()).isEqualTo(serialNumber);
    }

    @Test
    @Transactional
    public void successfulCreationOfTwoDevicesWithSameNameTest() {
        Device device1 = createSimpleDevice();
        Device device2 = createSimpleDevice();

        assertThat(device1).isNotEqualTo(device2);
        assertThat(device1.getName()).isEqualTo(device2.getName());
        assertThat(device1.getId()).isNotEqualTo(device2.getId());
    }

    @Test
    @Transactional
    public void successfulCreationOfTwoDevicesWithSameSerialNumberTest() {
        String serialNumber = "SerialNumber";
        Device device1 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME + "First");
        device1.setSerialNumber(serialNumber);
        device1.save();

        Device device2 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME + "Second");
        device2.setSerialNumber(serialNumber);
        device2.save();

        assertThat(device1).isNotEqualTo(device2);
        assertThat(device1.getSerialNumber()).isEqualTo(device2.getSerialNumber());
        assertThat(device1.getId()).isNotEqualTo(device2.getId());
    }

    @Test
    @Transactional
    public void successfulCreateWithExternalNameTest() {
        String externalName = "MyPublicExternalName";
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME + "First");
        device.setExternalName(externalName);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getExternalName()).isEqualTo(externalName);
    }

    @Test
    @Transactional
    public void successfulUpdateWithExternalNameTest() {
        String externalName = "MyUpdatedPublicExternalName";
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME + "First");
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        reloadedDevice.setExternalName(externalName);
        reloadedDevice.save();

        Device updatedDevice = getReloadedDevice(reloadedDevice);
        assertThat(updatedDevice.getExternalName()).isEqualTo(externalName);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.DUPLICATE_DEVICE_EXTERNAL_KEY + "}")
    public void uniquenessOfExternalNameTest() {
        String externalName = "MyPublicExternalName";
        Device device1 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME + "First");
        device1.setExternalName(externalName);
        device1.save();
        Device device2 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME + "Second");
        device2.setExternalName(externalName);
        device2.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.DUPLICATE_DEVICE_EXTERNAL_KEY + "}")
    public void uniquenessOfExternalNameAfterUpdateTest() {
        String externalName = "MyPublicExternalName";
        Device device1 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME + "First");
        device1.setExternalName(externalName);
        device1.save();
        Device device2 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME + "Second");
        device2.save();
        Device reloadedDevice = getReloadedDevice(device2);
        reloadedDevice.setExternalName(externalName);
        reloadedDevice.save();
    }

    /**
     * This test will get the default TimeZone of the system
     */
    @Test
    @Transactional
    public void defaultTimeZoneTest() {
        createTestDefaultTimeZone();
        Device simpleDevice = createSimpleDevice();

        assertThat(simpleDevice.getTimeZone()).isEqualTo(this.testDefaultTimeZone);
    }

    @Test
    @Transactional
    public void defaultTimeZoneFromTimeZoneInUseTest() {
        createTestDefaultTimeZone();
        TimeZone timeZoneInUseFromApplicationContext = TimeZone.getTimeZone("Indian/Christmas");
        DefaultSystemTimeZoneFactory moduleWithSystemTimeZone = mock(DefaultSystemTimeZoneFactory.class);
        when(moduleWithSystemTimeZone.getDefaultTimeZone()).thenReturn(timeZoneInUseFromApplicationContext);
        when(Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DefaultSystemTimeZoneFactory.class)).thenReturn(Arrays.asList(moduleWithSystemTimeZone));
        Device simpleDevice = createSimpleDevice();

        assertThat(timeZoneInUseFromApplicationContext).isEqualTo(simpleDevice.getTimeZone());
    }

    @Test
    @Transactional
    public void getWithIncorrectTimeZoneIdAndFallBackToSystemTimeZoneTest() {
        createTestDefaultTimeZone();
        Device simpleDevice = createSimpleDevice();

        InMemoryIntegrationPersistence.update("update eisrtu set TIMEZONE = 'InCorrectTimeZoneId' where id = " + simpleDevice.getId());

        Device reloadedDevice = getReloadedDevice(simpleDevice);

        assertThat(this.testDefaultTimeZone).isEqualTo(reloadedDevice.getTimeZone());
    }

    @Test
    @Transactional
    public void userDefinedTimeZoneTest() {
        createTestDefaultTimeZone();
        TimeZone userDefinedTimeZone = TimeZone.getTimeZone("Asia/Novokuznetsk");
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME);
        device.setTimeZone(userDefinedTimeZone);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(userDefinedTimeZone).isEqualTo(reloadedDevice.getTimeZone());
    }

    @Test
    @Transactional
    public void userDefinedNullTimeZoneResultsInDefaultTimeZoneTest() {
        createTestDefaultTimeZone();
        Device device = createSimpleDevice();

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(this.testDefaultTimeZone).isEqualTo(reloadedDevice.getTimeZone());
    }

    @Test
    @Transactional
    public void updateUserDefinedTimeZoneWithNullTimeZoneResultsInDefaultTest() {
        createTestDefaultTimeZone();
        TimeZone userDefinedTimeZone = TimeZone.getTimeZone("Asia/Novokuznetsk");
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME);
        device.setTimeZone(userDefinedTimeZone);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        reloadedDevice.setTimeZone(null);
        reloadedDevice.save();

        Device updatedDevice = getReloadedDevice(reloadedDevice);

        assertThat(this.testDefaultTimeZone).isEqualTo(updatedDevice.getTimeZone());
    }

    @Test
    @Transactional
    public void getRegistersForConfigWithoutRegistersTest() {
        Device simpleDevice = createSimpleDevice();

        assertThat(simpleDevice.getRegisters()).isEmpty();
    }

    @Test
    @Transactional
    public void getRegisterWithDeviceObisCodeForConfigWithoutRegistersTest() {
        Device simpleDevice = createSimpleDevice();

        assertThat(simpleDevice.getRegisterWithDeviceObisCode(ObisCode.fromString("1.0.1.8.0.255"))).isNull();
    }

    private DeviceConfiguration createDeviceConfigurationWithTwoRegisterSpecs() {
        RegisterMapping registerMapping1 = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping("RegisterMapping1", obisCode1, unit1, readingType1, 0);
        registerMapping1.save();
        RegisterMapping registerMapping2 = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping("RegisterMapping2", obisCode2, unit2, readingType2, 0);
        registerMapping2.save();
        deviceType.addRegisterMapping(registerMapping1);
        deviceType.addRegisterMapping(registerMapping2);
        DeviceType.DeviceConfigurationBuilder configurationWithRegisterMappings = deviceType.newConfiguration("ConfigurationWithRegisterMappings");
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder1 = configurationWithRegisterMappings.newRegisterSpec(registerMapping1);
        registerSpecBuilder1.setNumberOfDigits(9);
        RegisterSpec.RegisterSpecBuilder registerSpecBuilder2 = configurationWithRegisterMappings.newRegisterSpec(registerMapping2);
        registerSpecBuilder2.setNumberOfDigits(9);
        DeviceConfiguration deviceConfiguration = configurationWithRegisterMappings.add();
        deviceType.save();
        return deviceConfiguration;
    }

    @Test
    @Transactional
    public void getRegistersForConfigWithRegistersTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoRegisterSpecs();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getRegisters()).hasSize(2);
    }

    @Test
    @Transactional
    public void getRegisterReadingsShouldReturnEmptyListTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoRegisterSpecs();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getRegisterWithDeviceObisCode(obisCode1).getRegisterReadings(Interval.sinceEpoch())).isEmpty();
    }

    @Test
    @Transactional
    public void getRegisterReadingsShouldReturnResultsTest() {
        BigDecimal readingValue = new BigDecimal(5432.32);
        Date readingTimeStamp = new Date(123456789);
        Reading reading = new ReadingImpl(readingType1.getMRID(), readingValue, readingTimeStamp);
        MeterReadingImpl meterReading = new MeterReadingImpl();
        meterReading.addReading(reading);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoRegisterSpecs();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME);
        device.save();
        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        List<ReadingRecord> registerReadings = reloadedDevice.getRegisterWithDeviceObisCode(obisCode1).getRegisterReadings(Interval.sinceEpoch());
        assertThat(registerReadings).isNotEmpty();
        assertThat(registerReadings).hasSize(1);
        assertThat(registerReadings.get(0).getReadingType().getMRID()).isEqualTo(readingType1.getMRID());
        assertThat(registerReadings.get(0).getTimeStamp()).isEqualTo(readingTimeStamp);
        assertThat(registerReadings.get(0).getQuantity(0).getValue()).isEqualTo(readingValue);
    }

    @Test
    @Transactional
    public void getRegisterWithDeviceObisCodeForConfigWithRegistersTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoRegisterSpecs();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getRegisterWithDeviceObisCode(obisCode1)).isNotNull();
        assertThat(reloadedDevice.getRegisterWithDeviceObisCode(obisCode2)).isNotNull();
    }

    @Test
    @Transactional
    public void getDefaultPhysicalGatewayNullTest() {
        Device simpleDevice = createSimpleDevice();

        assertThat(simpleDevice.getPhysicalGateway()).isNull();
    }

    @Test
    @Transactional
    public void createWithPhysicalGatewayTest() {
        Device masterDevice = createSimpleDeviceWithName("Physical_MASTER");

        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Slave");
        device.setPhysicalGateway(masterDevice);
        device.save();
        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getPhysicalGateway()).isNotNull();
        assertThat(reloadedDevice.getPhysicalGateway().getId()).isEqualTo(masterDevice.getId());
    }

    @Test
    @Transactional
    public void updateWithPhysicalGatewayTest() {
        Device masterDevice = createSimpleDeviceWithName("Physical_MASTER");

        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Slave");
        device.save();
        Device reloadedDevice = getReloadedDevice(device);
        reloadedDevice.setPhysicalGateway(masterDevice);
        reloadedDevice.save();

        Device updatedDevice = getReloadedDevice(reloadedDevice);

        assertThat(updatedDevice.getPhysicalGateway()).isNotNull();
        assertThat(updatedDevice.getPhysicalGateway().getId()).isEqualTo(masterDevice.getId());
    }

    @Test
    @Transactional
    public void updateMultipleSlavesWithSameMasterTest() {
        Device masterDevice = createSimpleDeviceWithName("Physical_MASTER");
        Device slaveDevice1 = createSimpleDeviceWithName("SLAVE_1");
        Device slaveDevice2 = createSimpleDeviceWithName("SLAVE_2");

        slaveDevice1.setPhysicalGateway(masterDevice);
        slaveDevice1.save();
        slaveDevice2.setPhysicalGateway(masterDevice);
        slaveDevice2.save();

        Device reloadedSlave1 = getReloadedDevice(slaveDevice1);
        Device reloadedSlave2 = getReloadedDevice(slaveDevice2);

        assertThat(reloadedSlave1.getPhysicalGateway().getId()).isEqualTo(reloadedSlave2.getPhysicalGateway().getId()).isEqualTo(masterDevice.getId());
    }

    @Test
    @Transactional
    public void updateWithSecondMasterDeviceTest() {
        Device masterDevice1 = createSimpleDeviceWithName("Physical_MASTER_1");
        Device masterDevice2 = createSimpleDeviceWithName("Physical_MASTER_2");
        Device origin = createSimpleDeviceWithName("Origin");

        origin.setPhysicalGateway(masterDevice1);
        origin.save();

        Device slaveWithMaster1 = getReloadedDevice(origin);
        slaveWithMaster1.setPhysicalGateway(masterDevice2);
        slaveWithMaster1.save();

        Device slaveWithMaster2 = getReloadedDevice(slaveWithMaster1);

        assertThat(slaveWithMaster2.getPhysicalGateway().getId()).isEqualTo(masterDevice2.getId());
    }

    @Test
    @Transactional
    public void removePhysicalGatewayTest() {
        Device masterDevice = createSimpleDeviceWithName("Physical_MASTER");
        Device slaveDevice1 = createSimpleDeviceWithName("SLAVE_1");
        slaveDevice1.setPhysicalGateway(masterDevice);
        slaveDevice1.save();

        Device updatedSlave = getReloadedDevice(slaveDevice1);
        updatedSlave.clearPhysicalGateway();
        updatedSlave.save();

        Device slaveWithNoMaster = getReloadedDevice(updatedSlave);
        assertThat(slaveWithNoMaster.getPhysicalGateway()).isNull();
    }

    @Test
    @Transactional
    public void clearPhysicalGatewayWhenThereIsNoGatewayTest() {
        Device origin = createSimpleDeviceWithName("Origin");
        origin.clearPhysicalGateway();
        // no exception should be thrown
        assertThat(getReloadedDevice(origin).getPhysicalGateway()).isNull();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.GATEWAY_CANT_BE_SAME_AS_ORIGIN_KEY + "}")
    public void setPhysicalGatewaySameAsOriginDeviceTest() {
        Device origin = createSimpleDeviceWithName("Origin");

        origin.setPhysicalGateway(origin);
        origin.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.GATEWAY_CANT_BE_SAME_AS_ORIGIN_KEY + "}")
    public void updatePhysicalGatewayWithSameAsOriginDeviceTest() {
        Device physicalGateway = createSimpleDeviceWithName("PhysicalGateway");
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Slave");
        device.setPhysicalGateway(physicalGateway);
        device.save();
        Device reloadedDevice = getReloadedDevice(device);

        reloadedDevice.setPhysicalGateway(reloadedDevice);
        reloadedDevice.save();
    }

    @Test
    @Transactional
    public void defaultCommunicationGatewayNullTest() {
        Device simpleDevice = createSimpleDevice();

        assertThat(simpleDevice.getCommunicationGateway()).isNull();
    }

    @Test
    @Transactional
    public void createWithCommunicationGatewayTest() {
        Device communicationMaster = createSimpleDevice();

        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Slave");
        device.setCommunicationGateway(communicationMaster);
        device.save();
        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getCommunicationGateway()).isNotNull();
        assertThat(reloadedDevice.getCommunicationGateway().getId()).isEqualTo(communicationMaster.getId());
    }

    @Test
    @Transactional
    public void updateWithCommunicationGatewayTest() {
        Device communicationGateway = createSimpleDeviceWithName("CommunicationGateway");
        Device origin = createSimpleDeviceWithName("Origin");
        Device reloadedOrigin = getReloadedDevice(origin);
        reloadedOrigin.setCommunicationGateway(communicationGateway);
        reloadedOrigin.save();

        Device updatedDevice = getReloadedDevice(reloadedOrigin);

        assertThat(updatedDevice.getCommunicationGateway()).isNotNull();
        assertThat(updatedDevice.getCommunicationGateway().getId()).isEqualTo(communicationGateway.getId());
    }

    @Test
    @Transactional
    public void updateMultipleOriginsWithSameCommunicationGatewayTest() {
        Device masterDevice = createSimpleDeviceWithName("Physical_MASTER");
        Device slaveDevice1 = createSimpleDeviceWithName("SLAVE_1");
        Device slaveDevice2 = createSimpleDeviceWithName("SLAVE_2");

        slaveDevice1.setCommunicationGateway(masterDevice);
        slaveDevice1.save();
        slaveDevice2.setCommunicationGateway(masterDevice);
        slaveDevice2.save();

        Device reloadedSlave1 = getReloadedDevice(slaveDevice1);
        Device reloadedSlave2 = getReloadedDevice(slaveDevice2);

        assertThat(reloadedSlave1.getCommunicationGateway().getId()).isEqualTo(reloadedSlave2.getCommunicationGateway().getId()).isEqualTo(masterDevice.getId());
    }

    @Test
    @Transactional
    public void removeCommunicationGatewayTest() {
        Device communicationMaster = createSimpleDeviceWithName("CommunicationMaster");
        Device origin = createSimpleDeviceWithName("Origin");
        origin.setCommunicationGateway(communicationMaster);
        origin.save();

        Device originWithMaster = getReloadedDevice(origin);
        originWithMaster.clearCommunicationGateway();
        originWithMaster.save();

        Device originWithoutMaster = getReloadedDevice(originWithMaster);

        assertThat(originWithoutMaster.getCommunicationGateway()).isNull();
    }

    @Test
    @Transactional
    public void updateWithSecondCommunicationGatewayTest() {
        Device communicationMaster1 = createSimpleDeviceWithName("CommunicationMaster1");
        Device communicationMaster2 = createSimpleDeviceWithName("CommunicationMaster2");
        Device origin = createSimpleDeviceWithName("Origin");

        origin.setCommunicationGateway(communicationMaster1);
        origin.save();

        Device originWithMaster1 = getReloadedDevice(origin);
        originWithMaster1.setCommunicationGateway(communicationMaster2);

        Device originWithMaster2 = getReloadedDevice(originWithMaster1);

        assertThat(originWithMaster2.getCommunicationGateway().getId()).isEqualTo(communicationMaster2.getId());
    }

    @Test
    @Transactional
    public void clearCommunicationGatewayWhenThereIsNoGatewayTest() {
        Device originWithoutCommunicationGateway = createSimpleDevice();
        originWithoutCommunicationGateway.clearCommunicationGateway();
        // no exception should be thrown
        assertThat(getReloadedDevice(originWithoutCommunicationGateway).getCommunicationGateway()).isNull();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.GATEWAY_CANT_BE_SAME_AS_ORIGIN_KEY + "}")
    public void setCommunicationGatewaySameAsOriginTest() {
        Device origin = createSimpleDeviceWithName("Origin");

        origin.setCommunicationGateway(origin);
        origin.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.GATEWAY_CANT_BE_SAME_AS_ORIGIN_KEY + "}")
    public void updateCommunicationGatewayWithSameAsOriginDeviceTest() {
        Device communicationMaster = createSimpleDevice();

        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Slave");
        device.setCommunicationGateway(communicationMaster);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        reloadedDevice.setCommunicationGateway(reloadedDevice);
    }

    @Test
    @Transactional
    public void createWithSamePhysicalAndCommunicationGatewayTest() {
        Device gatewayForBoth = createSimpleDeviceWithName("GatewayForBoth");
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin");
        device.setPhysicalGateway(gatewayForBoth);
        device.setCommunicationGateway(gatewayForBoth);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getPhysicalGateway().getId()).isEqualTo(reloadedDevice.getCommunicationGateway().getId()).isEqualTo(gatewayForBoth.getId());
    }

    @Test
    @Transactional
    public void findDownstreamDevicesWhenNoneArePresentTest() {
        Device device = createSimpleDevice();

        assertThat(device.getPhysicalConnectedDevices()).isEmpty();
    }

    @Test
    @Transactional
    public void findPhysicalConnectedDevicesTest() {
        Device physicalMaster = createSimpleDeviceWithName("PhysicalMaster");
        final Device device1 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin1");
        device1.setPhysicalGateway(physicalMaster);
        device1.save();
        final Device device2 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin2");
        device2.setPhysicalGateway(physicalMaster);
        device2.save();

        List<BaseDevice<Channel, LoadProfile, Register>> downstreamDevices = physicalMaster.getPhysicalConnectedDevices();

        assertThat(downstreamDevices).hasSize(2);
        assertThat(downstreamDevices).has(new Condition<List<BaseDevice<Channel, LoadProfile, Register>>>() {
            @Override
            public boolean matches(List<BaseDevice<Channel, LoadProfile, Register>> value) {
                boolean bothMatch = true;
                for (BaseDevice baseDevice : value) {
                    bothMatch &= ((baseDevice.getId() == device1.getId()) || (baseDevice.getId() == device2.getId()));
                }
                return bothMatch;
            }
        });
    }

    @Test
    @Transactional
    public void findDownstreamDevicesAfterRemovingGatewayReferenceTest() {
        Device physicalMaster = createSimpleDeviceWithName("PhysicalMaster");
        final Device device1 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin1");
        device1.setPhysicalGateway(physicalMaster);
        device1.save();
        final Device device2 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin2");
        device2.setPhysicalGateway(physicalMaster);
        device2.save();

        //business method
        device1.clearPhysicalGateway();
        device1.save();

        List<BaseDevice<Channel, LoadProfile, Register>> downstreamDevices = physicalMaster.getPhysicalConnectedDevices();

        assertThat(downstreamDevices).hasSize(1);
        assertThat(downstreamDevices.get(0).getId()).isEqualTo(device2.getId());
    }

    @Test
    @Transactional
    public void findDownstreamDevicesAfterRemovalOfOneTest() {
        Device physicalMaster = createSimpleDeviceWithName("PhysicalMaster");
        final Device device1 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin1");
        device1.setPhysicalGateway(physicalMaster);
        device1.save();
        final Device device2 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin2");
        device2.setPhysicalGateway(physicalMaster);
        device2.save();

        //business method
        device1.delete();

        List<BaseDevice<Channel, LoadProfile, Register>> downstreamDevices = physicalMaster.getPhysicalConnectedDevices();

        assertThat(downstreamDevices).hasSize(1);
        assertThat(downstreamDevices.get(0).getId()).isEqualTo(device2.getId());
    }

    @Test
    @Transactional
    public void findDownstreamDevicesAfterSettingToOtherPhysicalGatewayTest() {
        Device physicalMaster = createSimpleDeviceWithName("PhysicalMaster");
        Device otherPhysicalMaster = createSimpleDeviceWithName("OtherPhysicalMaster");
        final Device device1 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin1");
        device1.setPhysicalGateway(physicalMaster);
        device1.save();
        final Device device2 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin2");
        device2.setPhysicalGateway(physicalMaster);
        device2.save();

        //business method
        device1.setPhysicalGateway(otherPhysicalMaster);
        device1.save();


        List<BaseDevice<Channel, LoadProfile, Register>> downstreamDevices = physicalMaster.getPhysicalConnectedDevices();

        assertThat(downstreamDevices).hasSize(1);
        assertThat(downstreamDevices.get(0).getId()).isEqualTo(device2.getId());
    }

    @Test(expected = StillGatewayException.class)
    @Transactional
    public void cannotDeleteBecauseStillUsedAsPhysicalGatewayTest() {
        Device physicalMaster = createSimpleDeviceWithName("PhysicalMaster");
        Device device1 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin1");
        device1.setPhysicalGateway(physicalMaster);
        device1.save();

        //business method
        try {
            physicalMaster.delete();
        } catch (StillGatewayException e) {
            if (!e.getMessageSeed().equals(MessageSeeds.DEVICE_IS_STILL_LINKED_AS_PHYSICAL_GATEWAY)) {
                fail("Should have gotten an exception indicating that the device was still linked as a physical gateway, but was " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    @Test
    @Transactional
    public void deletePhysicalMasterAfterDeletingSlaveTest() {
        Device physicalMaster = createSimpleDeviceWithName("PhysicalMaster");
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin");
        device.setPhysicalGateway(physicalMaster);
        device.save();

        Device reloadedSlave = getReloadedDevice(device);
        reloadedSlave.delete();

        Device reloadedMaster = getReloadedDevice(physicalMaster);
        long masterId = reloadedMaster.getId();
        reloadedMaster.delete();

        assertThat(inMemoryPersistence.getDeviceDataService().findDeviceById(masterId)).isNull();
    }

    @Test(expected = StillGatewayException.class)
    @Transactional
    public void cannotDeleteBecauseStillUsedAsCommunicationGatewayTest() {
        Device communicationMaster = createSimpleDeviceWithName("CommunicationMaster");
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin1");
        device.setCommunicationGateway(communicationMaster);
        device.save();

        //business method
        try {
            communicationMaster.delete();
        } catch (StillGatewayException e) {
            if (!e.getMessageSeed().equals(MessageSeeds.DEVICE_IS_STILL_LINKED_AS_COMMUNICATION_GATEWAY)) {
                fail("Should have gotten an exception indicating that the device was still linked as a physical gateway, but was " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    @Test
    @Transactional
    public void deleteCommunicationMasterAfterDeletingSlaveTest() {
        Device communicationMaster = createSimpleDeviceWithName("CommunicationMaster");
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin1");
        device.setCommunicationGateway(communicationMaster);
        device.save();

        Device reloadedSlave = getReloadedDevice(device);
        reloadedSlave.delete();

        Device reloadedCommunicationMaster = getReloadedDevice(communicationMaster);
        long masterId = reloadedCommunicationMaster.getId();
        reloadedCommunicationMaster.delete();

        assertThat(inMemoryPersistence.getDeviceDataService().findDeviceById(masterId)).isNull();
    }

    @Test
    @Transactional
    public void noDeviceCacheAfterDeviceDeleteTest() {
        DeviceCacheFactory deviceCacheFactory = mock(DeviceCacheFactory.class);
        when(Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DeviceCacheFactory.class)).thenReturn(Arrays.asList(deviceCacheFactory));
        Device simpleDevice = createSimpleDevice();
        long deviceId = simpleDevice.getId();
        simpleDevice.delete();

        verify(deviceCacheFactory).removeDeviceCacheFor(deviceId);
    }

    @Test
    @Transactional
    public void getChannelsForConfigWithNoChannelSpecsTest() {
        Device simpleDevice = createSimpleDevice();

        assertThat(simpleDevice.getChannels()).isEmpty();
    }


    @Test
    @Transactional
    public void createDeviceWithTwoChannelsTest() {
        DeviceConfiguration deviceConfigurationWithTwoChannelSpecs = createDeviceConfigurationWithTwoChannelSpecs();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithTwoChannelSpecs, "DeviceWithChannels");
        device.save();
        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getChannels()).isNotEmpty();
        assertThat(reloadedDevice.getChannels()).hasSize(2);
    }

    @Test
    @Transactional
    public void getChannelWithExistingNameTest() {
        DeviceConfiguration deviceConfigurationWithTwoChannelSpecs = createDeviceConfigurationWithTwoChannelSpecs();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithTwoChannelSpecs, "DeviceWithChannels");
        device.save();
        Device reloadedDevice = getReloadedDevice(device);

        BaseChannel channel = reloadedDevice.getChannel("RegisterMapping1");
        assertThat(channel).isNotNull();
        assertThat(channel.getRegisterTypeObisCode()).isEqualTo(obisCode1);
    }

    @Test
    @Transactional
    public void getChannelWithNonExistingNameTest() {
        DeviceConfiguration deviceConfigurationWithTwoChannelSpecs = createDeviceConfigurationWithTwoChannelSpecs();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithTwoChannelSpecs, "DeviceWithChannels");
        device.save();
        Device reloadedDevice = getReloadedDevice(device);

        BaseChannel channel = reloadedDevice.getChannel("IamJustASpiritChannel");
        assertThat(channel).isNull();
    }

    private DeviceConfiguration createDeviceConfigurationWithTwoChannelSpecs() {
        RegisterMapping registerMapping1 = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping("RegisterMapping1", obisCode1, unit1, readingType1, 0);
        registerMapping1.save();
        RegisterMapping registerMapping2 = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping("RegisterMapping2", obisCode2, unit2, readingType2, 0);
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

    @Test
    @Transactional
    public void deviceNotifiesDependentPartiesWhenDeletingTest() {
        DeviceDependant deviceDependant = mock(DeviceDependant.class);
        when(Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DeviceDependant.class)).thenReturn(Arrays.asList(deviceDependant));
        Device simpleDevice = createSimpleDevice();
        simpleDevice.delete();

        verify(deviceDependant).notifyDeviceDelete(simpleDevice);
    }

}