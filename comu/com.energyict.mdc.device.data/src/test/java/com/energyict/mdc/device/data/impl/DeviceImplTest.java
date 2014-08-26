package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.BillingReading;
import com.energyict.mdc.device.data.DefaultSystemTimeZoneFactory;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.exceptions.CannotDeleteComScheduleFromDevice;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.exceptions.StillGatewayException;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.ComScheduleBuilder;
import com.google.common.base.Optional;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.fest.assertions.core.Condition;
import org.joda.time.DateTimeConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Fail.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.device.data.impl.DeviceImpl} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 05/03/14
 * Time: 13:49
 */
public class DeviceImplTest extends PersistenceIntegrationTest {

    private static final String DEVICENAME = "deviceName";
    private static final String MRID = "MyUniqueMRID";
    private static final ObisCode loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");
    private final TimeZone testDefaultTimeZone = TimeZone.getTimeZone("Canada/East-Saskatchewan");
    private final TimeDuration interval = TimeDuration.minutes(15);

    private TimeZone actualDefaultTimeZone;
    private ReadingType forwardEnergyReadingType;
    private ReadingType reverseEnergyReadingType;
    private String averageForwardEnergyReadingTypeMRID;
    private ObisCode averageForwardEnergyObisCode;
    private Phenomenon phenomenon1;
    private Phenomenon phenomenon2;
    private ObisCode forwardEnergyObisCode;
    private ObisCode reverseEnergyObisCode;
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

    private Device createSimpleDeviceWithName(String name, String mRID){
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, name, mRID);
        device.save();
        return device;
    }

    private Device createSimpleDeviceWithName(String name) {
        return createSimpleDeviceWithName(name, "SimpleMrId");
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
        this.forwardEnergyReadingType = inMemoryPersistence.getMeteringService().getReadingType(code).get();
        this.forwardEnergyObisCode = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeInformationFor(forwardEnergyReadingType).getObisCode();
        String code2 = ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.REVERSE)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
        this.reverseEnergyReadingType = inMemoryPersistence.getMeteringService().getReadingType(code2).get();
        this.reverseEnergyObisCode = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeInformationFor(reverseEnergyReadingType).getObisCode();
        this.averageForwardEnergyReadingTypeMRID = ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .aggregate(Aggregate.AVERAGE).period(MacroPeriod.DAILY)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
        this.averageForwardEnergyObisCode = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeInformationFor(this.averageForwardEnergyReadingTypeMRID).getObisCode();
    }

    private void setupPhenomena() {
        this.unit1 = Unit.get("kWh");
        this.phenomenon1 = this.createPhenomenonIfMissing(this.unit1, DeviceImplTest.class.getSimpleName() + "1");
        this.unit2 = Unit.get("MWh");
        this.phenomenon2 = this.createPhenomenonIfMissing(this.unit2, DeviceImplTest.class.getSimpleName() + "2");
    }

    private Phenomenon createPhenomenonIfMissing(Unit unit, String name) {
        Optional<Phenomenon> phenomenonByUnit = inMemoryPersistence.getMasterDataService().findPhenomenonByUnit(unit);
        if (!phenomenonByUnit.isPresent()) {
            Phenomenon phenomenon = inMemoryPersistence.getMasterDataService().newPhenomenon(name, unit);
            phenomenon.save();
            return phenomenon;
        }
        else {
            return phenomenonByUnit.get();
        }
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
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_REQUIRED_KEY + "}")
    public void createWithoutNameTest() {
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, null, MRID);
        device.save();
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_REQUIRED_KEY + "}")
    public void createWithEmptyNameTest() {
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "", MRID);
        device.save();
    }

    @Test
    @Transactional
    public void createWithSerialNumberTest() {
        String serialNumber = "MyTestSerialNumber";

        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME, MRID);
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
        Device device1 = createSimpleDeviceWithName("MyName", "1");
        Device device2 = createSimpleDeviceWithName("MyName", "2");

        assertThat(device1).isNotEqualTo(device2);
        assertThat(device1.getName()).isEqualTo(device2.getName());
        assertThat(device1.getId()).isNotEqualTo(device2.getId());
    }

    @Test
    @Transactional
    public void successfulCreationOfTwoDevicesWithSameSerialNumberTest() {
        String serialNumber = "SerialNumber";
        Device device1 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME + "MRIDFirst", MRID + "First");
        device1.setSerialNumber(serialNumber);
        device1.save();

        Device device2 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME + "MRIDSecond", MRID + "Second");
        device2.setSerialNumber(serialNumber);
        device2.save();

        assertThat(device1).isNotEqualTo(device2);
        assertThat(device1.getSerialNumber()).isEqualTo(device2.getSerialNumber());
        assertThat(device1.getId()).isNotEqualTo(device2.getId());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DUPLICATE_DEVICE_MRID + "}")
    public void uniquenessOfExternalNameTest() {
        String mRID = "MyPublicExternalName";
        Device device1 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME + "First", mRID);
        device1.save();
        Device device2 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME + "Second", mRID);
        device2.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MRID_REQUIRED_KEY + "}")
    public void noMRIDTest() {
        Device device1 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME, null);
        device1.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MRID_REQUIRED_KEY + "}")
    public void emptyMRIDTest() {
        Device device1 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME, "");
        device1.save();
    }

    @Test
    @Transactional
    public void getMridTest() {
        String mRID = "Bananas";
        Device device = createSimpleDeviceWithName("MyName", mRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getmRID()).isEqualTo(mRID);
    }

    @Test
    @Transactional
    public void canReuseMridAfterDeviceDeletionTest() {
        String mRID = "Strawberries";
        Device device = createSimpleDeviceWithName("MyName", mRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        reloadedDevice.delete();

        Device newDevice = createSimpleDeviceWithName("NewDevice", mRID);
        Device lastDevice = getReloadedDevice(newDevice);
        assertThat(lastDevice.getmRID()).isEqualTo(mRID);
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

        InMemoryIntegrationPersistence.update("update ddc_device set TIMEZONE = 'InCorrectTimeZoneId' where id = " + simpleDevice.getId());

        Device reloadedDevice = getReloadedDevice(simpleDevice);

        assertThat(this.testDefaultTimeZone).isEqualTo(reloadedDevice.getTimeZone());
    }

    @Test
    @Transactional
    public void userDefinedTimeZoneTest() {
        createTestDefaultTimeZone();
        TimeZone userDefinedTimeZone = TimeZone.getTimeZone("Asia/Novokuznetsk");
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME, MRID);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME, MRID);
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

    @Test
    @Transactional
    public void getRegistersForConfigWithRegistersTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoRegisterSpecs();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getRegisters()).hasSize(2);
    }

    @Test
    @Transactional
    public void getRegisterReadingsShouldReturnEmptyListTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoRegisterSpecs();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getRegisterWithDeviceObisCode(forwardEnergyObisCode).getReadings(Interval.sinceEpoch())).isEmpty();
    }

    @Test
    @Transactional
    public void getRegisterReadingsShouldReturnResultsTest() {
        BigDecimal readingValue = new BigDecimal(5432.32);
        Date readingTimeStamp = new Date(123456789);
        com.elster.jupiter.metering.readings.Reading reading = new com.elster.jupiter.metering.readings.beans.ReadingImpl(forwardEnergyReadingType.getMRID(), readingValue, readingTimeStamp);
        MeterReadingImpl meterReading = new MeterReadingImpl();
        meterReading.addReading(reading);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoRegisterSpecs();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        List<Reading> readings = reloadedDevice.getRegisterWithDeviceObisCode(forwardEnergyObisCode).getReadings(Interval.sinceEpoch());
        assertThat(readings).isNotEmpty();
        assertThat(readings).hasSize(1);
        assertThat(readings.get(0)).isInstanceOf(NumericalReading.class);
        NumericalReading numericalReading = (NumericalReading) readings.get(0);
        assertThat(numericalReading.getType().getMRID()).isEqualTo(forwardEnergyReadingType.getMRID());
        assertThat(numericalReading.getTimeStamp()).isEqualTo(readingTimeStamp);
        assertThat(numericalReading.getValue()).isEqualTo(readingValue);
    }

    @Test
    @Transactional
    @Ignore // Todo: wait for fix in MeterReadingStorer to pass profileStatus as second value
    public void getEventRegisterReadings() {
        BigDecimal readingValue = new BigDecimal(5432.32);
        long eventStartUTC = 957225600000L;    // May 1st, 2000 00:00:00 (UTC)
        Date eventStart = new Date(eventStartUTC);
        Date eventEnd = new Date(eventStartUTC + DateTimeConstants.MILLIS_PER_DAY);   // May 2nd, 2000 00:00:00
        Date readingTimeStamp = eventEnd;
        com.elster.jupiter.metering.readings.beans.ReadingImpl reading =
                new com.elster.jupiter.metering.readings.beans.ReadingImpl(this.averageForwardEnergyReadingTypeMRID, readingValue, readingTimeStamp);
        reading.setTimePeriod(eventStart, eventEnd);
        MeterReadingImpl meterReading = new MeterReadingImpl();
        meterReading.addReading(reading);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoRegisterSpecs();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        List<Reading> readings = reloadedDevice.getRegisterWithDeviceObisCode(this.averageForwardEnergyObisCode).getReadings(Interval.sinceEpoch());
        assertThat(readings).isNotEmpty();
        assertThat(readings).hasSize(1);
        assertThat(readings.get(0)).isInstanceOf(BillingReading.class);
        BillingReading billingReading = (BillingReading) readings.get(0);
        assertThat(billingReading.getType().getMRID()).isEqualTo(forwardEnergyReadingType.getMRID());
        assertThat(billingReading.getTimeStamp()).isEqualTo(readingTimeStamp);
        assertThat(billingReading.getInterval().isPresent()).isTrue();
        assertThat(billingReading.getInterval().get()).isEqualTo(new Interval(eventStart, eventEnd));
        assertThat(billingReading.getValue()).isEqualTo(readingValue);
    }

    @Test
    @Transactional
    public void getRegisterWithDeviceObisCodeForConfigWithRegistersTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoRegisterSpecs();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getRegisterWithDeviceObisCode(forwardEnergyObisCode)).isNotNull();
        assertThat(reloadedDevice.getRegisterWithDeviceObisCode(reverseEnergyObisCode)).isNotNull();
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

        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Slave", MRID);
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

        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Slave", MRID);
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
        Device masterDevice = createSimpleDeviceWithName("Physical_MASTER","m");
        Device slaveDevice1 = createSimpleDeviceWithName("SLAVE_1","s1");
        Device slaveDevice2 = createSimpleDeviceWithName("SLAVE_2", "s2");

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
        Device masterDevice1 = createSimpleDeviceWithName("Physical_MASTER_1", "m1");
        Device masterDevice2 = createSimpleDeviceWithName("Physical_MASTER_2", "m2");
        Device origin = createSimpleDeviceWithName("Origin","o");

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
        Device masterDevice = createSimpleDeviceWithName("Physical_MASTER","m");
        Device slaveDevice1 = createSimpleDeviceWithName("SLAVE_1","s");
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
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.GATEWAY_CANT_BE_SAME_AS_ORIGIN_KEY + "}")
    public void setPhysicalGatewaySameAsOriginDeviceTest() {
        Device origin = createSimpleDeviceWithName("Origin");

        origin.setPhysicalGateway(origin);
        origin.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.GATEWAY_CANT_BE_SAME_AS_ORIGIN_KEY + "}")
    public void updatePhysicalGatewayWithSameAsOriginDeviceTest() {
        Device physicalGateway = createSimpleDeviceWithName("PhysicalGateway");
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Slave", "SlaveMrid");
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

        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Slave", MRID);
        device.setCommunicationGateway(communicationMaster);
        device.save();
        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getCommunicationGateway()).isNotNull();
        assertThat(reloadedDevice.getCommunicationGateway().getId()).isEqualTo(communicationMaster.getId());
    }

    @Test
    @Transactional
    public void updateWithCommunicationGatewayTest() {
        Device communicationGateway = createSimpleDeviceWithName("CommunicationGateway","cg");
        Device origin = createSimpleDeviceWithName("Origin","o");
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
        Device masterDevice = createSimpleDeviceWithName("Physical_MASTER","m");
        Device slaveDevice1 = createSimpleDeviceWithName("SLAVE_1","s1");
        Device slaveDevice2 = createSimpleDeviceWithName("SLAVE_2","s2");

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
        Device communicationMaster = createSimpleDeviceWithName("CommunicationMaster","1");
        Device origin = createSimpleDeviceWithName("Origin","2");
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
        Device communicationMaster1 = createSimpleDeviceWithName("CommunicationMaster1", "1");
        Device communicationMaster2 = createSimpleDeviceWithName("CommunicationMaster2", "2");
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
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.GATEWAY_CANT_BE_SAME_AS_ORIGIN_KEY + "}")
    public void setCommunicationGatewaySameAsOriginTest() {
        Device origin = createSimpleDeviceWithName("Origin");

        origin.setCommunicationGateway(origin);
        origin.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.GATEWAY_CANT_BE_SAME_AS_ORIGIN_KEY + "}")
    public void updateCommunicationGatewayWithSameAsOriginDeviceTest() {
        Device communicationMaster = createSimpleDevice();

        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Slave", MRID);
        device.setCommunicationGateway(communicationMaster);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        reloadedDevice.setCommunicationGateway(reloadedDevice);
    }

    @Test
    @Transactional
    public void createWithSamePhysicalAndCommunicationGatewayTest() {
        Device gatewayForBoth = createSimpleDeviceWithName("GatewayForBoth");
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin", MRID);
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
        final Device device1 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin1", MRID);
        device1.setPhysicalGateway(physicalMaster);
        device1.save();
        final Device device2 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin2", MRID+"2");
        device2.setPhysicalGateway(physicalMaster);
        device2.save();

        List<Device> downstreamDevices = physicalMaster.getPhysicalConnectedDevices();

        assertThat(downstreamDevices).hasSize(2);
        assertThat(downstreamDevices).has(new Condition<List<Device>>() {
            @Override
            public boolean matches(List<Device> value) {
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
        final Device device1 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin1", "1");
        device1.setPhysicalGateway(physicalMaster);
        device1.save();
        final Device device2 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin2", "2");
        device2.setPhysicalGateway(physicalMaster);
        device2.save();

        //business method
        device1.clearPhysicalGateway();
        device1.save();

        List<Device> downstreamDevices = physicalMaster.getPhysicalConnectedDevices();

        assertThat(downstreamDevices).hasSize(1);
        assertThat(downstreamDevices.get(0).getId()).isEqualTo(device2.getId());
    }

    @Test
    @Transactional
    public void findDownstreamDevicesAfterRemovalOfOneTest() {
        Device physicalMaster = createSimpleDeviceWithName("PhysicalMaster");
        final Device device1 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin1", "1");
        device1.setPhysicalGateway(physicalMaster);
        device1.save();
        final Device device2 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin2", "2");
        device2.setPhysicalGateway(physicalMaster);
        device2.save();

        //business method
        device1.delete();

        List<Device> downstreamDevices = physicalMaster.getPhysicalConnectedDevices();

        assertThat(downstreamDevices).hasSize(1);
        assertThat(downstreamDevices.get(0).getId()).isEqualTo(device2.getId());
    }

    @Test
    @Transactional
    public void findDownstreamDevicesAfterSettingToOtherPhysicalGatewayTest() {
        Device physicalMaster = createSimpleDeviceWithName("PhysicalMaster","pm");
        Device otherPhysicalMaster = createSimpleDeviceWithName("OtherPhysicalMaster", "opm");
        final Device device1 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin1", "1");
        device1.setPhysicalGateway(physicalMaster);
        device1.save();
        final Device device2 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin2", "2");
        device2.setPhysicalGateway(physicalMaster);
        device2.save();

        //business method
        device1.setPhysicalGateway(otherPhysicalMaster);
        device1.save();


        List<Device> downstreamDevices = physicalMaster.getPhysicalConnectedDevices();

        assertThat(downstreamDevices).hasSize(1);
        assertThat(downstreamDevices.get(0).getId()).isEqualTo(device2.getId());
    }

    @Test(expected = StillGatewayException.class)
    @Transactional
    public void cannotDeleteBecauseStillUsedAsPhysicalGatewayTest() {
        Device physicalMaster = createSimpleDeviceWithName("PhysicalMaster");
        Device device1 = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin1", MRID);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin", MRID);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin1", MRID);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Origin1", MRID);
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
    public void getChannelsForConfigWithNoChannelSpecsTest() {
        Device simpleDevice = createSimpleDevice();

        assertThat(simpleDevice.getChannels()).isEmpty();
    }


    @Test
    @Transactional
    public void createDeviceWithTwoChannelsTest() {
        DeviceConfiguration deviceConfigurationWithTwoChannelSpecs = createDeviceConfigurationWithTwoChannelSpecs();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithTwoChannelSpecs, "DeviceWithChannels", MRID);
        device.save();
        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getChannels()).isNotEmpty();
        assertThat(reloadedDevice.getChannels()).hasSize(2);
    }

    @Test
    @Transactional
    public void getChannelWithExistingNameTest() {
        DeviceConfiguration deviceConfigurationWithTwoChannelSpecs = createDeviceConfigurationWithTwoChannelSpecs();
        List<ChannelSpec> channelSpecs = deviceConfigurationWithTwoChannelSpecs.getChannelSpecs();
        String channelSpecName = "ChannelType1";
        for (ChannelSpec channelSpec : channelSpecs) {
            if (channelSpec.getChannelType().getTemplateRegister().getReadingType().getName().equals(forwardEnergyReadingType.getName())) {
                channelSpecName = channelSpec.getName();
            }
        }

        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithTwoChannelSpecs, "DeviceWithChannels", MRID);
        device.save();
        Device reloadedDevice = getReloadedDevice(device);

        BaseChannel channel = reloadedDevice.getChannel(channelSpecName);
        assertThat(channel).isNotNull();
        assertThat(channel.getRegisterTypeObisCode()).isEqualTo(forwardEnergyObisCode);
    }

    @Test
    @Transactional
    public void getChannelWithNonExistingNameTest() {
        DeviceConfiguration deviceConfigurationWithTwoChannelSpecs = createDeviceConfigurationWithTwoChannelSpecs();

        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithTwoChannelSpecs, "DeviceWithChannels", MRID);
        device.save();
        Device reloadedDevice = getReloadedDevice(device);

        BaseChannel channel = reloadedDevice.getChannel("IamJustASpiritChannel");
        assertThat(channel).isNull();
    }

    @Test(expected = CannotDeleteComScheduleFromDevice.class)
    @Transactional
    public void removeComScheduleThatWasNotAddedToDevice() {
        ComSchedule comSchedule = this.createComSchedule("removeComScheduleThatWasNotAddedToDevice");
        Device simpleDevice = this.createSimpleDevice();

        // Business method
        simpleDevice.removeComSchedule(comSchedule);

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_CONFIGURATION_NOT_ACTIVE + "}")
    public void createWithInActiveDeviceConfigurationTest() {
        DeviceType.DeviceConfigurationBuilder inactiveConfig = deviceType.newConfiguration("Inactie");
        DeviceConfiguration deviceConfiguration = inactiveConfig.add();
        deviceType.save();

        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "MySimpleName", "BlaBla");
        device.save();
    }

    private ComSchedule createComSchedule(String mRIDAndName) {
        ComScheduleBuilder builder = inMemoryPersistence.getSchedulingService().newComSchedule(mRIDAndName, new TemporalExpression(TimeDuration.days(1)), new UtcInstant(clock.now()));
        builder.mrid(mRIDAndName);
        return builder.build();
    }

    @Test
    @Transactional
    public void testGetLoadProfileData() {
        BigDecimal readingValue = new BigDecimal(5432.32);
        Date readingTimeStamp = new Date(1406884422000L); // 8/1/2014, 09:13:42 AM
        Date dayStart = new Date(1406851200000L); // Fri, 01 Aug 2014 00:00:00 GMT
        Date dayEnd = new Date(1406937600000L); // Sat, 02 Aug 2014 00:00:00 GMT
        com.elster.jupiter.metering.readings.Reading reading = new com.elster.jupiter.metering.readings.beans.ReadingImpl(forwardEnergyReadingType.getMRID(), readingValue, readingTimeStamp);
        MeterReadingImpl meterReading = new MeterReadingImpl();
        meterReading.addReading(reading);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        Collection<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(new Interval(dayStart, dayEnd));
        assertThat(readings).hasSize(24 * 4);
    }

    @Test
    @Transactional
    public void testGetLoadProfileDataMidInterval() {
        BigDecimal readingValue = new BigDecimal(5432.32);
        Date readingTimeStamp = new Date(1406884422000L); // 8/1/2014, 09:13:42 AM
        Date dayStart = new Date(1406851800000L); // Fri, 01 Aug 2014 00:10:00 GMT
        Date dayEnd = new Date(1406854200000L); // Sat, 02 Aug 2014 00:50:00 GMT
        com.elster.jupiter.metering.readings.Reading reading = new com.elster.jupiter.metering.readings.beans.ReadingImpl(forwardEnergyReadingType.getMRID(), readingValue, readingTimeStamp);
        MeterReadingImpl meterReading = new MeterReadingImpl();
        meterReading.addReading(reading);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        List<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(new Interval(dayStart, dayEnd));
        assertThat(readings).hasSize(3);
        assertThat(readings.get(1).getInterval().getStart()).isEqualTo(new Date(1406853000000L)); // Fri, 01 Aug 2014 00:30:00 GMT
        assertThat(readings.get(2).getInterval().getStart()).isEqualTo(new Date(1406852100000L)); // Fri, 01 Aug 2014 00:15:00 GMT
    }

    @Test
    @Transactional
    public void testGetLoadProfileDataDST() {
        BigDecimal readingValue = new BigDecimal(5432.32);
        Date readingTimeStamp = new Date(1406884422000L); // 8/1/2014, 09:13:42 AM
        Date dayStart = new Date(1396137600000L); // 3/30/2014, 1:00:00 AM
        Date dayEnd = new Date(1396159200000L); // 3/30/2014, 8:00:00 AM !! 6 hours later!!
        com.elster.jupiter.metering.readings.Reading reading = new com.elster.jupiter.metering.readings.beans.ReadingImpl(forwardEnergyReadingType.getMRID(), readingValue, readingTimeStamp);
        MeterReadingImpl meterReading = new MeterReadingImpl();
        meterReading.addReading(reading);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        List<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(new Interval(dayStart, dayEnd));
        assertThat(readings).hasSize(4*6);  // 4 per hour, during 6 hours
    }

    private DeviceConfiguration createDeviceConfigurationWithTwoRegisterSpecs() {
        RegisterType registerType1 = this.createRegisterTypeIfMissing("RegisterType1", forwardEnergyObisCode, unit1, forwardEnergyReadingType, 0);
        RegisterType registerType2 = this.createRegisterTypeIfMissing("RegisterType2", reverseEnergyObisCode, unit2, reverseEnergyReadingType, 0);
        deviceType.addRegisterType(registerType1);
        deviceType.addRegisterType(registerType2);
        DeviceType.DeviceConfigurationBuilder configurationWithRegisterTypes = deviceType.newConfiguration("ConfigurationWithRegisterTypes");
        NumericalRegisterSpec.Builder registerSpecBuilder1 = configurationWithRegisterTypes.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.setNumberOfDigits(9);
        registerSpecBuilder1.setNumberOfFractionDigits(0);
        NumericalRegisterSpec.Builder registerSpecBuilder2 = configurationWithRegisterTypes.newNumericalRegisterSpec(registerType2);
        registerSpecBuilder2.setNumberOfDigits(9);
        registerSpecBuilder2.setNumberOfFractionDigits(0);
        DeviceConfiguration deviceConfiguration = configurationWithRegisterTypes.add();
        deviceType.save();
        deviceConfiguration.activate();
        return deviceConfiguration;
    }

    private DeviceConfiguration createDeviceConfigurationWithTwoChannelSpecs() {
        RegisterType registerType1 = createRegisterTypeIfMissing("ChannelType1", forwardEnergyObisCode, unit1, forwardEnergyReadingType, 0);
        RegisterType registerType2 = createRegisterTypeIfMissing("ChannelType2", reverseEnergyObisCode, unit2, reverseEnergyReadingType, 0);
        registerType2.save();
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
        RegisterType measurementType;
        if (xRegisterType.isPresent()) {
            measurementType = xRegisterType.get();
        }
        else {
            measurementType = inMemoryPersistence.getMasterDataService().newRegisterType(name, obisCode, unit, readingType, timeOfUse);
            measurementType.save();
        }
        return measurementType;
    }

    private ChannelType createChannelTypeIfMissing(RegisterType registerType, TimeDuration timeDuration, ReadingType readingType) {
        Optional<ChannelType> xRegisterType = inMemoryPersistence.getMasterDataService().findChannelTypeByReadingType(readingType);
        ChannelType channelType;
        if (xRegisterType.isPresent()) {
            channelType = xRegisterType.get();
        }
        else {
            channelType = inMemoryPersistence.getMasterDataService().newChannelType(registerType, timeDuration, readingType);
            channelType.save();
        }
        return channelType;
    }

}