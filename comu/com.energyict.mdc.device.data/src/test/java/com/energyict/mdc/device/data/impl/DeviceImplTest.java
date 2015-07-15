package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.BillingReading;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.exceptions.CannotDeleteComScheduleFromDevice;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.ComScheduleBuilder;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import org.joda.time.DateTimeConstants;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import org.junit.*;
import org.junit.rules.*;

import static org.assertj.core.api.Assertions.assertThat;
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

    private ReadingType forwardEnergyReadingType;
    private ReadingType reverseEnergyReadingType;
    private String averageForwardEnergyReadingTypeMRID;
    private ObisCode averageForwardEnergyObisCode;
    private ObisCode forwardEnergyObisCode;
    private ObisCode reverseEnergyObisCode;

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @BeforeClass
    public static void setup() {
        try (TransactionContext context = getTransactionService().getContext()) {
            deviceProtocolPluggableClass = inMemoryPersistence.getProtocolPluggableService().newDeviceProtocolPluggableClass("MyTestProtocol", TestProtocol.class.getName());
            deviceProtocolPluggableClass.save();
            context.commit();
        }
    }

    @Before
    public void setupMasterData() {
        this.setupReadingTypes();
        IssueStatus wontFix = mock(IssueStatus.class);
        when(inMemoryPersistence.getIssueService().findStatus(IssueStatus.WONT_FIX)).thenReturn(Optional.of(wontFix));
    }

    private Device createSimpleDevice() {
        return createSimpleDeviceWithName(DEVICENAME);
    }

    private Device createSimpleDeviceWithName(String name, String mRID){
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, name, mRID);
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
        String code = getForwardEnergyReadingTypeCodeBuilder().code();
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
    public void noMeterActivationAfterInitialCreation() {
        Device device = createSimpleDevice();

        assertThat(device.getCurrentMeterActivation()).isEmpty();
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
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    public void createWithoutNameTest() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, null, MRID);
        device.save();
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    public void createWithEmptyNameTest() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "", MRID);
        device.save();
    }

    @Test
    @Transactional
    public void createWithSerialNumberTest() {
        String serialNumber = "MyTestSerialNumber";

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
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
        Device device1 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME + "MRIDFirst", MRID + "First");
        device1.setSerialNumber(serialNumber);
        device1.save();

        Device device2 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME + "MRIDSecond", MRID + "Second");
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
        Device device1 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME + "First", mRID);
        device1.save();
        Device device2 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME + "Second", mRID);
        device2.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MRID_REQUIRED + "}")
    public void noMRIDTest() {
        Device device1 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, null);
        device1.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MRID_REQUIRED + "}")
    public void emptyMRIDTest() {
        Device device1 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, "");
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

    /**
     * This test will get the default TimeZone of the system.
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
    public void getWithIncorrectTimeZoneIdAndFallBackToSystemTimeZoneTest() {
        createTestDefaultTimeZone();
        Device simpleDevice = createSimpleDevice();

        inMemoryPersistence.update("update ddc_device set TIMEZONE = 'InCorrectTimeZoneId' where id = " + simpleDevice.getId());

        Device reloadedDevice = getReloadedDevice(simpleDevice);

        assertThat(this.testDefaultTimeZone).isEqualTo(reloadedDevice.getTimeZone());
    }

    @Test
    @Transactional
    public void userDefinedTimeZoneTest() {
        createTestDefaultTimeZone();
        TimeZone userDefinedTimeZone = TimeZone.getTimeZone("Asia/Novokuznetsk");
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getRegisters()).hasSize(2);
    }

    @Test
    @Transactional
    public void getRegisterReadingsShouldReturnEmptyListTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoRegisterSpecs();
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getRegisterWithDeviceObisCode(forwardEnergyObisCode).getReadings(Interval.sinceEpoch())).isEmpty();
    }

    @Test
    @Transactional
    public void getRegisterReadingsShouldReturnResultsTest() {
        BigDecimal readingValue = new BigDecimal(5432.32);
        Instant readingTimeStamp = Instant.ofEpochMilli(123456789);
        com.elster.jupiter.metering.readings.Reading reading = com.elster.jupiter.metering.readings.beans.ReadingImpl.of(forwardEnergyReadingType.getMRID(), readingValue, readingTimeStamp);
        MeterReadingImpl meterReading = MeterReadingImpl.of(reading);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoRegisterSpecs();
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
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
        Instant eventStart = Instant.ofEpochMilli(eventStartUTC);
        Instant eventEnd = Instant.ofEpochMilli(eventStartUTC + DateTimeConstants.MILLIS_PER_DAY);   // May 2nd, 2000 00:00:00
        Instant readingTimeStamp = eventEnd;
        com.elster.jupiter.metering.readings.beans.ReadingImpl reading =
                com.elster.jupiter.metering.readings.beans.ReadingImpl.of(this.averageForwardEnergyReadingTypeMRID, readingValue, readingTimeStamp);
        reading.setTimePeriod(eventStart, eventEnd);
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addReading(reading);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoRegisterSpecs();
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
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
        assertThat(billingReading.getRange().isPresent()).isTrue();
        assertThat(billingReading.getRange().get()).isEqualTo(Ranges.openClosed(eventStart, eventEnd));
        assertThat(billingReading.getValue()).isEqualTo(readingValue);
    }

    @Test
    @Transactional
    public void getRegisterWithDeviceObisCodeForConfigWithRegistersTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoRegisterSpecs();
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getRegisterWithDeviceObisCode(forwardEnergyObisCode)).isNotNull();
        assertThat(reloadedDevice.getRegisterWithDeviceObisCode(reverseEnergyObisCode)).isNotNull();
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
        DeviceConfiguration deviceConfigurationWithTwoChannelSpecs = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithTwoChannelSpecs, "DeviceWithChannels", MRID);
        device.save();
        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getChannels()).isNotEmpty();
        assertThat(reloadedDevice.getChannels()).hasSize(2);
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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "MySimpleName", "BlaBla");
        device.save();
    }

    @Test
    @Transactional
    public void testGatewayTypeMethodsForHAN() {
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER));
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("GatewayTypeMethodsForHAN", deviceProtocolPluggableClass);
        deviceType.save();
        DeviceType.DeviceConfigurationBuilder config = deviceType.newConfiguration("some config").gatewayType(GatewayType.HOME_AREA_NETWORK);
        DeviceConfiguration deviceConfiguration = config.add();
        deviceConfiguration.activate();
        deviceType.save();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "name", "description");
        device.save();

        assertThat(device.getConfigurationGatewayType()).isEqualTo(GatewayType.HOME_AREA_NETWORK);
    }

    @Test
    @Transactional
    public void testGatewayTypeMethodsForLAN() {
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER));
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("GatewayTypeMethodsForLAN", deviceProtocolPluggableClass);
        deviceType.save();
        DeviceType.DeviceConfigurationBuilder config = deviceType.newConfiguration("some config").gatewayType(GatewayType.LOCAL_AREA_NETWORK);
        DeviceConfiguration deviceConfiguration = config.add();
        deviceConfiguration.activate();
        deviceType.save();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "name", "description");
        device.save();

        assertThat(device.getConfigurationGatewayType()).isEqualTo(GatewayType.LOCAL_AREA_NETWORK);
    }

    @Test
    @Transactional
    public void testGatewayTypeMethodsForNonConcentrator() {
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER));
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("GatewayTypeMethodsForNonConcentrator", deviceProtocolPluggableClass);
        deviceType.save();
        DeviceType.DeviceConfigurationBuilder config = deviceType.newConfiguration("some config");
        DeviceConfiguration deviceConfiguration = config.add();
        deviceConfiguration.activate();
        deviceType.save();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "name", "description");
        device.save();

        assertThat(device.getConfigurationGatewayType()).isEqualTo(GatewayType.NONE);
    }

    private ComSchedule createComSchedule(String mRIDAndName) {
        ComTask simpleComTask = inMemoryPersistence.getTaskService().newComTask("Simple task");
        simpleComTask.createStatusInformationTask();
        simpleComTask.save();

        ComScheduleBuilder builder = inMemoryPersistence.getSchedulingService().newComSchedule(mRIDAndName, new TemporalExpression(TimeDuration.days(1)), inMemoryPersistence.getClock().instant());
        builder.mrid(mRIDAndName);
        ComSchedule comSchedule = builder.build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();
        return comSchedule;
    }

    @Test
    @Transactional
    public void testGetLoadProfileDataIfRequestIntervalPreceedsActualMeterActivation() {
        BigDecimal readingValue = BigDecimal.valueOf(543232, 2);
        Instant dayStart = Instant.ofEpochMilli(1406851200000L); // Fri, 01 Aug 2014 00:00:00 GMT
        Instant dayEnd = Instant.ofEpochMilli(1406937600000L); // Sat, 02 Aug 2014 00:00:00 GMT
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        String code = getForwardEnergyReadingTypeCodeBuilder()
                .period(TimeAttribute.MINUTE15)
                .code();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(code);
        Instant readingTimeStamp = Instant.ofEpochMilli(1406884500000L);// 1/8/2014 9:15
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(readingTimeStamp, readingValue));
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
        Instant previousReadingTimeStamp = Instant.ofEpochMilli(1406883600000L);// 1/8/2014 9:00 -> this reading will end up in an interval starting at 8:45
        Instant startIntervalForPreviousReadingTimeStamp = Instant.ofEpochMilli(1406882700000L);// 1/8/2014 8:45
        intervalBlock2.addIntervalReading(IntervalReadingImpl.of(previousReadingTimeStamp, BigDecimal.ZERO));
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(intervalBlock);
        meterReading.addIntervalBlock(intervalBlock2);
        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        List<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(Ranges.openClosed(dayStart, dayEnd));
        assertThat(readings).describedAs("There should be no data(holders) for the interval 00:00->08:45").hasSize(24 * 4 - 4 * 9 + 1);
        assertThat(readings.get(0).getRange().upperEndpoint()).isEqualTo(dayEnd);
        assertThat(readings.get(readings.size()-1).getRange().lowerEndpoint()).isEqualTo(startIntervalForPreviousReadingTimeStamp);
        for (LoadProfileReading reading : readings) { // Only 1 channel will contain a value for a single interval
            if (reading.getRange().upperEndpoint().equals(readingTimeStamp)) {
                assertThat(reading.getChannelValues()).hasSize(1);
                for (Map.Entry<Channel, IntervalReadingRecord> channelBigDecimalEntry : reading.getChannelValues().entrySet()) {
                    assertThat(channelBigDecimalEntry.getKey().getReadingType().getMRID()).isEqualTo(code);
                    assertThat(channelBigDecimalEntry.getValue().getValue()).isEqualTo(readingValue);
                }
            }
        }
    }


    // JP-5583
    @Test
    @Transactional
    public void testGetChannelDataIfRequestedIntervalHasNoReadingsButDataWasExpected() {
        BigDecimal readingValue = BigDecimal.valueOf(543232, 2);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        String code = getForwardEnergyReadingTypeCodeBuilder()
                .period(TimeAttribute.MINUTE15)
                .code();
        IntervalBlockImpl intervalBlock1 = IntervalBlockImpl.of(code);
        intervalBlock1.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(2014, 8, 1, 0, 15, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
        intervalBlock2.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(2014, 8, 1, 0, 30, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock3 = IntervalBlockImpl.of(code);
        intervalBlock3.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(2014, 8, 1, 0, 45, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock4 = IntervalBlockImpl.of(code);
        intervalBlock4.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(2014, 8, 1, 1, 0, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlockX = IntervalBlockImpl.of(code);
        intervalBlockX.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(2014, 8, 1, 23, 45, 0).toInstant(ZoneOffset.UTC), readingValue));
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(intervalBlock1);
        meterReading.addIntervalBlock(intervalBlock2);
        meterReading.addIntervalBlock(intervalBlock3);
        meterReading.addIntervalBlock(intervalBlock4);
        meterReading.addIntervalBlock(intervalBlockX);
        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        Instant start = LocalDateTime.of(2014, 8, 1, 12, 0, 0).toInstant(ZoneOffset.UTC);
        Instant end = LocalDateTime.of(2014, 8, 1, 16, 0, 0).toInstant(ZoneOffset.UTC);
        List<LoadProfileReading> readings = reloadedDevice.getChannels().get(0).getChannelData(Ranges.openClosed(start, end));
        assertThat(readings).describedAs("There should be data(holders) for the interval 12:00->16:00 even though there are no meter readings").hasSize(4 * 4);
    }

    // JP-5583
    @Test
    @Transactional
    public void testGetLoadProfileDataIfRequestedIntervalHasNoReadingsButDataWasExpected() {
        BigDecimal readingValue = BigDecimal.valueOf(543232, 2);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        String code = getForwardEnergyReadingTypeCodeBuilder()
                .period(TimeAttribute.MINUTE15)
                .code();
        IntervalBlockImpl intervalBlock1 = IntervalBlockImpl.of(code);
        intervalBlock1.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(2014, 8, 1, 0, 15, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
        intervalBlock2.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(2014, 8, 1, 0, 30, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock3 = IntervalBlockImpl.of(code);
        intervalBlock3.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(2014, 8, 1, 0, 45, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock4 = IntervalBlockImpl.of(code);
        intervalBlock4.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(2014, 8, 1, 1, 0, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlockX = IntervalBlockImpl.of(code);
        intervalBlockX.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(2014, 8, 1, 23, 45, 0).toInstant(ZoneOffset.UTC), readingValue));
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(intervalBlock1);
        meterReading.addIntervalBlock(intervalBlock2);
        meterReading.addIntervalBlock(intervalBlock3);
        meterReading.addIntervalBlock(intervalBlock4);
        meterReading.addIntervalBlock(intervalBlockX);
        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        Instant start = LocalDateTime.of(2014, 8, 1, 12, 0, 0).toInstant(ZoneOffset.UTC);
        Instant end = LocalDateTime.of(2014, 8, 1, 16, 0, 0).toInstant(ZoneOffset.UTC);
        List<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(Ranges.openClosed(start, end));
        assertThat(readings).describedAs("There should be data(holders) for the interval 12:00->16:00 even though there are no meter readings").hasSize(4*4);
    }

    // JP-5583
    @Test
    @Transactional
    public void testGetLoadProfileDataIfRequestedIntervalIsEmptyButDataWasExpected() {
        BigDecimal readingValue = BigDecimal.valueOf(543232, 2);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        String code = getForwardEnergyReadingTypeCodeBuilder()
                .period(TimeAttribute.MINUTE15)
                .code();
        IntervalBlockImpl intervalBlock1 = IntervalBlockImpl.of(code);
        intervalBlock1.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(2014, 8, 1, 0, 15, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
        intervalBlock2.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(2014, 8, 1, 0, 30, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock3 = IntervalBlockImpl.of(code);
        intervalBlock3.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(2014, 8, 1, 0, 45, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock4 = IntervalBlockImpl.of(code);
        intervalBlock4.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(2014, 8, 1, 1, 0, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlockX = IntervalBlockImpl.of(code);
        intervalBlockX.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(2014, 8, 1, 23, 45, 0).toInstant(ZoneOffset.UTC), readingValue));
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(intervalBlock1);
        meterReading.addIntervalBlock(intervalBlock2);
        meterReading.addIntervalBlock(intervalBlock3);
        meterReading.addIntervalBlock(intervalBlock4);
        meterReading.addIntervalBlock(intervalBlockX);
        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        Instant start = LocalDateTime.of(2014, 8, 1, 12, 5, 0).toInstant(ZoneOffset.UTC);
        Instant end = LocalDateTime.of(2014, 8, 1, 12, 10, 0).toInstant(ZoneOffset.UTC);
        List<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(Ranges.openClosed(start, end));
        assertThat(readings).describedAs("There should be 1 data(holders) for the interval 12:05->12:10: 1x15 minute reading overlaps with the interval").hasSize(1);
    }

    @Test
    @Transactional
    // @see  JP-8514
    public void testGetLoadProfileDataAfterLastReading() {
        BigDecimal readingValue = BigDecimal.valueOf(543232, 2);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        String code = getForwardEnergyReadingTypeCodeBuilder()
                .period(TimeAttribute.MINUTE15)
                .code();
        IntervalBlockImpl intervalBlock1 = IntervalBlockImpl.of(code);
        intervalBlock1.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(2014, 8, 1, 0, 15, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
        intervalBlock2.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(2014, 8, 1, 0, 30, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock3 = IntervalBlockImpl.of(code);
        intervalBlock3.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(2014, 8, 1, 0, 45, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock4 = IntervalBlockImpl.of(code);
        intervalBlock4.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(2014, 8, 1, 1, 0, 0).toInstant(ZoneOffset.UTC), readingValue));
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(intervalBlock1);
        meterReading.addIntervalBlock(intervalBlock2);
        meterReading.addIntervalBlock(intervalBlock3);
        meterReading.addIntervalBlock(intervalBlock4);
        device.store(meterReading);
        device.getLoadProfileUpdaterFor(device.getLoadProfiles().get(0)).setLastReading(LocalDateTime.of(2014, 8, 1, 1, 0, 0).toInstant(ZoneOffset.UTC)).update();

        Device reloadedDevice = getReloadedDevice(device);
        Instant start = LocalDateTime.of(2014, 8, 1, 12, 0, 0).toInstant(ZoneOffset.UTC);
        Instant end = LocalDateTime.of(2014, 8, 1, 13, 0, 0).toInstant(ZoneOffset.UTC);
        List<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(Ranges.openClosed(start, end));
        assertThat(readings).describedAs("There should be no data(holders) after the last reading").isEmpty();
    }

    @Test
    @Transactional
    public void testGetLoadProfileDataIfRequestIntervalExceedsLoadProfilesLastReading() {
        BigDecimal readingValue = BigDecimal.valueOf(543232, 2);
        Instant requestIntervalStart = Instant.ofEpochMilli(1406851200000L); // Fri, 01 Aug 2014 00:00:00 GMT
        Instant requestIntervalEnd = Instant.ofEpochMilli(1406937600000L); // Sat, 02 Aug 2014 00:00:00 GMT
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        String code = getForwardEnergyReadingTypeCodeBuilder()
                .period(TimeAttribute.MINUTE15)
                .code();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(code);
        Instant readingTimeStamp = Instant.ofEpochMilli(1406852100000L);// 1/8/2014 0:15
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(readingTimeStamp, readingValue));
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
        Instant previousReadingTimeStamp = Instant.ofEpochMilli(1406851200000L);// 1/8/2014 0:00
        intervalBlock2.addIntervalReading(IntervalReadingImpl.of(previousReadingTimeStamp, BigDecimal.ZERO));
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(intervalBlock);
        meterReading.addIntervalBlock(intervalBlock2);
        device.store(meterReading);
        Instant lastReading = Instant.ofEpochMilli(1406926800000L); //  Fri, 01 Aug 2014 21:00:00 GMT
        device.getLoadProfileUpdaterFor(device.getLoadProfiles().get(0)).setLastReading(lastReading).update();

        Device reloadedDevice = getReloadedDevice(device);
        List<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(Ranges.openClosed(requestIntervalStart, requestIntervalEnd));
        assertThat(readings).describedAs("There should be no data(holders) for the interval 21:00->00:00").hasSize(24 * 4 - 12); // 3 times 4 intervals/hour missing
        assertThat(readings.get(0).getRange().upperEndpoint()).isEqualTo(lastReading);
        assertThat(readings.get(readings.size() - 1).getRange().lowerEndpoint()).isEqualTo(requestIntervalStart);
        for (LoadProfileReading reading : readings) { // Only 1 channel will contain a value for a single interval
            if (reading.getRange().upperEndpoint().equals(readingTimeStamp)) {
                assertThat(reading.getChannelValues()).hasSize(1);
                for (Map.Entry<Channel, IntervalReadingRecord> channelBigDecimalEntry : reading.getChannelValues().entrySet()) {
                    assertThat(channelBigDecimalEntry.getKey().getReadingType().getMRID()).isEqualTo(code);
                    assertThat(channelBigDecimalEntry.getValue().getValue()).isEqualTo(readingValue);
                }
            }
        }
    }

    @Test
    @Transactional
    public void testGetLoadProfileDataMonthlyMoscowTime() {
        TimeZone MSK = TimeZone.getTimeZone("GMT+4");
        when(inMemoryPersistence.getClock().getZone()).thenReturn(MSK.toZoneId());
        when(inMemoryPersistence.getClock().instant()).thenAnswer(invocationOnMock -> Instant.now());

        BigDecimal readingValue = BigDecimal.valueOf(543232, 2);
        Instant requestIntervalStart = Instant.ofEpochMilli(1104523200000L); // Fri, 31 Dec 2004 20:00:00 GMT
        Instant requestIntervalEnd = Instant.ofEpochMilli(1420056000000L); // Wed, 31 Dec 2014 20:00:00 GMT
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(TimeDuration.months(1));
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        Optional<AmrSystem> amrSystem = inMemoryPersistence.getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId());
        Optional<Meter> meter = amrSystem.get().findMeter("" + device.getId());
        meter.get().activate(Instant.ofEpochMilli(1385841600000L));//Sat, 30 Nov 2013 20:00:00 GMT

        String code = getForwardEnergyReadingTypeCodeBuilder()
                .period(MacroPeriod.MONTHLY)
                .code();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(code);
        Instant readingTimeStamp = Instant.ofEpochMilli(1385841600000L);//Sat, 30 Nov 2013 20:00:00 GMT
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(readingTimeStamp, readingValue));
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
        Instant previousReadingTimeStamp = Instant.ofEpochMilli(1388520000000L);//  Wed, 31 Dec 2013 20:00:00 GMT
        intervalBlock2.addIntervalReading(IntervalReadingImpl.of(previousReadingTimeStamp, BigDecimal.ZERO));
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(intervalBlock);
        meterReading.addIntervalBlock(intervalBlock2);
        device.store(meterReading);
        Instant lastReading = Instant.ofEpochMilli(1420802100000L); // Fri, 09 Jan 2015 11:15:00 GMT
        device.getLoadProfileUpdaterFor(device.getLoadProfiles().get(0)).setLastReading(lastReading).update();

        Device reloadedDevice = getReloadedDevice(device);
        List<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(Ranges.openClosed(requestIntervalStart, requestIntervalEnd));
        assertThat(readings).describedAs("There should be 14 intervals since meter activation").hasSize(14);
        assertThat(readings.get(13).getRange().upperEndpoint()).isEqualTo(readingTimeStamp);
    }

    @Test
    @Transactional
    public void testGetLoadProfileDataMonthlyMeterActivationNotAlignedWithMidnight() {
        // cfr JP-8199
        TimeZone UTC = TimeZone.getTimeZone("GMT+1"); // Paris time zone
        when(inMemoryPersistence.getClock().getZone()).thenReturn(UTC.toZoneId());
        when(inMemoryPersistence.getClock().instant()).thenAnswer(invocationOnMock -> Instant.now());

        Instant requestIntervalStart = Instant.ofEpochMilli(1385851500000L); //   11/30/2013, 11:45:00 PM (UTC)
        Instant requestIntervalEnd = Instant.ofEpochMilli(1420066800000L); // 1/1/2015, 12:00:00 AM (UTC)
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(TimeDuration.months(1));
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        Optional<AmrSystem> amrSystem = inMemoryPersistence.getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId());
        Optional<Meter> meter = amrSystem.get().findMeter("" + device.getId());
        meter.get().activate(Instant.ofEpochMilli(1385851500000L));//Sat, 30 Nov 2013 22:45:00 GMT

        String code = getForwardEnergyReadingTypeCodeBuilder()
                .period(MacroPeriod.MONTHLY)
                .code();
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
        Instant readingTimeStamp = Instant.ofEpochMilli(1385852400000L);//  Sat, 30 Nov 2013 23:00:00 GMT
        intervalBlock2.addIntervalReading(IntervalReadingImpl.of(readingTimeStamp, BigDecimal.ZERO));
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(intervalBlock2);
        device.store(meterReading);


        Instant lastReading = Instant.ofEpochMilli(1420802100000L); // Fri, 09 Jan 2015 11:15:00 GMT
        device.getLoadProfileUpdaterFor(device.getLoadProfiles().get(0)).setLastReading(lastReading).update();

        Device reloadedDevice = getReloadedDevice(device);
        List<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(Ranges.openClosed(requestIntervalStart, requestIntervalEnd));
        assertThat(readings.get(13).getRange().upperEndpoint()).isEqualTo(Instant.ofEpochMilli(1385852400000L)); // Sat, 30 Nov 2013 23:00:00 GMT
    }

    @Test
    @Transactional
    public void testGetLoadProfileDataIfExternalMeterActivationDoesNotAlignWithChannelIntervalBoundary() {
        BigDecimal readingValue = BigDecimal.valueOf(543232, 2);
        Instant requestIntervalStart = Instant.ofEpochMilli(1420761600000L); // Fri, 09 Jan 2015 00:00:00 GMT
        Instant requestIntervalEnd = Instant.ofEpochMilli(1420848000000L); //  Sat, 10 Jan 2015 00:00:00 GMT
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();

        Optional<AmrSystem> amrSystem = inMemoryPersistence.getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId());
        Optional<Meter> meter = amrSystem.get().findMeter("" + device.getId());
        meter.get().activate(Instant.ofEpochMilli(1420801085000L));// 9/1/2015 10:58

        String code = getForwardEnergyReadingTypeCodeBuilder()
                .period(TimeAttribute.MINUTE15)
                .code();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(code);
        Instant readingTimeStamp = Instant.ofEpochMilli(1420801200000L);// 9/1/2015 11:00
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(readingTimeStamp, readingValue));
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
        Instant previousReadingTimeStamp = Instant.ofEpochMilli(1420802100000L);// Fri, 09 Jan 2015 11:15:00 GMT
        intervalBlock2.addIntervalReading(IntervalReadingImpl.of(previousReadingTimeStamp, BigDecimal.ZERO));
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(intervalBlock);
        meterReading.addIntervalBlock(intervalBlock2);
        device.store(meterReading);
        Instant lastReading = Instant.ofEpochMilli(1420802100000L); // Fri, 09 Jan 2015 11:15:00 GMT
        device.getLoadProfileUpdaterFor(device.getLoadProfiles().get(0)).setLastReading(lastReading).update();

        Device reloadedDevice = getReloadedDevice(device);
        List<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(Ranges.openClosed(requestIntervalStart, requestIntervalEnd));
        assertThat(readings).describedAs("There should be only 2 intervals between activation and last reading").hasSize(2);
        assertThat(readings.get(0).getRange().upperEndpoint()).isEqualTo(lastReading);
        assertThat(readings.get(readings.size()-1).getRange().lowerEndpoint()).isEqualTo(Instant.ofEpochMilli(1420800300000L));// 9/1/2015 10:45
    }

    @Test
    @Transactional
    public void testGetLoadProfileData() {
        BigDecimal readingValue = BigDecimal.valueOf(543232, 2);
        Instant dayStart = Instant.ofEpochMilli(1406851200000L); // Fri, 01 Aug 2014 00:00:00 GMT
        Instant dayEnd = Instant.ofEpochMilli(1406937600000L); // Sat, 02 Aug 2014 00:00:00 GMT
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        String code = getForwardEnergyReadingTypeCodeBuilder()
                .period(TimeAttribute.MINUTE15)
                .code();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(code);
        Instant readingTimeStamp = Instant.ofEpochMilli(1406852100000L);// 1/8/2014 0:15
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(readingTimeStamp, readingValue));
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
        Instant previousReadingTimeStamp = Instant.ofEpochMilli(1406851200000L);// 1/8/2014 0:00
        intervalBlock2.addIntervalReading(IntervalReadingImpl.of(previousReadingTimeStamp, BigDecimal.ZERO));
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(intervalBlock);
        meterReading.addIntervalBlock(intervalBlock2);
        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        List<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(Ranges.openClosed(dayStart, dayEnd));
        assertThat(readings).hasSize(24 * 4);
        assertThat(readings.get(0).getRange().upperEndpoint()).isEqualTo(dayEnd);
        assertThat(readings.get(readings.size()-1).getRange().lowerEndpoint()).isEqualTo(dayStart);
        for (LoadProfileReading reading : readings) { // Only 1 channel will contain a value for a single interval
            if (reading.getRange().upperEndpoint().equals(readingTimeStamp)) {
                assertThat(reading.getChannelValues()).hasSize(1);
                for (Map.Entry<Channel, IntervalReadingRecord> channelBigDecimalEntry : reading.getChannelValues().entrySet()) {
                    assertThat(channelBigDecimalEntry.getKey().getReadingType().getMRID()).isEqualTo(code);
                    assertThat(channelBigDecimalEntry.getValue().getValue()).isEqualTo(readingValue);
                }
            }
        }
    }

    @Test
    @Transactional
    public void testGetLoadProfileDataHasValidationState() {
        BigDecimal readingValue = BigDecimal.valueOf(543232, 2);
        Instant dayStart = Instant.ofEpochMilli(1406851200000L); // Fri, 01 Aug 2014 00:00:00 GMT
        Instant dayEnd = Instant.ofEpochMilli(1406937600000L); // Sat, 02 Aug 2014 00:00:00 GMT
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        String code = getForwardEnergyReadingTypeCodeBuilder()
                .period(TimeAttribute.MINUTE15)
                .code();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(code);
        Instant readingTimeStamp = Instant.ofEpochMilli(1406852100000L);// 1/8/2014 0:15
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(readingTimeStamp, readingValue));
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
        Instant previousReadingTimeStamp = Instant.ofEpochMilli(1406851200000L);// 1/8/2014 0:00
        intervalBlock2.addIntervalReading(IntervalReadingImpl.of(previousReadingTimeStamp, BigDecimal.ZERO));
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(intervalBlock);
        meterReading.addIntervalBlock(intervalBlock2);
        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        List<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(Ranges.openClosed(dayStart, dayEnd));
        assertThat(readings).hasSize(24 * 4);
        assertThat(readings.get(0).getRange().upperEndpoint()).isEqualTo(dayEnd);
        assertThat(readings.get(readings.size()-1).getRange().lowerEndpoint()).isEqualTo(dayStart);
        for (LoadProfileReading reading : readings) { // Only 1 channel will contain a value for a single interval
            if (reading.getRange().upperEndpoint().equals(readingTimeStamp)) {
                assertThat(reading.getChannelValidationStates()).hasSize(1);
                for (Map.Entry<Channel, DataValidationStatus> channelBigDecimalEntry : reading.getChannelValidationStates().entrySet()) {
                    assertThat(channelBigDecimalEntry.getKey().getReadingType().getMRID()).isEqualTo(code);
                    DataValidationStatus status = channelBigDecimalEntry.getValue();
                    assertThat(status.completelyValidated()).isFalse();
                    assertThat(status.getReadingQualities()).isEmpty();
                }
            }
        }
    }

    @Test
    @Transactional
    public void testGetEmptyLoadProfileData() {
        Instant dayStart = Instant.ofEpochMilli(1406851200000L); // Fri, 01 Aug 2014 00:00:00 GMT
        Instant dayEnd = Instant.ofEpochMilli(1406937600000L); // Sat, 02 Aug 2014 00:00:00 GMT
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        Collection<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(Ranges.openClosed(dayStart, dayEnd));
        assertThat(readings).isEmpty();
    }

    @Test
    @Transactional
    public void testGetLoadProfileDataDST() {
        BigDecimal readingValue = new BigDecimal(5432.32);
        Instant readingTimeStamp = Instant.ofEpochMilli(1396138500000L); // 3/30/2014, 01:15:00 AM
        Instant dayStart = Instant.ofEpochMilli(1396137600000L); // 3/30/2014, 1:00:00 AM
        Instant dayEnd = Instant.ofEpochMilli(1396159200000L); // 3/30/2014, 8:00:00 AM !! 6 hours later!!
        String code = getForwardEnergyReadingTypeCodeBuilder().period(TimeAttribute.MINUTE15).code();
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(code);
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(readingTimeStamp, readingValue));
        meterReading.addIntervalBlock(intervalBlock);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        List<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(Ranges.openClosed(dayStart, dayEnd));
        assertThat(readings).hasSize(4 * 6);  // 4 per hour, during 6 hours
    }

    @Test
    @Transactional
    public void activateMeter() {
        Device device = this.createSimpleDevice();
        Instant expectedStart = Instant.ofEpochMilli(97L);

        // Business method
        device.activate(expectedStart);

        // Asserts
        assertThat(device.getCurrentMeterActivation()).isPresent();
        assertThat(device.getCurrentMeterActivation().get().getStart()).isEqualTo(expectedStart);
    }

    @Test
    @Transactional
    public void deactivateNowOnMeterThatWasNotActive() {
        Device device = this.createSimpleDevice();

        // Business method
        device.deactivateNow();

        // Asserts
        assertThat(device.getCurrentMeterActivation()).isEmpty();
    }

    @Test
    @Transactional
    public void deactivateMeterThatWasNotActive() {
        Device device = this.createSimpleDevice();
        Instant instant = Instant.ofEpochMilli(97L);

        // Business method
        device.deactivate(instant);

        // Asserts
        assertThat(device.getCurrentMeterActivation()).isEmpty();
    }

    @Test
    @Transactional
    public void deactivateMeter() {
        Device device = this.createSimpleDevice();
        Instant initialStart = Instant.ofEpochMilli(1000L);
        device.activate(initialStart);
        Instant end = Instant.ofEpochMilli(2000L);

        // Business method
        device.deactivate(end);

        // Asserts
        assertThat(device.getCurrentMeterActivation()).isEmpty();
    }

    @Test
    @Transactional
    public void reactivateMeter() {
        Device device = this.createSimpleDevice();
        Instant initialStart = Instant.ofEpochMilli(1000L);
        device.activate(initialStart);
        Instant end = Instant.ofEpochMilli(2000L);
        device.deactivate(end);
        Instant expectedStart = Instant.ofEpochMilli(3000L);

        // Business method
        device.activate(expectedStart);

        // Asserts
        assertThat(device.getCurrentMeterActivation()).isPresent();
        assertThat(device.getCurrentMeterActivation().get().getStart()).isEqualTo(expectedStart);
    }

    @Test
    @Transactional
    public void updateCimLifecycleDates() {
        Device device = this.createSimpleDevice();
        Instant expectedInstalledDate = Instant.ofEpochSecond(1L);
        Instant expectedManufacturedDate = Instant.ofEpochSecond(2L);
        Instant expectedPurchasedDate = Instant.ofEpochSecond(3L);
        Instant expectedReceivedDate = Instant.ofEpochSecond(4L);
        Instant expectedRetiredDate = Instant.ofEpochSecond(5L);
        Instant expectedRemovedDate = Instant.ofEpochSecond(6L);

        // Business method(s)
        device
            .getLifecycleDates()
            .setInstalledDate(expectedInstalledDate)
            .setManufacturedDate(expectedManufacturedDate)
            .setPurchasedDate(expectedPurchasedDate)
            .setReceivedDate(expectedReceivedDate)
            .setRetiredDate(expectedRetiredDate)
            .setRemovedDate(expectedRemovedDate)
            .save();

        // Asserts: assert the dates on the EndDevice
        EndDevice endDevice = inMemoryPersistence.getMeteringService().findEndDevice(device.getmRID()).get();
        assertThat(endDevice.getLifecycleDates().getInstalledDate()).contains(expectedInstalledDate);
        assertThat(endDevice.getLifecycleDates().getManufacturedDate()).contains(expectedManufacturedDate);
        assertThat(endDevice.getLifecycleDates().getPurchasedDate()).contains(expectedPurchasedDate);
        assertThat(endDevice.getLifecycleDates().getReceivedDate()).contains(expectedReceivedDate);
        assertThat(endDevice.getLifecycleDates().getRetiredDate()).contains(expectedRetiredDate);
        assertThat(endDevice.getLifecycleDates().getRemovedDate()).contains(expectedRemovedDate);
    }

    private DeviceConfiguration createDeviceConfigurationWithTwoRegisterSpecs() {
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardEnergyReadingType);
        RegisterType registerType2 = this.createRegisterTypeIfMissing(reverseEnergyObisCode, reverseEnergyReadingType);
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

    private DeviceConfiguration createDeviceConfigurationWithTwoChannelSpecs(TimeDuration myInterval) {
        RegisterType registerType1 = createRegisterTypeIfMissing(forwardEnergyObisCode, forwardEnergyReadingType);
        RegisterType registerType2 = createRegisterTypeIfMissing(reverseEnergyObisCode, reverseEnergyReadingType);
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("LoadProfileType", loadProfileObisCode, myInterval, Arrays.asList(registerType1, registerType2));
        loadProfileType.save();
        ChannelType channelTypeForRegisterType1 = loadProfileType.findChannelType(registerType1).get();
        ChannelType channelTypeForRegisterType2 = loadProfileType.findChannelType(registerType2).get();
        deviceType.addLoadProfileType(loadProfileType);
        DeviceType.DeviceConfigurationBuilder configurationWithLoadProfileAndChannel = deviceType.newConfiguration("ConfigurationWithLoadProfileAndChannel");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationWithLoadProfileAndChannel.newLoadProfileSpec(loadProfileType);
        configurationWithLoadProfileAndChannel.newChannelSpec(channelTypeForRegisterType1, loadProfileSpecBuilder);
        configurationWithLoadProfileAndChannel.newChannelSpec(channelTypeForRegisterType2, loadProfileSpecBuilder);
        DeviceConfiguration deviceConfiguration = configurationWithLoadProfileAndChannel.add();
        deviceType.save();
        deviceConfiguration.activate();
        return deviceConfiguration;
    }

    private RegisterType createRegisterTypeIfMissing(ObisCode obisCode, ReadingType readingType) {
        Optional<RegisterType> xRegisterType = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType);
        RegisterType measurementType;
        if (xRegisterType.isPresent()) {
            measurementType = xRegisterType.get();
        }
        else {
            measurementType = inMemoryPersistence.getMasterDataService().newRegisterType(readingType, obisCode);
            measurementType.save();
        }
        return measurementType;
    }

    private ReadingTypeCodeBuilder getForwardEnergyReadingTypeCodeBuilder() {
        return ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR);
    }


}