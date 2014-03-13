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
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exception.MessageSeeds;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.TimeZone;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.device.data.impl.DeviceImpl} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 05/03/14
 * Time: 13:49
 */
public class DeviceImplTest extends PersistenceTest {

    private static final String DEVICENAME = "deviceName";
    private final TimeZone testDefaultTimeZone = TimeZone.getTimeZone("Canada/East-Saskatchewan");
    private TimeZone actualDefaultTimeZone;

    private ReadingType readingType1;
    private ReadingType readingType2;
    private Phenomenon phenomenon1;
    private Phenomenon phenomenon2;
    private ObisCode obisCode1;
    private ObisCode obisCode2;
    private Unit unit1;
    private Unit unit2;

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Before
    public void saveTheDefaultTimeZone(){
        this.actualDefaultTimeZone = TimeZone.getDefault();
    }

    @Before
    public void setupMasterData() {
        this.setupReadingTypes();
        this.setupPhenomena();
    }

    @After
    public void restoreTheDefaultTimeZone(){
        TimeZone.setDefault(this.actualDefaultTimeZone);
    }

    @After
    public void cleanupDefaultSystemTimeZoneInUseFactoryOnEnvironment(){
        when(Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DefaultSystemTimeZoneFactory.class)).thenReturn(Collections.<DefaultSystemTimeZoneFactory>emptyList());
    }

    private Device createSimpleDevice() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME);
        device.save();
        return device;
    }

    private Device getReloadedDevice(Device device) {
        return inMemoryPersistence.getDeviceService().findDeviceById(device.getId());
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
        this.phenomenon1 = inMemoryPersistence.getDeviceConfigurationService().newPhenomenon(DeviceImplTest.class.getSimpleName()+"1", unit1);
        this.phenomenon1.save();
        this.unit2 = Unit.get("MWh");
        this.phenomenon2 = inMemoryPersistence.getDeviceConfigurationService().newPhenomenon(DeviceImplTest.class.getSimpleName()+"2", unit2);
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, null);
        device.save();
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    public void createWithEmptyNameTest() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "");
        device.save();
    }

    @Test
    @Transactional
    public void createWithSerialNumberTest() {
        String serialNumber = "MyTestSerialNumber";

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME);
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
        Device device1 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME + "First");
        device1.setSerialNumber(serialNumber);
        device1.save();

        Device device2 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME + "Second");
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME + "First");
        device.setExternalName(externalName);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getExternalName()).isEqualTo(externalName);
    }

    @Test
    @Transactional
    public void successfulUpdateWithExternalNameTest() {
        String externalName = "MyUpdatedPublicExternalName";
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME + "First");
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
        Device device1 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME + "First");
        device1.setExternalName(externalName);
        device1.save();
        Device device2 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME + "Second");
        device2.setExternalName(externalName);
        device2.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.DUPLICATE_DEVICE_EXTERNAL_KEY + "}")
    public void uniquenessOfExternalNameAfterUpdateTest() {
        String externalName = "MyPublicExternalName";
        Device device1 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME + "First");
        device1.setExternalName(externalName);
        device1.save();
        Device device2 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME + "Second");
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

        InMemoryPersistence.update("update eisrtu set TIMEZONE = 'InCorrectTimeZoneId' where id = " + simpleDevice.getId());

        Device reloadedDevice = getReloadedDevice(simpleDevice);

        assertThat(this.testDefaultTimeZone).isEqualTo(reloadedDevice.getTimeZone());
    }

    @Test
    @Transactional
    public void userDefinedTimeZoneTest() {
        createTestDefaultTimeZone();
        TimeZone userDefinedTimeZone = TimeZone.getTimeZone("Asia/Novokuznetsk");
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME);
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME);
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

    private DeviceConfiguration createDeviceConfigurationWithTwoRegisterSpecs(){
        RegisterMapping registerMapping1 = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping("RegisterMapping1", obisCode1, unit1, readingType1, 0);
        registerMapping1.save();
        RegisterMapping registerMapping2 = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping("RegisterMapping2", obisCode2, unit2, readingType2, 0);
        registerMapping2.save();
        deviceType.addRegisterMapping(registerMapping1);
        deviceType.addRegisterMapping(registerMapping2);
        DeviceType.DeviceConfigurationBuilder configurationWithRegisterMappings = deviceType.newConfiguration("ConfigurationWithRegisterMappings");
        configurationWithRegisterMappings.newRegisterSpec(registerMapping1);
        configurationWithRegisterMappings.newRegisterSpec(registerMapping2);
        DeviceConfiguration deviceConfiguration = configurationWithRegisterMappings.add();
        deviceType.save();
        return deviceConfiguration;
    }

    @Test
    @Transactional
    public void getRegistersForConfigWithRegistersTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoRegisterSpecs();
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getRegisters()).hasSize(2);
    }

    @Test
    @Transactional
    public void getRegisterWithDeviceObisCodeForConfigWithRegistersTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoRegisterSpecs();
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME);
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

    @Ignore
    @Test
    @Transactional
    public void createWithPhysicalGatewayTest() {
        Device masterDevice = createSimpleDevice();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "Slave");
        device.save();

        // After the add, to the TemporalReference, the all() returns nothing ...

        Device test = getReloadedDevice(device);
        test.setPhysicalGateway(masterDevice);
        test.save();

        Device reloadedDevice = getReloadedDevice(test);

        assertThat(reloadedDevice.getPhysicalGateway()).isNotNull();
        assertThat(reloadedDevice.getPhysicalGateway().getId()).isEqualTo(masterDevice.getId());
    }

    @Ignore
    @Test
    @Transactional
    public void createWithCommunicationGatewayTest() {
        Device communicationMaster = createSimpleDevice();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "Slave");
        device.save();

        Device test = getReloadedDevice(device);
        test.setCommunicationGateway(communicationMaster);
        test.save();

        Device reloadedDevice = getReloadedDevice(test);

        assertThat(reloadedDevice.getCommunicationGateway()).isNotNull();
        assertThat(reloadedDevice.getCommunicationGateway().getId()).isEqualTo(communicationMaster.getId());
    }

}