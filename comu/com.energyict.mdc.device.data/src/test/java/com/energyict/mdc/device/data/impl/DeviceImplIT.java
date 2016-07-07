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
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.ComTaskEnablementBuilder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.BillingReading;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.NumericalRegister;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.exceptions.CannotDeleteComScheduleFromDevice;
import com.energyict.mdc.device.data.exceptions.MultiplierConfigurationException;
import com.energyict.mdc.device.data.exceptions.NoStatusInformationTaskException;
import com.energyict.mdc.device.data.impl.tasks.AbstractComTaskExecutionImplTest;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.ComScheduleBuilder;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;

import com.google.common.collect.Range;
import org.joda.time.DateTimeConstants;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceImpl} component.
 * <p>
 * Copyrights EnergyICT
 * Date: 05/03/14
 * Time: 13:49
 */
public class DeviceImplIT extends PersistenceIntegrationTest {

    private static final String DEVICENAME = "deviceName";
    private static final String MRID = "MyUniqueMRID";
    private static final ObisCode loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");
    private final TimeZone testDefaultTimeZone = TimeZone.getTimeZone("Canada/East-Saskatchewan");
    private final TimeDuration interval = TimeDuration.minutes(15);
    private final BigDecimal overflowValue = BigDecimal.valueOf(1234567);
    private final int numberOfFractionDigits = 2;

    private ReadingType forwardBulkSecondaryEnergyReadingType;
    private ReadingType forwardDeltaSecondaryEnergyReadingType;
    private ReadingType forwardBulkPrimaryEnergyReadingType;
    private ReadingType forwardDeltaPrimaryMonthlyEnergyReadingType;
    private ReadingType reverseBulkSecondaryEnergyReadingType;
    private ReadingType reverseBulkPrimaryEnergyReadingType;
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

    private Device createSimpleDeviceWithName(String name, String mRID) {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, name, mRID);
        device.save();
        return device;
    }

    private Device createSimpleDeviceWithName(String name) {
        return createSimpleDeviceWithName(name, "SimpleMrId");
    }

    private void createTestDefaultTimeZone() {
        TimeZone.setDefault(this.testDefaultTimeZone);
        when(inMemoryPersistence.getClock().getZone()).thenReturn(this.testDefaultTimeZone.toZoneId());
    }

    private void setupReadingTypes() {
        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().code();
        this.forwardBulkSecondaryEnergyReadingType = inMemoryPersistence.getMeteringService().getReadingType(code).get();
        this.forwardDeltaSecondaryEnergyReadingType = inMemoryPersistence.getMeteringService()
                .getReadingType(getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().accumulate(Accumulation.DELTADELTA).code())
                .get();
        this.forwardBulkPrimaryEnergyReadingType = inMemoryPersistence.getMeteringService().getReadingType(getForwardBulkPrimaryEnergyReadingType().code()).get();
        this.forwardDeltaPrimaryMonthlyEnergyReadingType = inMemoryPersistence.getMeteringService()
                .getReadingType(getForwardDeltaPrimaryMonthlyEnergyReadingType().period(MacroPeriod.MONTHLY).code())
                .get();
        this.forwardEnergyObisCode = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeInformationFor(forwardBulkSecondaryEnergyReadingType).getObisCode();
        String reverseBulkSecondaryCode = getReverseSecondaryBulkReadingTypeCodeBuilder().code();
        this.reverseBulkSecondaryEnergyReadingType = inMemoryPersistence.getMeteringService().getReadingType(reverseBulkSecondaryCode).get();
        String reverseBulkPrimaryCode = ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_PRIMARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.REVERSE)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
        this.reverseBulkPrimaryEnergyReadingType = inMemoryPersistence.getMeteringService().getReadingType(reverseBulkPrimaryCode).get();
        this.reverseEnergyObisCode = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeInformationFor(reverseBulkSecondaryEnergyReadingType).getObisCode();
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
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    public void createWithoutNameTest() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, null, MRID);
        device.save();
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
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
    public void testUpdateMRID() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();

        DeviceImpl reloadedDevice = (DeviceImpl) getReloadedDevice(device);

        reloadedDevice.setmRID("newMRID");
        reloadedDevice.save();

        reloadedDevice = (DeviceImpl) getReloadedDevice(device);

        assertThat(reloadedDevice.getmRID()).isEqualTo("newMRID");

        Optional<Meter> koreMeter = reloadedDevice.findKoreMeter(inMemoryPersistence.getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId()).get());
        assertThat(koreMeter).isPresent();
        assertThat(koreMeter.get().getMRID()).isEqualTo("newMRID");
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
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    public void noMRIDTest() {
        Device device1 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, null);
        device1.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
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
        reloadedDevice.setZone(null);
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
        com.elster.jupiter.metering.readings.Reading reading = com.elster.jupiter.metering.readings.beans.ReadingImpl.of(forwardBulkSecondaryEnergyReadingType.getMRID(), readingValue, readingTimeStamp);
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
        assertThat(numericalReading.getType().getMRID()).isEqualTo(forwardBulkSecondaryEnergyReadingType.getMRID());
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
        assertThat(billingReading.getType().getMRID()).isEqualTo(forwardBulkSecondaryEnergyReadingType.getMRID());
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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "MySimpleName", "BlaBla");
        device.save();
    }

    @Test
    @Transactional
    public void testGatewayTypeMethodsForHAN() {
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER));
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("GatewayTypeMethodsForHAN", deviceProtocolPluggableClass);
        DeviceType.DeviceConfigurationBuilder config = deviceType.newConfiguration("some config").gatewayType(GatewayType.HOME_AREA_NETWORK);
        DeviceConfiguration deviceConfiguration = config.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "name", "description");
        device.save();

        assertThat(device.getConfigurationGatewayType()).isEqualTo(GatewayType.HOME_AREA_NETWORK);
    }

    @Test
    @Transactional
    public void testGatewayTypeMethodsForLAN() {
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER));
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("GatewayTypeMethodsForLAN", deviceProtocolPluggableClass);
        DeviceType.DeviceConfigurationBuilder config = deviceType.newConfiguration("some config").gatewayType(GatewayType.LOCAL_AREA_NETWORK);
        DeviceConfiguration deviceConfiguration = config.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "name", "description");
        device.save();

        assertThat(device.getConfigurationGatewayType()).isEqualTo(GatewayType.LOCAL_AREA_NETWORK);
    }

    @Test
    @Transactional
    public void testGatewayTypeMethodsForNonConcentrator() {
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER));
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("GatewayTypeMethodsForNonConcentrator", deviceProtocolPluggableClass);
        DeviceType.DeviceConfigurationBuilder config = deviceType.newConfiguration("some config");
        DeviceConfiguration deviceConfiguration = config.add();
        deviceConfiguration.activate();

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
        builder.addComTask(simpleComTask);
        return builder.build();
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
        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder()
                .period(TimeAttribute.MINUTE15)
                .code();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(code);
        Instant readingTimeStamp = dayEnd;
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
        assertThat(readings.get(readings.size() - 1).getRange().lowerEndpoint()).isEqualTo(startIntervalForPreviousReadingTimeStamp);
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
        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder()
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
        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder()
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
        assertThat(readings).describedAs("There should be data(holders) for the interval 12:00->16:00 even though there are no meter readings").hasSize(4 * 4);
    }

    // JP-5583
    @Test
    @Transactional
    public void testGetLoadProfileDataIfRequestedIntervalIsEmptyButDataWasExpected() {
        BigDecimal readingValue = BigDecimal.valueOf(543232, 2);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder()
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
        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder()
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
        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder()
                .period(TimeAttribute.MINUTE15)
                .code();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(code);
        Instant readingTimeStamp = Instant.ofEpochMilli(1406926800000L);//  Fri, 01 Aug 2014 21:00:00 GMT
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
        Instant meterActivation = Instant.ofEpochMilli(1385841600000L);
        meter.get().activate(meterActivation);//Sat, 30 Nov 2013 20:00:00 GMT

        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder()
                .period(MacroPeriod.MONTHLY)
                .code();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(code);
        Instant readingTimeStamp = requestIntervalEnd;
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
        assertThat(readings.get(13).getRange().upperEndpoint()).isEqualTo(meterActivation);
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

        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder()
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

        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder()
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
        assertThat(readings.get(readings.size() - 1).getRange().lowerEndpoint()).isEqualTo(Instant.ofEpochMilli(1420800300000L));// 9/1/2015 10:45
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
        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder()
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
        assertThat(readings).hasSize(1);

        LoadProfileReading reading = readings.get(0);
        assertThat(reading.getRange().upperEndpoint()).isEqualTo(readingTimeStamp);
        assertThat(reading.getRange().lowerEndpoint()).isEqualTo(dayStart);
        if (reading.getRange().upperEndpoint().equals(readingTimeStamp)) {
            assertThat(reading.getChannelValues()).hasSize(1);
            for (Map.Entry<Channel, IntervalReadingRecord> channelBigDecimalEntry : reading.getChannelValues().entrySet()) {
                assertThat(channelBigDecimalEntry.getKey().getReadingType().getMRID()).isEqualTo(code);
                assertThat(channelBigDecimalEntry.getValue().getValue()).isEqualTo(readingValue);
            }
        }
    }

    @Test
    @Transactional
    public void testGetLoadProfileDataClippedOnLastReading() {
        BigDecimal readingValue = new BigDecimal(5432.32);
        Instant readingTimeStamp_A = Instant.ofEpochMilli(1396138500000L); // 3/30/2014, 01:15:00 AM
        Instant readingTimeStamp_B = Instant.ofEpochMilli(1396155600000L); // 3/30/2014, 05:15:00 AM
        Instant dayStart = Instant.ofEpochMilli(1396137600000L); // 3/30/2014, 1:00:00 AM
        Instant dayEnd = Instant.ofEpochMilli(1396159200000L); // 3/30/2014, 8:00:00 AM !! 6 hours later!!
        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().period(TimeAttribute.MINUTE15).code();
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock1 = IntervalBlockImpl.of(code);
        intervalBlock1.addIntervalReading(IntervalReadingImpl.of(readingTimeStamp_A, readingValue));
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
        intervalBlock2.addIntervalReading(IntervalReadingImpl.of(readingTimeStamp_B, readingValue));
        meterReading.addIntervalBlock(intervalBlock1);
        meterReading.addIntervalBlock(intervalBlock2);

        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        List<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(Ranges.openClosed(dayStart, dayEnd));
        assertThat(readings).hasSize(20);  // only readings from 01:15:00 to 05:00:00, shouldn't have readings for period 05:00:00 - 08:00:00
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
        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder()
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
        assertThat(readings).hasSize(1);

        LoadProfileReading reading = readings.get(0);
        assertThat(reading.getRange().upperEndpoint()).isEqualTo(readingTimeStamp);
        assertThat(reading.getRange().lowerEndpoint()).isEqualTo(dayStart);
        assertThat(reading.getChannelValidationStates()).hasSize(1);
        for (Map.Entry<Channel, DataValidationStatus> channelBigDecimalEntry : reading.getChannelValidationStates().entrySet()) {
            assertThat(channelBigDecimalEntry.getKey().getReadingType().getMRID()).isEqualTo(code);
            DataValidationStatus status = channelBigDecimalEntry.getValue();
            assertThat(status.completelyValidated()).isFalse();
            assertThat(status.getReadingQualities()).isEmpty();
        }
    }

    @Test
    @Transactional
    public void testEditLoadProfileDataWithSeveralMeterActivations() {
        //COMU-1763
        Instant dayStart = ZonedDateTime.of(2015, 8, 14, 0, 15, 0, 0, ZoneOffset.UTC).toInstant();
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();

        device.activate(dayStart);
        device.deactivate(dayStart.plus(10, ChronoUnit.MINUTES));
        device.activate(dayStart.plus(10, ChronoUnit.MINUTES));
        device.save();

        String bulkReadingTypeCode = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().period(TimeAttribute.MINUTE15).accumulate(Accumulation.BULKQUANTITY).code();
        String deltaReadingTypeCode = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(bulkReadingTypeCode);
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(dayStart.plus(30, ChronoUnit.MINUTES), BigDecimal.valueOf(100)));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(dayStart.plus(45, ChronoUnit.MINUTES), BigDecimal.valueOf(200)));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(dayStart.plus(60, ChronoUnit.MINUTES), BigDecimal.valueOf(300)));
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(intervalBlock);
        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        List<LoadProfileReading> readings = reloadedDevice.getChannels().get(0).getChannelData(Range.openClosed(dayStart, dayStart.plus(60, ChronoUnit.MINUTES)));
        assertThat(readings).hasSize(4);
        assertThat(getRecordFromLoadProfileReading(readings, 0).getValue()).isEqualTo(BigDecimal.valueOf(300));
        assertThat(getRecordFromLoadProfileReading(readings, 1).getValue()).isEqualTo(BigDecimal.valueOf(200));
        assertThat(getRecordFromLoadProfileReading(readings, 2).getValue()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(readings.get(3).getChannelValues()).isEmpty();

        reloadedDevice.getChannels().get(0)
                .startEditingData()
                .editChannelData(Collections.singletonList(IntervalReadingImpl.of(dayStart.plus(60, ChronoUnit.MINUTES), BigDecimal.valueOf(50))))
                .editBulkChannelData(Arrays.asList(
                        IntervalReadingImpl.of(dayStart.plus(15, ChronoUnit.MINUTES), BigDecimal.valueOf(50)),
                        IntervalReadingImpl.of(dayStart.plus(30, ChronoUnit.MINUTES), BigDecimal.valueOf(150))
                ))
                .removeChannelData(Collections.singletonList(dayStart.plus(45, ChronoUnit.MINUTES)))
                .complete();

        readings = reloadedDevice.getChannels().get(0).getChannelData(Range.openClosed(dayStart, dayStart.plus(60, ChronoUnit.MINUTES)));
        assertThat(readings).hasSize(4);

        ReadingType bulkReadingType = inMemoryPersistence.getMeteringService().getReadingType(bulkReadingTypeCode).get();
        ReadingType deltaReadingType = inMemoryPersistence.getMeteringService().getReadingType(deltaReadingTypeCode).get();

        IntervalReadingRecord updatedReading = null;
        updatedReading = getRecordFromLoadProfileReading(readings, 0);//60
        assertThat(updatedReading.getQuantity(bulkReadingType).getValue()).isEqualTo(BigDecimal.valueOf(300));
        assertThat(updatedReading.getQuantity(deltaReadingType).getValue()).isEqualTo(BigDecimal.valueOf(50));

        assertThat(readings.get(1).getChannelValues()).isEmpty();//45

        updatedReading = getRecordFromLoadProfileReading(readings, 2);//30
        assertThat(updatedReading.getQuantity(bulkReadingType).getValue()).isEqualTo(BigDecimal.valueOf(150));
        assertThat(updatedReading.getQuantity(deltaReadingType).getValue()).isEqualTo(BigDecimal.valueOf(100));

        updatedReading = getRecordFromLoadProfileReading(readings, 3);//15
        assertThat(updatedReading.getQuantity(bulkReadingType).getValue()).isEqualTo(BigDecimal.valueOf(50));
        assertThat(updatedReading.getQuantity(deltaReadingType)).isNull();
    }

    private IntervalReadingRecord getRecordFromLoadProfileReading(List<LoadProfileReading> readings, int index) {
        return readings.get(index).getChannelValues().values().iterator().next();
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
        Instant dayStart = Instant.ofEpochMilli(1396137600000L); // 3/30/2014, 1:00:00 AM
        Instant dayEnd = Instant.ofEpochMilli(1396159200000L); // 3/30/2014, 8:00:00 AM !! 6 hours later!!
        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().period(TimeAttribute.MINUTE15).code();
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(code);
        Instant readingTimeStamp = dayEnd;
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(readingTimeStamp, readingValue));
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
        Instant previousReadingTimeStamp = Instant.ofEpochMilli(1396136700000L); // 3/30/2014, 0:45:00 AM
        intervalBlock2.addIntervalReading(IntervalReadingImpl.of(previousReadingTimeStamp, BigDecimal.ZERO));
        meterReading.addIntervalBlock(intervalBlock);
        meterReading.addIntervalBlock(intervalBlock2);

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

    private DeviceConfiguration createSetupWithMultiplierRegisterSpec() {
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = this.createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        deviceType.addRegisterType(registerType1);
        deviceType.addRegisterType(registerType2);
        DeviceType.DeviceConfigurationBuilder configurationWithRegisterTypes = deviceType.newConfiguration("ConfigurationWithRegisterTypes");
        NumericalRegisterSpec.Builder registerSpecBuilder1 = configurationWithRegisterTypes.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.numberOfFractionDigits(0);
        registerSpecBuilder1.overflowValue(overflowValue);
        registerSpecBuilder1.useMultiplierWithCalculatedReadingType(forwardBulkPrimaryEnergyReadingType);

        NumericalRegisterSpec.Builder registerSpecBuilder2 = configurationWithRegisterTypes.newNumericalRegisterSpec(registerType2);
        registerSpecBuilder2.numberOfFractionDigits(0);
        registerSpecBuilder2.overflowValue(overflowValue);
        DeviceConfiguration deviceConfiguration = configurationWithRegisterTypes.add();
        deviceConfiguration.activate();
        return deviceConfiguration;
    }

    @Test
    @Transactional
    public void setMultiplierTest() {
        DeviceConfiguration deviceConfiguration = createSetupWithMultiplierRegisterSpec();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "setMultiplierTest", "setMultiplierTest");
        device.save();

        assertThat(device.getMeterActivationsMostRecentFirst()).isEmpty();
        BigDecimal multiplier = BigDecimal.TEN;
        device.setMultiplier(multiplier);
        assertThat(device.getMeterActivationsMostRecentFirst()).hasSize(1);
        assertThat(device.getMultiplier()).isEqualTo(multiplier);
    }

    @Test
    @Transactional
    public void setSameMultiplierDoesNotCreateNewMeterActivationTest() {
        DeviceConfiguration deviceConfiguration = createSetupWithMultiplierRegisterSpec();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "setMultiplierTest", "setMultiplierTest");
        device.save();

        assertThat(device.getMeterActivationsMostRecentFirst()).isEmpty();
        BigDecimal multiplier = BigDecimal.TEN;
        Instant initialStart = freezeClock(2015, 11, 25);
        device.setMultiplier(multiplier);
        freezeClock(2015, 11, 26);
        device.setMultiplier(multiplier);
        freezeClock(2015, 11, 30);
        device.setMultiplier(multiplier);
        assertThat(device.getMeterActivationsMostRecentFirst()).hasSize(1);
        assertThat(device.getMultiplier()).isEqualTo(multiplier);
        assertThat(device.getCurrentMeterActivation().get().getStart()).isEqualTo(initialStart);
    }

    @Test
    @Transactional
    public void setMultiplierOfOneDoesNotCreateMeterActivationTest() {
        DeviceConfiguration deviceConfiguration = createSetupWithMultiplierRegisterSpec();

        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "setMultiplierOfOneDoesNotCreateMeterActivationTest", "setMultiplierOfOneDoesNotCreateMeterActivationTest");
        device.save();

        assertThat(device.getMeterActivationsMostRecentFirst()).isEmpty();
        BigDecimal multiplier = BigDecimal.ONE;
        device.setMultiplier(multiplier);
        assertThat(device.getMeterActivationsMostRecentFirst()).isEmpty();
        assertThat(device.getMultiplier()).isEqualTo(multiplier);
    }

    @Test
    @Transactional
    public void settingMultiplierBackToOneShouldRemovePreviouslyDefinedMultiplierTest() {
        DeviceConfiguration deviceConfiguration = createSetupWithMultiplierRegisterSpec();

        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "settingMultiplierBackToOneShouldRemovePreviouslyDefinedMultiplierTest", "settingMultiplierBackToOneShouldRemovePreviouslyDefinedMultiplierTest");
        device.save();

        assertThat(device.getMeterActivationsMostRecentFirst()).isEmpty();
        device.setMultiplier(BigDecimal.TEN);
        device.setMultiplier(BigDecimal.ONE);
        device.setMultiplier(BigDecimal.ONE);
        device.setMultiplier(BigDecimal.ONE);
        device.setMultiplier(BigDecimal.ONE);
        assertThat(device.getMeterActivationsMostRecentFirst()).hasSize(2);
        assertThat(device.getMultiplier()).isEqualTo(BigDecimal.ONE);
        assertThat(device.getCurrentMeterActivation().get().getMultipliers()).isEmpty();
    }

    @Test
    @Transactional
    @Expected(value = MultiplierConfigurationException.class, message = "The multiplier should be larger than zero")
    public void settingMultiplierWithValueZeroShouldFailTest() {
        DeviceConfiguration deviceConfiguration = createSetupWithMultiplierRegisterSpec();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "settingMultiplierWithValueZeroShouldFailTest", "settingMultiplierWithValueZeroShouldFailTest");
        device.save();

        device.setMultiplier(BigDecimal.ZERO);
    }

    @Test
    @Transactional
    @Expected(value = MultiplierConfigurationException.class, message = "The multiplier exceeds the max value " + Integer.MAX_VALUE)
    public void settingMultiplierLargerThanMaxIntShouldFailTest() {
        DeviceConfiguration deviceConfiguration = createSetupWithMultiplierRegisterSpec();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "settingMultiplierLargerThanMaxIntShouleFailTest", "settingMultiplierLargerThanMaxIntShouleFailTest");
        device.save();

        device.setMultiplier(BigDecimal.valueOf(Long.MAX_VALUE));
    }


    @Test
    @Transactional
    public void createMeterConfigurationForMultipliedRegisterSpecTest() {
        DeviceConfiguration deviceConfiguration = createSetupWithMultiplierRegisterSpec();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithMultiplierOnRegister", "DeviceWithMultiplierOnRegister");
        device.save();
        device.setMultiplier(BigDecimal.TEN);

        Optional<MeterConfiguration> meterConfigurationOptional = inMemoryPersistence.getMeteringService().findMeter(device.getId()).get().getConfiguration(inMemoryPersistence.getClock().instant());
        assertThat(meterConfigurationOptional).isPresent();
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).hasSize(2);
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).haveExactly(1, new Condition<MeterReadingTypeConfiguration>() {
            @Override
            public boolean matches(MeterReadingTypeConfiguration value) {
                return value.getMeasured().getMRID().equals(forwardBulkSecondaryEnergyReadingType.getMRID()) &&
                        value.getCalculated().get().getMRID().equals(forwardBulkPrimaryEnergyReadingType.getMRID());
            }
        });
    }

    @Test
    @Transactional
    public void createMeterConfigurationForMultipliedRegisterSpecWithoutMultiplierSetOnDeviceTest() {
        DeviceConfiguration deviceConfiguration = createSetupWithMultiplierRegisterSpec();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithMultiplierOnRegister2", "DeviceWithMultiplierOnRegister");
        device.save();

        Optional<MeterConfiguration> meterConfigurationOptional = inMemoryPersistence.getMeteringService().findMeter(device.getId()).get().getConfiguration(inMemoryPersistence.getClock().instant());
        assertThat(meterConfigurationOptional).isPresent();
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).hasSize(2);
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).haveExactly(1, new Condition<MeterReadingTypeConfiguration>() {
            @Override
            public boolean matches(MeterReadingTypeConfiguration value) {
                return value.getMeasured().getMRID().equals(forwardBulkSecondaryEnergyReadingType.getMRID()) &&
                        !value.getCalculated().isPresent();
            }
        });
    }

    @Test
    @Transactional
    public void createMeterConfigurationForMultipliedRegisterSpecsInSameMeterConfigurationTest() {
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = this.createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        deviceType.addRegisterType(registerType1);
        deviceType.addRegisterType(registerType2);
        DeviceType.DeviceConfigurationBuilder configurationWithRegisterTypes = deviceType.newConfiguration("ConfigurationWithRegisterTypes");
        NumericalRegisterSpec.Builder registerSpecBuilder1 = configurationWithRegisterTypes.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.numberOfFractionDigits(0);
        registerSpecBuilder1.overflowValue(overflowValue);
        registerSpecBuilder1.useMultiplierWithCalculatedReadingType(forwardBulkPrimaryEnergyReadingType);

        NumericalRegisterSpec.Builder registerSpecBuilder2 = configurationWithRegisterTypes.newNumericalRegisterSpec(registerType2);
        registerSpecBuilder2.numberOfFractionDigits(0);
        registerSpecBuilder2.overflowValue(overflowValue);
        registerSpecBuilder2.useMultiplierWithCalculatedReadingType(reverseBulkPrimaryEnergyReadingType);
        DeviceConfiguration deviceConfiguration = configurationWithRegisterTypes.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithMultiplierOnRegister", "DeviceWithMultiplierOnRegister");
        device.save();
        device.setMultiplier(BigDecimal.TEN);

        Optional<MeterConfiguration> meterConfigurationOptional = inMemoryPersistence.getMeteringService().findMeter(device.getId()).get().getConfiguration(inMemoryPersistence.getClock().instant());
        assertThat(meterConfigurationOptional).isPresent();
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).hasSize(2);
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).haveExactly(1, new Condition<MeterReadingTypeConfiguration>() {
            @Override
            public boolean matches(MeterReadingTypeConfiguration value) {
                return value.getMeasured().getMRID().equals(forwardBulkSecondaryEnergyReadingType.getMRID()) &&
                        value.getCalculated().get().getMRID().equals(forwardBulkPrimaryEnergyReadingType.getMRID());
            }
        });
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).haveExactly(1, new Condition<MeterReadingTypeConfiguration>() {
            @Override
            public boolean matches(MeterReadingTypeConfiguration value) {
                return value.getMeasured().getMRID().equals(reverseBulkSecondaryEnergyReadingType.getMRID()) &&
                        value.getCalculated().get().getMRID().equals(reverseBulkPrimaryEnergyReadingType.getMRID());
            }
        });
    }

    @Test
    @Transactional
    public void createMeterConfigurationForMultipliedRegisterSpecsInSameMeterConfigurationWithoutMultiplierOnDeviceTest() {
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = this.createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        deviceType.addRegisterType(registerType1);
        deviceType.addRegisterType(registerType2);
        DeviceType.DeviceConfigurationBuilder configurationWithRegisterTypes = deviceType.newConfiguration("ConfigurationWithRegisterTypes");
        NumericalRegisterSpec.Builder registerSpecBuilder1 = configurationWithRegisterTypes.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.numberOfFractionDigits(0);
        registerSpecBuilder1.overflowValue(overflowValue);
        registerSpecBuilder1.useMultiplierWithCalculatedReadingType(forwardBulkPrimaryEnergyReadingType);

        NumericalRegisterSpec.Builder registerSpecBuilder2 = configurationWithRegisterTypes.newNumericalRegisterSpec(registerType2);
        registerSpecBuilder2.numberOfFractionDigits(0);
        registerSpecBuilder2.overflowValue(overflowValue);
        registerSpecBuilder2.useMultiplierWithCalculatedReadingType(reverseBulkPrimaryEnergyReadingType);
        DeviceConfiguration deviceConfiguration = configurationWithRegisterTypes.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithMultiplierOnRegister", "DeviceWithMultiplierOnRegister");
        device.save();

        Optional<MeterConfiguration> meterConfigurationOptional = inMemoryPersistence.getMeteringService().findMeter(device.getId()).get().getConfiguration(inMemoryPersistence.getClock().instant());
        assertThat(meterConfigurationOptional).isPresent();
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).hasSize(2);
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).haveExactly(1, new Condition<MeterReadingTypeConfiguration>() {
            @Override
            public boolean matches(MeterReadingTypeConfiguration value) {
                return value.getMeasured().getMRID().equals(forwardBulkSecondaryEnergyReadingType.getMRID()) &&
                        !value.getCalculated().isPresent();
            }
        });
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).haveExactly(1, new Condition<MeterReadingTypeConfiguration>() {
            @Override
            public boolean matches(MeterReadingTypeConfiguration value) {
                return value.getMeasured().getMRID().equals(reverseBulkSecondaryEnergyReadingType.getMRID()) &&
                        !value.getCalculated().isPresent();
            }
        });
    }

    @Test
    @Transactional
    public void createSingleMeterConfigurationForChannelSpecsAndRegisterSpecsTest() {
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("LoadProfileType", loadProfileObisCode, TimeDuration.months(1), Arrays.asList(registerType1, registerType2));
        loadProfileType.save();
        ChannelType channelTypeForRegisterType1 = loadProfileType.findChannelType(registerType1).get();
        ChannelType channelTypeForRegisterType2 = loadProfileType.findChannelType(registerType2).get();
        deviceType.addLoadProfileType(loadProfileType);
        deviceType.addRegisterType(registerType1);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("ConfigurationWithRegisterTypes");
        NumericalRegisterSpec.Builder registerSpecBuilder1 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.numberOfFractionDigits(0);
        registerSpecBuilder1.overflowValue(overflowValue);
        registerSpecBuilder1.useMultiplierWithCalculatedReadingType(forwardBulkPrimaryEnergyReadingType);
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfigurationBuilder.newLoadProfileSpec(loadProfileType);
        BigDecimal overflow = BigDecimal.valueOf(9999);
        final int nbrOfFractionDigits = 3;
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType1, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow)
                .useMultiplierWithCalculatedReadingType(forwardDeltaPrimaryMonthlyEnergyReadingType);
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType2, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow);


        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithMultipliers", "DeviceWithMultipliers");
        device.save();
        device.setMultiplier(BigDecimal.TEN);

        Optional<MeterConfiguration> meterConfigurationOptional = inMemoryPersistence.getMeteringService().findMeter(device.getId()).get().getConfiguration(inMemoryPersistence.getClock().instant());
        assertThat(meterConfigurationOptional).isPresent();
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).hasSize(3);
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).haveExactly(1, new Condition<MeterReadingTypeConfiguration>() {
            @Override
            public boolean matches(MeterReadingTypeConfiguration value) {
                return value.getMeasured().getMRID().equals(forwardBulkSecondaryEnergyReadingType.getMRID()) &&
                        value.getCalculated().get().getMRID().equals(forwardBulkPrimaryEnergyReadingType.getMRID());
            }
        });
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).haveExactly(1, new Condition<MeterReadingTypeConfiguration>() {
            @Override
            public boolean matches(MeterReadingTypeConfiguration value) {
                return value.getMeasured().getMRID().equals(getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().period(MacroPeriod.MONTHLY).code()) &&
                        value.getCalculated().get().getMRID().equals(getForwardBulkPrimaryEnergyReadingType().period(MacroPeriod.MONTHLY).code()) &&
                        (value.getOverflowValue().isPresent() && value.getOverflowValue().get().compareTo(overflow) == 0) &&
                        value.getNumberOfFractionDigits().getAsInt() == nbrOfFractionDigits;
            }
        });
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).haveExactly(1, new Condition<MeterReadingTypeConfiguration>() {
            @Override
            public boolean matches(MeterReadingTypeConfiguration value) {
                return value.getMeasured().getMRID().equals(getReverseSecondaryBulkReadingTypeCodeBuilder().period(MacroPeriod.MONTHLY).code()) &&
                        !value.getCalculated().isPresent() &&
                        (value.getOverflowValue().isPresent() && value.getOverflowValue().get().compareTo(overflow) == 0) &&
                        value.getNumberOfFractionDigits().getAsInt() == nbrOfFractionDigits;
            }
        });
    }

    @Test
    @Transactional
    public void createSingleMeterConfigurationForChannelSpecsAndRegisterSpecsWithoutMultiplierOnDeviceTest() {
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("LoadProfileType", loadProfileObisCode, TimeDuration.months(1), Arrays.asList(registerType1, registerType2));
        loadProfileType.save();
        ChannelType channelTypeForRegisterType1 = loadProfileType.findChannelType(registerType1).get();
        ChannelType channelTypeForRegisterType2 = loadProfileType.findChannelType(registerType2).get();
        deviceType.addLoadProfileType(loadProfileType);
        deviceType.addRegisterType(registerType1);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("ConfigurationWithRegisterTypes");
        NumericalRegisterSpec.Builder registerSpecBuilder1 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.numberOfFractionDigits(0);
        registerSpecBuilder1.overflowValue(overflowValue);
        registerSpecBuilder1.useMultiplierWithCalculatedReadingType(forwardBulkPrimaryEnergyReadingType);
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfigurationBuilder.newLoadProfileSpec(loadProfileType);
        BigDecimal overflow = BigDecimal.valueOf(9999);
        final int nbrOfFractionDigits = 3;
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType1, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow)
                .useMultiplierWithCalculatedReadingType(forwardDeltaPrimaryMonthlyEnergyReadingType);
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType2, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow);


        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithMultipliers", "DeviceWithMultipliers");
        device.save();

        Optional<MeterConfiguration> meterConfigurationOptional = inMemoryPersistence.getMeteringService().findMeter(device.getId()).get().getConfiguration(inMemoryPersistence.getClock().instant());
        assertThat(meterConfigurationOptional).isPresent();
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).hasSize(3);
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).haveExactly(1, new Condition<MeterReadingTypeConfiguration>() {
            @Override
            public boolean matches(MeterReadingTypeConfiguration value) {
                return value.getMeasured().getMRID().equals(forwardBulkSecondaryEnergyReadingType.getMRID()) &&
                        !value.getCalculated().isPresent();
            }
        });
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).haveExactly(1, new Condition<MeterReadingTypeConfiguration>() {
            @Override
            public boolean matches(MeterReadingTypeConfiguration value) {
                return value.getMeasured().getMRID().equals(getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().period(MacroPeriod.MONTHLY).code()) &&
                        !value.getCalculated().isPresent() &&
                        (value.getOverflowValue().isPresent() && value.getOverflowValue().get().compareTo(overflow) == 0) &&
                        value.getNumberOfFractionDigits().getAsInt() == nbrOfFractionDigits;
            }
        });
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).haveExactly(1, new Condition<MeterReadingTypeConfiguration>() {
            @Override
            public boolean matches(MeterReadingTypeConfiguration value) {
                return value.getMeasured().getMRID().equals(getReverseSecondaryBulkReadingTypeCodeBuilder().period(MacroPeriod.MONTHLY).code()) &&
                        !value.getCalculated().isPresent() &&
                        (value.getOverflowValue().isPresent() && value.getOverflowValue().get().compareTo(overflow) == 0) &&
                        value.getNumberOfFractionDigits().getAsInt() == nbrOfFractionDigits;
            }
        });
    }

    @Test
    @Transactional
    public void overruleChannelOverFlowTest() {
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("LoadProfileType", loadProfileObisCode, TimeDuration.months(1), Arrays.asList(registerType1, registerType2));
        loadProfileType.save();
        ChannelType channelTypeForRegisterType1 = loadProfileType.findChannelType(registerType1).get();
        ChannelType channelTypeForRegisterType2 = loadProfileType.findChannelType(registerType2).get();
        deviceType.addLoadProfileType(loadProfileType);
        deviceType.addRegisterType(registerType1);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("overruleChannelOverFlowTest");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfigurationBuilder.newLoadProfileSpec(loadProfileType);
        BigDecimal overflow = BigDecimal.valueOf(99999999);
        final int nbrOfFractionDigits = 3;
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType1, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow)
                .useMultiplierWithCalculatedReadingType(forwardDeltaPrimaryMonthlyEnergyReadingType);
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType2, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow);

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", "OverruleTest");
        device.save();

        // business logic to check
        String channelReadingType = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().period(MacroPeriod.MONTHLY).code();
        Channel channel = device.getLoadProfiles().get(0).getChannels().stream().filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType)).findFirst().get();
        BigDecimal overruledOverflow = BigDecimal.valueOf(456123L);
        device.getChannelUpdaterFor(channel).setOverflowValue(overruledOverflow).update();

        Device reloadedDevice = getReloadedDevice(device);
        Channel updatedChannel = reloadedDevice.getLoadProfiles().get(0).getChannels().stream().filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType)).findFirst().get();
        assertThat(channel.getId() == updatedChannel.getId()); //just to make sure we have the same channel
        assertThat(updatedChannel.getOverflow().get()).isEqualTo(overruledOverflow);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.OVERFLOW_INCREASED + "}", property = "overruledOverflowValue")
    public void overruleChannelOverFlowWithLargerValueTest() {
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("LoadProfileType", loadProfileObisCode, TimeDuration.months(1), Arrays.asList(registerType1, registerType2));
        loadProfileType.save();
        ChannelType channelTypeForRegisterType1 = loadProfileType.findChannelType(registerType1).get();
        ChannelType channelTypeForRegisterType2 = loadProfileType.findChannelType(registerType2).get();
        deviceType.addLoadProfileType(loadProfileType);
        deviceType.addRegisterType(registerType1);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("overruleChannelOverFlowTest");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfigurationBuilder.newLoadProfileSpec(loadProfileType);
        BigDecimal overflow = BigDecimal.valueOf(9999);
        final int nbrOfFractionDigits = 3;
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType1, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow)
                .useMultiplierWithCalculatedReadingType(forwardDeltaPrimaryMonthlyEnergyReadingType);
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType2, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow);

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", "OverruleTest");
        device.save();

        // business logic to check
        String channelReadingType = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().period(MacroPeriod.MONTHLY).code();
        Channel channel = device.getLoadProfiles().get(0).getChannels().stream().filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType)).findFirst().get();
        BigDecimal overruledOverflow = BigDecimal.valueOf(456123L);
        device.getChannelUpdaterFor(channel).setOverflowValue(overruledOverflow).update();
    }

    @Test
    @Transactional
    public void overruleChannelFractionDigitsTest() {
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("LoadProfileType", loadProfileObisCode, TimeDuration.months(1), Arrays.asList(registerType1, registerType2));
        loadProfileType.save();
        ChannelType channelTypeForRegisterType1 = loadProfileType.findChannelType(registerType1).get();
        ChannelType channelTypeForRegisterType2 = loadProfileType.findChannelType(registerType2).get();
        deviceType.addLoadProfileType(loadProfileType);
        deviceType.addRegisterType(registerType1);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("overruleChannelFractionDigitsTest");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfigurationBuilder.newLoadProfileSpec(loadProfileType);
        BigDecimal overflow = BigDecimal.valueOf(9999);
        final int nbrOfFractionDigits = 3;
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType1, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow)
                .useMultiplierWithCalculatedReadingType(forwardDeltaPrimaryMonthlyEnergyReadingType);
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType2, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow);

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", "OverruleTest");
        device.save();

        // business logic to check
        String channelReadingType = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().period(MacroPeriod.MONTHLY).code();
        Channel channel = device.getLoadProfiles().get(0).getChannels().stream().filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType)).findFirst().get();
        Integer overruledFractionDigits = 5;
        device.getChannelUpdaterFor(channel).setNumberOfFractionDigits(overruledFractionDigits).update();

        Device reloadedDevice = getReloadedDevice(device);
        Channel updatedChannel = reloadedDevice.getLoadProfiles().get(0).getChannels().stream().filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType)).findFirst().get();
        assertThat(channel.getId() == updatedChannel.getId()); //just to make sure we have the same channel
        assertThat(updatedChannel.getNrOfFractionDigits()).isEqualTo(overruledFractionDigits);
    }

    @Test
    @Transactional
    public void overruleChannelObisCodeTest() {
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("LoadProfileType", loadProfileObisCode, TimeDuration.months(1), Arrays.asList(registerType1, registerType2));
        loadProfileType.save();
        ChannelType channelTypeForRegisterType1 = loadProfileType.findChannelType(registerType1).get();
        ChannelType channelTypeForRegisterType2 = loadProfileType.findChannelType(registerType2).get();
        deviceType.addLoadProfileType(loadProfileType);
        deviceType.addRegisterType(registerType1);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("overruleChannelObisCodeTest");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfigurationBuilder.newLoadProfileSpec(loadProfileType);
        BigDecimal overflow = BigDecimal.valueOf(9999);
        final int nbrOfFractionDigits = 3;
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType1, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow)
                .useMultiplierWithCalculatedReadingType(forwardDeltaPrimaryMonthlyEnergyReadingType);
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType2, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow);

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", "OverruleTest");
        device.save();

        // business logic to check
        String channelReadingType = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().period(MacroPeriod.MONTHLY).code();
        Channel channel = device.getLoadProfiles().get(0).getChannels().stream().filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType)).findFirst().get();
        ObisCode overruledObisCode = ObisCode.fromString("1.2.3.4.5.6");
        device.getChannelUpdaterFor(channel).setObisCode(overruledObisCode).update();

        Device reloadedDevice = getReloadedDevice(device);
        Channel updatedChannel = reloadedDevice.getLoadProfiles().get(0).getChannels().stream().filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType)).findFirst().get();
        assertThat(channel.getId() == updatedChannel.getId()); //just to make sure we have the same channel
        assertThat(updatedChannel.getObisCode()).isEqualTo(overruledObisCode);
    }


    @Test
    @Transactional
    public void overruleRegisterObisCodeTest() {
        final int nbrOfFractionDigits = 3;
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        deviceType.addRegisterType(registerType1);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("ConfigurationWithRegisterTypes");
        NumericalRegisterSpec.Builder registerSpecBuilder1 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.numberOfFractionDigits(nbrOfFractionDigits);
        registerSpecBuilder1.overflowValue(overflowValue);
        registerSpecBuilder1.useMultiplierWithCalculatedReadingType(forwardBulkPrimaryEnergyReadingType);

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", "OverruleTest");
        device.save();

        // business logic to check
        String registerReadingType = forwardBulkSecondaryEnergyReadingType.getMRID();
        Register<?, ?> register = device.getRegisters().stream().filter(sRegister -> sRegister.getReadingType().getMRID().equals(registerReadingType)).findFirst().get();
        ObisCode overruledObisCode = ObisCode.fromString("1.2.3.4.5.6");
        device.getRegisterUpdaterFor(register).setObisCode(overruledObisCode).update();

        Device reloadedDevice = getReloadedDevice(device);
        Register<?, ?> updatedRegister = reloadedDevice.getRegisters().stream().filter(sRegister -> sRegister.getReadingType().getMRID().equals(registerReadingType)).findFirst().get();
        assertThat(register.getRegisterSpecId() == updatedRegister.getRegisterSpecId()); //just to make sure we have the same channel
        assertThat(updatedRegister.getDeviceObisCode()).isEqualTo(overruledObisCode);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DUPLICATE_REGISTER_OBISCODE + "}")
    public void overruleRegisterDuplicateObisCodeTest() {
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        deviceType.addRegisterType(registerType1);
        deviceType.addRegisterType(registerType2);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("overruleChannelObisCodeTest");
        final int nbrOfFractionDigits = 3;
        NumericalRegisterSpec.Builder registerSpecBuilder1 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.numberOfFractionDigits(nbrOfFractionDigits);
        registerSpecBuilder1.overflowValue(overflowValue);
        NumericalRegisterSpec.Builder registerSpecBuilder2 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType2);
        registerSpecBuilder2.numberOfFractionDigits(nbrOfFractionDigits);
        registerSpecBuilder2.overflowValue(overflowValue);

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", "OverruleTest");
        device.save();

        // business logic to check
        String registerReadingType = forwardBulkSecondaryEnergyReadingType.getMRID();
        Register<?, ?> register = device.getRegisters().stream().filter(sRegister -> sRegister.getReadingType().getMRID().equals(registerReadingType)).findFirst().get();

        device.getRegisterUpdaterFor(register).setObisCode(reverseEnergyObisCode).update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DUPLICATE_CHANNEL_OBISCODE + "}")
    public void overruleChannelDuplicateObisCodeTest() {
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("LoadProfileType", loadProfileObisCode, TimeDuration.months(1), Arrays.asList(registerType1, registerType2));
        loadProfileType.save();
        ChannelType channelTypeForRegisterType1 = loadProfileType.findChannelType(registerType1).get();
        ChannelType channelTypeForRegisterType2 = loadProfileType.findChannelType(registerType2).get();
        deviceType.addLoadProfileType(loadProfileType);
        deviceType.addRegisterType(registerType1);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("overruleChannelObisCodeTest");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfigurationBuilder.newLoadProfileSpec(loadProfileType);
        BigDecimal overflow = BigDecimal.valueOf(9999);
        final int nbrOfFractionDigits = 3;
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType1, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow)
                .useMultiplierWithCalculatedReadingType(forwardDeltaPrimaryMonthlyEnergyReadingType);
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType2, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow);

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", "OverruleTest");
        device.save();

        // business logic to check
        String channelReadingType = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().period(MacroPeriod.MONTHLY).code();
        Channel channel = device.getLoadProfiles().get(0).getChannels().stream().filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType)).findFirst().get();

        device.getChannelUpdaterFor(channel).setObisCode(reverseEnergyObisCode).update();

    }

    @Test
    @Transactional
    public void overruleRegisterOverflowTest() {
        final int nbrOfFractionDigits = 3;
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        deviceType.addRegisterType(registerType1);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("ConfigurationWithRegisterTypes");
        NumericalRegisterSpec.Builder registerSpecBuilder1 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.numberOfFractionDigits(nbrOfFractionDigits);
        registerSpecBuilder1.overflowValue(overflowValue);
        registerSpecBuilder1.useMultiplierWithCalculatedReadingType(forwardBulkPrimaryEnergyReadingType);

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", "OverruleTest");
        device.save();

        // business logic to check
        String registerReadingType = forwardBulkSecondaryEnergyReadingType.getMRID();
        Register<?, ?> register = device.getRegisters().stream().filter(sRegister -> sRegister.getReadingType().getMRID().equals(registerReadingType)).findFirst().get();
        BigDecimal overruledOverflow = BigDecimal.valueOf(6851L);
        device.getRegisterUpdaterFor(register).setOverflowValue(overruledOverflow).update();

        Device reloadedDevice = getReloadedDevice(device);
        Register<?, ?> updatedRegister = reloadedDevice.getRegisters().stream().filter(sRegister -> sRegister.getReadingType().getMRID().equals(registerReadingType)).findFirst().get();
        assertThat(register.getRegisterSpecId() == updatedRegister.getRegisterSpecId()); //just to make sure we have the same channel
        assertThat(((NumericalRegister) updatedRegister).getOverflow().get()).isEqualTo(overruledOverflow);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.OVERFLOW_INCREASED + "}", property = "overruledOverflow")
    public void overruleRegisterOverflowExceedConfigValueTest() {
        final int nbrOfFractionDigits = 3;
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        deviceType.addRegisterType(registerType1);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("ConfigurationWithRegisterTypes");
        NumericalRegisterSpec.Builder registerSpecBuilder1 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.numberOfFractionDigits(nbrOfFractionDigits);
        registerSpecBuilder1.overflowValue(overflowValue);
        registerSpecBuilder1.useMultiplierWithCalculatedReadingType(forwardBulkPrimaryEnergyReadingType);

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", "OverruleTest");
        device.save();

        // business logic to check
        String registerReadingType = forwardBulkSecondaryEnergyReadingType.getMRID();
        Register<?, ?> register = device.getRegisters().stream().filter(sRegister -> sRegister.getReadingType().getMRID().equals(registerReadingType)).findFirst().get();
        BigDecimal overruledOverflow = BigDecimal.valueOf(9999999999L);
        device.getRegisterUpdaterFor(register).setOverflowValue(overruledOverflow).update();
    }

    @Test
    @Transactional
    public void overruleRegisterFractionDigitsTest() {
        final int nbrOfFractionDigits = 3;
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        deviceType.addRegisterType(registerType1);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("overruleRegisterFractionDigitsTest");
        NumericalRegisterSpec.Builder registerSpecBuilder1 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.numberOfFractionDigits(nbrOfFractionDigits);
        registerSpecBuilder1.overflowValue(overflowValue);
        registerSpecBuilder1.useMultiplierWithCalculatedReadingType(forwardBulkPrimaryEnergyReadingType);

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", "OverruleTest");
        device.save();

        // business logic to check
        String registerReadingType = forwardBulkSecondaryEnergyReadingType.getMRID();
        Register<?, ?> register = device.getRegisters().stream().filter(sRegister -> sRegister.getReadingType().getMRID().equals(registerReadingType)).findFirst().get();
        Integer overruledFractionDigits = 1;
        device.getRegisterUpdaterFor(register).setNumberOfFractionDigits(overruledFractionDigits).update();

        Device reloadedDevice = getReloadedDevice(device);
        Register<?, ?> updatedRegister = reloadedDevice.getRegisters().stream().filter(sRegister -> sRegister.getReadingType().getMRID().equals(registerReadingType)).findFirst().get();
        assertThat(register.getRegisterSpecId() == updatedRegister.getRegisterSpecId()); //just to make sure we have the same channel
        assertThat(((NumericalRegister) updatedRegister).getNumberOfFractionDigits()).isEqualTo(overruledFractionDigits);
    }

    @Test
    @Transactional
    public void noOverflowRequiredOnDeltaUpdateTest() {
        int nbrOfFractionDigits = 3;
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardDeltaSecondaryEnergyReadingType);
        deviceType.addRegisterType(registerType1);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("ConfigurationWithRegisterTypes");
        NumericalRegisterSpec.Builder registerSpecBuilder1 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.numberOfFractionDigits(nbrOfFractionDigits);

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "noOverflowRequiredOnDeltaUpdateTest", "noOverflowRequiredOnDeltaUpdateTest");
        device.save();

        // business logic to check
        String registerReadingType = forwardDeltaSecondaryEnergyReadingType.getMRID();
        Register<?, ?> register = device.getRegisters().stream().filter(sRegister -> sRegister.getReadingType().getMRID().equals(registerReadingType)).findFirst().get();
        int overruledNbrOfFractionDigits = 1;
        device.getRegisterUpdaterFor(register).setNumberOfFractionDigits(overruledNbrOfFractionDigits).update();

        Device reloadedDevice = getReloadedDevice(device);
        Register<?, ?> updatedRegister = reloadedDevice.getRegisters().stream().filter(sRegister -> sRegister.getReadingType().getMRID().equals(registerReadingType)).findFirst().get();
        assertThat(register.getRegisterSpecId() == updatedRegister.getRegisterSpecId()); //just to make sure we have the same channel
        assertThat(((NumericalRegister) updatedRegister).getOverflow().isPresent()).isFalse();
        assertThat(((NumericalRegister) updatedRegister).getNumberOfFractionDigits()).isEqualTo(overruledNbrOfFractionDigits);
    }

    @Test
    @Transactional
    public void overruleOverflowOnDeltaWhenNoOverflowOnConfigTest() {
        int nbrOfFractionDigits = 3;
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardDeltaSecondaryEnergyReadingType);
        deviceType.addRegisterType(registerType1);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("ConfigurationWithRegisterTypes");
        NumericalRegisterSpec.Builder registerSpecBuilder1 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.numberOfFractionDigits(nbrOfFractionDigits);

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "overruleOverflowOnDeltaWhenNoOverflowOnConfigTest", "overruleOverflowOnDeltaWhenNoOverflowOnConfigTest");
        device.save();

        // business logic to check
        String registerReadingType = forwardDeltaSecondaryEnergyReadingType.getMRID();
        Register<?, ?> register = device.getRegisters().stream().filter(sRegister -> sRegister.getReadingType().getMRID().equals(registerReadingType)).findFirst().get();
        BigDecimal overruledNbrOfFractionDigits = BigDecimal.valueOf(123L);
        device.getRegisterUpdaterFor(register).setOverflowValue(overruledNbrOfFractionDigits).update();

        Device reloadedDevice = getReloadedDevice(device);
        Register<?, ?> updatedRegister = reloadedDevice.getRegisters().stream().filter(sRegister -> sRegister.getReadingType().getMRID().equals(registerReadingType)).findFirst().get();
        assertThat(register.getRegisterSpecId() == updatedRegister.getRegisterSpecId()); //just to make sure we have the same channel
        assertThat(((NumericalRegister) updatedRegister).getOverflow().isPresent()).isTrue();
        assertThat(((NumericalRegister) updatedRegister).getOverflow().get()).isEqualTo(overruledNbrOfFractionDigits);
    }

    @Test
    @Transactional
    public void noOverflowRequiredOnDeltaChannelUpdateTest() {
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardDeltaSecondaryEnergyReadingType);
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("LoadProfileType", loadProfileObisCode, TimeDuration.months(1), Arrays.asList(registerType1));
        loadProfileType.save();
        ChannelType channelTypeForRegisterType1 = loadProfileType.findChannelType(registerType1).get();
        deviceType.addLoadProfileType(loadProfileType);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("noOverflowRequiredOnDeltaChannelUpdateTest");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfigurationBuilder.newLoadProfileSpec(loadProfileType);
        BigDecimal overflow = BigDecimal.valueOf(9999);
        final int nbrOfFractionDigits = 3;
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType1, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits);

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", "OverruleTest");
        device.save();

        // business logic to check
        String channelReadingType = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().accumulate(Accumulation.DELTADELTA).period(MacroPeriod.MONTHLY).code();
        Channel channel = device.getLoadProfiles().get(0).getChannels().stream().filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType)).findFirst().get();
        Integer overruledNrOfFractionDigits = 4;
        device.getChannelUpdaterFor(channel).setNumberOfFractionDigits(overruledNrOfFractionDigits).update();

        Device reloadedDevice = getReloadedDevice(device);
        Channel updatedChannel = reloadedDevice.getLoadProfiles().get(0).getChannels().stream().filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType)).findFirst().get();
        assertThat(channel.getId() == updatedChannel.getId()); //just to make sure we have the same channel
        assertThat(updatedChannel.getNrOfFractionDigits()).isEqualTo(overruledNrOfFractionDigits);
        assertThat(updatedChannel.getOverflow().isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void overruleOverflowOnDeltaChannelWhenNotConfiguredOnConfigTest() {
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardDeltaSecondaryEnergyReadingType);
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("LoadProfileType", loadProfileObisCode, TimeDuration.months(1), Arrays.asList(registerType1));
        loadProfileType.save();
        ChannelType channelTypeForRegisterType1 = loadProfileType.findChannelType(registerType1).get();
        deviceType.addLoadProfileType(loadProfileType);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("overruleOverflowOnDeltaChannelWhenNotConfiguredOnConfigTest");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfigurationBuilder.newLoadProfileSpec(loadProfileType);
        BigDecimal overflow = BigDecimal.valueOf(9999);
        final int nbrOfFractionDigits = 3;
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType1, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits);

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", "OverruleTest");
        device.save();

        // business logic to check
        String channelReadingType = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().accumulate(Accumulation.DELTADELTA).period(MacroPeriod.MONTHLY).code();
        Channel channel = device.getLoadProfiles().get(0).getChannels().stream().filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType)).findFirst().get();
        BigDecimal overruledOverflow = BigDecimal.valueOf(987654L);
        device.getChannelUpdaterFor(channel).setOverflowValue(overruledOverflow).update();

        Device reloadedDevice = getReloadedDevice(device);
        Channel updatedChannel = reloadedDevice.getLoadProfiles().get(0).getChannels().stream().filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType)).findFirst().get();
        assertThat(channel.getId() == updatedChannel.getId()); //just to make sure we have the same channel
        assertThat(updatedChannel.getOverflow().isPresent()).isTrue();
        assertThat(updatedChannel.getOverflow().get()).isEqualTo(overruledOverflow);
    }

    @Test
    @Transactional
    public void successfulCreateNoAdditionalComTaskExecutionsCreatedTest() {
        DeviceConfiguration deviceConfiguration = super.deviceConfiguration;
        ComTask comTask_1 = inMemoryPersistence.getTaskService().newComTask("Status information task");
        comTask_1.createStatusInformationTask();
        comTask_1.save();
        ComTask comTask_2 = inMemoryPersistence.getTaskService().newComTask("Messages task");
        comTask_2.createStatusInformationTask();
        comTask_2.save();

        deviceConfiguration.enableComTask(
                comTask_1,
                deviceConfiguration.getSecurityPropertySets().stream().findFirst().get(),
                deviceConfiguration.getProtocolDialectConfigurationPropertiesList().stream().findFirst().get())
                .setIgnoreNextExecutionSpecsForInbound(false)
                .add();
        deviceConfiguration.enableComTask(
                comTask_2,
                deviceConfiguration.getSecurityPropertySets().stream().findFirst().get(),
                deviceConfiguration.getProtocolDialectConfigurationPropertiesList().stream().findFirst().get())
                .setIgnoreNextExecutionSpecsForInbound(false)
                .add();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getComTaskExecutions()).isEmpty(); // I don't expect any ComTaskExecution was created
    }

    @Transactional
    public void successfulCreateWithAdditionalComTaskExecutionsCreatedTest() {
        DeviceConfiguration deviceConfiguration = super.deviceConfiguration;
        ComTask comTask_1 = inMemoryPersistence.getTaskService().newComTask("Status information task");
        comTask_1.createStatusInformationTask();
        comTask_1.save();
        ComTask comTask_2 = inMemoryPersistence.getTaskService().newComTask("Messages task");
        comTask_2.createStatusInformationTask();
        comTask_2.save();

        deviceConfiguration.enableComTask(
                comTask_1,
                deviceConfiguration.getSecurityPropertySets().stream().findFirst().get(),
                deviceConfiguration.getProtocolDialectConfigurationPropertiesList().stream().findFirst().get())
                .setIgnoreNextExecutionSpecsForInbound(false)
                .add();
        deviceConfiguration.enableComTask(
                comTask_2,
                deviceConfiguration.getSecurityPropertySets().stream().findFirst().get(),
                deviceConfiguration.getProtocolDialectConfigurationPropertiesList().stream().findFirst().get())
                .setIgnoreNextExecutionSpecsForInbound(true)
                .add();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getComTaskExecutions()).isNotEmpty(); // I expect a ComTaskExecution was created for comTask_2 (which was marked as ignoreNextExecutionSpecsForInbound)
        assertThat(reloadedDevice.getComTaskExecutions()).hasSize(1);
        assertThat(reloadedDevice.getComTaskExecutions().get(0).getComTasks().stream().mapToLong(ComTask::getId).toArray()).containsOnly(Long.valueOf(comTask_2.getId()));
    }

    private DeviceConfiguration createDeviceConfigurationWithTwoRegisterSpecs() {
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = this.createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        deviceType.addRegisterType(registerType1);
        deviceType.addRegisterType(registerType2);
        DeviceType.DeviceConfigurationBuilder configurationWithRegisterTypes = deviceType.newConfiguration("ConfigurationWithRegisterTypes");
        NumericalRegisterSpec.Builder registerSpecBuilder1 = configurationWithRegisterTypes.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.numberOfFractionDigits(0);
        registerSpecBuilder1.overflowValue(overflowValue);
        NumericalRegisterSpec.Builder registerSpecBuilder2 = configurationWithRegisterTypes.newNumericalRegisterSpec(registerType2);
        registerSpecBuilder2.numberOfFractionDigits(0);
        registerSpecBuilder2.overflowValue(overflowValue);
        DeviceConfiguration deviceConfiguration = configurationWithRegisterTypes.add();
        deviceConfiguration.activate();
        return deviceConfiguration;
    }

    private DeviceConfiguration createDeviceConfigurationWithTwoChannelSpecs(TimeDuration myInterval) {
        RegisterType registerType1 = createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("LoadProfileType", loadProfileObisCode, myInterval, Arrays.asList(registerType1, registerType2));
        loadProfileType.save();
        ChannelType channelTypeForRegisterType1 = loadProfileType.findChannelType(registerType1).get();
        ChannelType channelTypeForRegisterType2 = loadProfileType.findChannelType(registerType2).get();
        deviceType.addLoadProfileType(loadProfileType);
        DeviceType.DeviceConfigurationBuilder configurationWithLoadProfileAndChannel = deviceType.newConfiguration("ConfigurationWithLoadProfileAndChannel");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationWithLoadProfileAndChannel.newLoadProfileSpec(loadProfileType);
        configurationWithLoadProfileAndChannel.newChannelSpec(channelTypeForRegisterType1, loadProfileSpecBuilder).overflow(overflowValue).nbrOfFractionDigits(numberOfFractionDigits);
        configurationWithLoadProfileAndChannel.newChannelSpec(channelTypeForRegisterType2, loadProfileSpecBuilder).overflow(overflowValue).nbrOfFractionDigits(numberOfFractionDigits);
        DeviceConfiguration deviceConfiguration = configurationWithLoadProfileAndChannel.add();
        deviceConfiguration.activate();
        return deviceConfiguration;
    }

    private RegisterType createRegisterTypeIfMissing(ObisCode obisCode, ReadingType readingType) {
        Optional<RegisterType> xRegisterType = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType);
        RegisterType measurementType;
        if (xRegisterType.isPresent()) {
            measurementType = xRegisterType.get();
        } else {
            measurementType = inMemoryPersistence.getMasterDataService().newRegisterType(readingType, obisCode);
            measurementType.save();
        }
        return measurementType;
    }

    private ReadingTypeCodeBuilder getForwardBulkSecondaryEnergyReadingTypeCodeBuilder() {
        return ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR);
    }

    private ReadingTypeCodeBuilder getForwardBulkPrimaryEnergyReadingType() {
        return getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().commodity(Commodity.ELECTRICITY_PRIMARY_METERED);
    }

    private ReadingTypeCodeBuilder getForwardDeltaPrimaryMonthlyEnergyReadingType() {
        return getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().commodity(Commodity.ELECTRICITY_PRIMARY_METERED).accumulate(Accumulation.DELTADELTA);
    }

    private ReadingTypeCodeBuilder getReverseSecondaryBulkReadingTypeCodeBuilder() {
        return ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.REVERSE)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR);
    }

    @Test
    @Transactional
    @Expected(NoStatusInformationTaskException.class)
    public void testNoStatusInformationTaskAvailable() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        device.runStatusInformationTask(ComTaskExecution::runNow);
    }

    @Test
    @Transactional
    public void runStatusInformationComTaskTest() {
        createComTaskWithStatusInformation();
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        assertThat(device.getComTaskExecutions()).hasSize(0);
        assertThat(device.getDeviceConfiguration().getComTaskEnablements()).hasSize(2);
        device.runStatusInformationTask(ComTaskExecution::runNow);
        assertThat(device.getComTaskExecutions()).hasSize(1);
        assertThat(device.getComTaskExecutions().get(0).getProtocolTasks()).hasSize(2);
        boolean containsClockTask = false;
        for(ProtocolTask protocolTask: device.getComTaskExecutions().get(0).getProtocolTasks()) {
            if(protocolTask instanceof ClockTask) {
                containsClockTask = true;
            }
        }
        assertThat(containsClockTask).isTrue();
    }

    @Test
    @Transactional
    public void runStatusInformationComTaskTwiceTest() {
        createComTaskWithStatusInformation();
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICENAME, MRID);
        device.save();
        device.runStatusInformationTask(ComTaskExecution::runNow);
        assertThat(device.getComTaskExecutions()).hasSize(1);
        assertThat(device.getComTaskExecutions().get(0).getProtocolTasks()).hasSize(2);
        Instant plannedTime = device.getComTaskExecutions().get(0).getPlannedNextExecutionTimestamp();
        device.runStatusInformationTask(ComTaskExecution::runNow);
        assertThat(device.getComTaskExecutions()).hasSize(1);
        assertThat(device.getComTaskExecutions().get(0).getProtocolTasks()).hasSize(2);
        assertThat(plannedTime.isBefore(device.getComTaskExecutions().get(0).getPlannedNextExecutionTimestamp()));
    }

    protected void createComTaskWithStatusInformation() {
        ComTask comTask1 = inMemoryPersistence.getTaskService().newComTask("StatusInformationComTaskAndTopology");
        comTask1.setStoreData(true);
        comTask1.setMaxNrOfTries(3);
        comTask1.createStatusInformationTask();
        comTask1.createTopologyTask(TopologyAction.UPDATE);
        comTask1.save();
        ComTask comTask2 = inMemoryPersistence.getTaskService().newComTask("StatusInformationAndClock");
        comTask2.setStoreData(true);
        comTask2.setMaxNrOfTries(3);
        comTask2.createStatusInformationTask();
        comTask2.createClockTask(ClockTaskType.SETCLOCK)
                .maximumClockDifference(TimeDuration.days(1))
                .minimumClockDifference(TimeDuration.NONE)
                .add();
        comTask2.save();
        ProtocolDialectConfigurationProperties configDialect = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new ComTaskExecutionDialect());
        deviceConfiguration.save();
        enableComTask(comTask1, configDialect);
        enableComTask(comTask2, configDialect);
    }

    private void enableComTask(ComTask comTask1, ProtocolDialectConfigurationProperties configDialect) {
        deviceConfiguration.enableComTask(comTask1, this.securityPropertySet, configDialect)
                .useDefaultConnectionTask(true)
                .setPriority(213)
                .add();
    }

    protected class ComTaskExecutionDialect implements DeviceProtocolDialect {

        @Override
        public String getDeviceProtocolDialectName() {
            return "ProtocolDialectName";
        }

        @Override
        public String getDisplayName() {
            return "It's a Dell Display";
        }

        @Override
        public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
            return Optional.empty();
        }

    }
}