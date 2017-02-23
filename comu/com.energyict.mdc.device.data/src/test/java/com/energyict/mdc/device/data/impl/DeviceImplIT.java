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
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
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
import com.energyict.mdc.device.data.exceptions.UnsatisfiedReadingTypeRequirementsOfUsagePointException;
import com.energyict.mdc.device.data.exceptions.UsagePointAlreadyLinkedToAnotherDeviceException;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.ComScheduleBuilder;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.tasks.TopologyAction;
import com.energyict.obis.ObisCode;
import com.google.common.collect.Range;
import org.assertj.core.api.Condition;
import org.joda.time.DateTimeConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
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

    private static final String DEVICE_NAME = "MyUniqueName";
    private static final ObisCode loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");
    private static final TimeZone testDefaultTimeZone = TimeZone.getTimeZone("Canada/East-Saskatchewan");
    private static final TimeDuration interval = TimeDuration.minutes(15);
    private static final BigDecimal overflowValue = BigDecimal.valueOf(1234567);
    private static final int numberOfFractionDigits = 2;
    private static MeterRole defaultMeterRole;
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    private ReadingType forwardBulkSecondaryEnergyReadingType;
    private ReadingType forwardDeltaSecondaryEnergyReadingType;
    private ReadingType reverseDeltaSecondaryMonthlyEnergyReadingType;
    private ReadingType forwardBulkPrimaryEnergyReadingType;
    private ReadingType forwardDeltaPrimaryMonthlyEnergyReadingType;
    private ReadingType reverseBulkSecondaryEnergyReadingType;
    private ReadingType reverseBulkPrimaryEnergyReadingType;
    private String averageForwardEnergyReadingTypeMRID;
    private ObisCode averageForwardEnergyObisCode;
    private ObisCode forwardEnergyObisCode;
    private ObisCode reverseEnergyObisCode;

    @BeforeClass
    public static void setup() {
        try (TransactionContext context = getTransactionService().getContext()) {
            deviceProtocolPluggableClass = inMemoryPersistence.getProtocolPluggableService().newDeviceProtocolPluggableClass("MyTestProtocol", TestProtocol.class.getName());
            deviceProtocolPluggableClass.save();
            defaultMeterRole = inMemoryPersistence.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.DEFAULT);
            context.commit();
        }
    }

    @Before
    public void setupMasterData() {
        this.setupReadingTypes();
        IssueStatus wontFix = mock(IssueStatus.class);
        when(inMemoryPersistence.getIssueService().findStatus(IssueStatus.WONT_FIX)).thenReturn(Optional.of(wontFix));
    }

    @After
    // MultiplierType is a cached object - make sure the cache is cleared after each test
    public void clearCache() {
        inMemoryPersistence.getDataModel().getInstance(OrmService.class).invalidateCache("MTR", "MTR_MULTIPLIERTYPE");
    }

    private Device createSimpleDeviceWithName(String name) {
        return createSimpleDeviceWithName(name, inMemoryPersistence.getClock().instant());
    }

    private Device createSimpleDeviceWithName(String name, Instant start) {
        return inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, name, start);
    }

    private void createTestDefaultTimeZone() {
        TimeZone.setDefault(testDefaultTimeZone);
        when(inMemoryPersistence.getClock().getZone()).thenReturn(testDefaultTimeZone.toZoneId());
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
        this.reverseDeltaSecondaryMonthlyEnergyReadingType = inMemoryPersistence.getMeteringService()
                .getReadingType(getReverseSecondaryBulkReadingTypeCodeBuilder().accumulate(Accumulation.DELTADELTA).period(MacroPeriod.MONTHLY).code())
                .get();
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
        Device device = createSimpleDeviceWithName(DEVICE_NAME);

        assertThat(device).isNotNull();
        assertThat(device.getId()).isGreaterThan(0L);
        assertThat(device.getName()).isEqualTo(DEVICE_NAME);
        assertThat(device.getSerialNumber()).isNullOrEmpty();
    }

    @Test
    @Transactional
    public void meterActivationAfterInitialCreation() {
        Device device = createSimpleDeviceWithName(DEVICE_NAME);
        assertThat(device.getCurrentMeterActivation()).isPresent();
    }

    @Test
    @Transactional
    public void successfulReloadTest() {
        Device device = createSimpleDeviceWithName(DEVICE_NAME);

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice).isNotNull();
        assertThat(reloadedDevice.getName()).isEqualTo(DEVICE_NAME);
        assertThat(reloadedDevice.getSerialNumber()).isNullOrEmpty();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    public void createWithoutNameTest() {
        inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, null, Instant.now());
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    public void createWithEmptyNameTest() {
        inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "", Instant.now());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}", property = "shipmentDate")
    public void createWithoutShipmentDateTest() {
        inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "MyNameWithoutShipment", null);
    }

    @Test
    @Transactional
    public void createWithSerialNumberTest() {
        String serialNumber = "MyTestSerialNumber";

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, Instant.now());
        device.setSerialNumber(serialNumber);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getSerialNumber()).isEqualTo(serialNumber);
    }

    @Test
    @Transactional
    public void testUpdateName() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, Instant.now());

        DeviceImpl reloadedDevice = (DeviceImpl) getReloadedDevice(device);
        reloadedDevice.setName("new name");
        reloadedDevice.save();

        reloadedDevice = (DeviceImpl) getReloadedDevice(device);

        assertThat(reloadedDevice.getName()).isEqualTo("new name");
    }

    @Test
    @Transactional
    public void updateWithSerialNumberTest() {
        String serialNumber = "MyUpdatedSerialNumber";
        Device simpleDevice = createSimpleDeviceWithName(DEVICE_NAME);

        simpleDevice.setSerialNumber(serialNumber);
        simpleDevice.save();

        Device reloadedDevice = getReloadedDevice(simpleDevice);

        assertThat(reloadedDevice.getSerialNumber()).isEqualTo(serialNumber);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DUPLICATE_DEVICE_NAME + "}")
    public void duplicateDeviceName() {
        createSimpleDeviceWithName(DEVICE_NAME);
        createSimpleDeviceWithName(DEVICE_NAME);
    }

    @Test
    @Transactional
    public void successfulCreationOfTwoDevicesWithSameSerialNumberTest() {
        String serialNumber = "SerialNumber";
        Device device1 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME + "First", Instant.now());
        device1.setSerialNumber(serialNumber);
        device1.save();

        Device device2 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME + "Second", Instant.now());
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
        Device device1 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME + "First", Instant.now());
        device1.save();
        Device device2 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME + "Second", Instant.now());
        field("mRID").ofType(String.class).in(device2).set(device1.getmRID());
        device2.save();
    }

    @Test
    @Transactional
    public void getMridTest() {
        Device device = createSimpleDeviceWithName("MyName");

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(UUID.fromString(reloadedDevice.getmRID())).isNotNull();
    }

    @Test
    @Transactional
    public void getHistoryTest() {
        Device simpleDevice = createSimpleDeviceWithName("getHistoryTest");
        assertThat(simpleDevice.getHistory(simpleDevice.getCreateTime()).get().getId()).isEqualTo(simpleDevice.getId());
    }

    /**
     * This test will get the default TimeZone of the system.
     */
    @Test
    @Transactional
    public void defaultTimeZoneTest() {
        createTestDefaultTimeZone();
        Device simpleDevice = createSimpleDeviceWithName(DEVICE_NAME);

        assertThat(simpleDevice.getTimeZone()).isEqualTo(testDefaultTimeZone);
    }

    @Test
    @Transactional
    public void getWithIncorrectTimeZoneIdAndFallBackToSystemTimeZoneTest() {
        createTestDefaultTimeZone();
        Device simpleDevice = createSimpleDeviceWithName(DEVICE_NAME);

        inMemoryPersistence.update("update ddc_device set TIMEZONE = 'InCorrectTimeZoneId' where id = " + simpleDevice.getId());

        Device reloadedDevice = getReloadedDevice(simpleDevice);

        assertThat(testDefaultTimeZone).isEqualTo(reloadedDevice.getTimeZone());
    }

    @Test
    @Transactional
    public void userDefinedTimeZoneTest() {
        createTestDefaultTimeZone();
        TimeZone userDefinedTimeZone = TimeZone.getTimeZone("Asia/Novokuznetsk");
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, Instant.now());
        device.setTimeZone(userDefinedTimeZone);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(userDefinedTimeZone).isEqualTo(reloadedDevice.getTimeZone());
    }

    @Test
    @Transactional
    public void userDefinedNullTimeZoneResultsInDefaultTimeZoneTest() {
        createTestDefaultTimeZone();
        Device device = createSimpleDeviceWithName(DEVICE_NAME);

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(testDefaultTimeZone).isEqualTo(reloadedDevice.getTimeZone());
    }

    @Test
    @Transactional
    public void updateUserDefinedTimeZoneWithNullTimeZoneResultsInDefaultTest() {
        createTestDefaultTimeZone();
        TimeZone userDefinedTimeZone = TimeZone.getTimeZone("Asia/Novokuznetsk");
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, Instant.now());
        device.setTimeZone(userDefinedTimeZone);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        reloadedDevice.setZone(null);
        reloadedDevice.save();

        Device updatedDevice = getReloadedDevice(reloadedDevice);

        assertThat(testDefaultTimeZone).isEqualTo(updatedDevice.getTimeZone());
    }

    @Test
    @Transactional
    public void getRegistersForConfigWithoutRegistersTest() {
        Device simpleDevice = createSimpleDeviceWithName(DEVICE_NAME);

        assertThat(simpleDevice.getRegisters()).isEmpty();
    }

    @Test
    @Transactional
    public void getRegisterWithDeviceObisCodeForConfigWithoutRegistersTest() {
        Device simpleDevice = createSimpleDeviceWithName(DEVICE_NAME);

        assertThat(simpleDevice.getRegisterWithDeviceObisCode(ObisCode.fromString("1.0.1.8.0.255"))).isEmpty();
    }

    @Test
    @Transactional
    public void getRegistersForConfigWithRegistersTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoRegisterSpecs();
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, Instant.now());

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getRegisters()).hasSize(2);
    }

    @Test
    @Transactional
    public void getRegisterReadingsShouldReturnEmptyListTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoRegisterSpecs();
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, Instant.now());

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getRegisterWithDeviceObisCode(forwardEnergyObisCode).get().getReadings(Interval.sinceEpoch())).isEmpty();
    }

    @Test
    @Transactional
    public void getRegisterReadingsShouldReturnResultsTest() {
        BigDecimal readingValue = new BigDecimal(5432.32);
        Instant readingTimeStamp = inMemoryPersistence.getClock().instant();
        com.elster.jupiter.metering.readings.Reading reading = com.elster.jupiter.metering.readings.beans.ReadingImpl.of(forwardBulkSecondaryEnergyReadingType.getMRID(), readingValue, readingTimeStamp);
        MeterReadingImpl meterReading = MeterReadingImpl.of(reading);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoRegisterSpecs();
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, readingTimeStamp);

        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        List<Reading> readings = reloadedDevice.getRegisterWithDeviceObisCode(forwardEnergyObisCode).get().getReadings(Interval.sinceEpoch());
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
        com.elster.jupiter.metering.readings.beans.ReadingImpl reading =
                com.elster.jupiter.metering.readings.beans.ReadingImpl.of(this.averageForwardEnergyReadingTypeMRID, readingValue, eventEnd);
        reading.setTimePeriod(eventStart, eventEnd);
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addReading(reading);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoRegisterSpecs();
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, eventStart);

        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        List<Reading> readings = reloadedDevice.getRegisterWithDeviceObisCode(this.averageForwardEnergyObisCode).get().getReadings(Interval.sinceEpoch());
        assertThat(readings).isNotEmpty();
        assertThat(readings).hasSize(1);
        assertThat(readings.get(0)).isInstanceOf(BillingReading.class);
        BillingReading billingReading = (BillingReading) readings.get(0);
        assertThat(billingReading.getType().getMRID()).isEqualTo(forwardBulkSecondaryEnergyReadingType.getMRID());
        assertThat(billingReading.getTimeStamp()).isEqualTo(eventEnd);
        assertThat(billingReading.getRange().isPresent()).isTrue();
        assertThat(billingReading.getRange().get()).isEqualTo(Ranges.openClosed(eventStart, eventEnd));
        assertThat(billingReading.getValue()).isEqualTo(readingValue);
    }

    @Test
    @Transactional
    public void getRegisterWithDeviceObisCodeForConfigWithRegistersTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoRegisterSpecs();
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, Instant.now());

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getRegisterWithDeviceObisCode(forwardEnergyObisCode)).isPresent();
        assertThat(reloadedDevice.getRegisterWithDeviceObisCode(reverseEnergyObisCode)).isPresent();
    }

    @Test
    @Transactional
    public void getChannelsForConfigWithNoChannelSpecsTest() {
        Device simpleDevice = createSimpleDeviceWithName(DEVICE_NAME);

        assertThat(simpleDevice.getChannels()).isEmpty();
    }

    @Test
    @Transactional
    public void createDeviceWithTwoChannelsTest() {
        DeviceConfiguration deviceConfigurationWithTwoChannelSpecs = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithTwoChannelSpecs, "DeviceWithChannels", Instant.now());

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getChannels()).isNotEmpty();
        assertThat(reloadedDevice.getChannels()).hasSize(2);
    }

    @Test(expected = CannotDeleteComScheduleFromDevice.class)
    @Transactional
    public void removeComScheduleThatWasNotAddedToDevice() {
        ComSchedule comSchedule = this.createComSchedule("removeComScheduleThatWasNotAddedToDevice");
        Device simpleDevice = this.createSimpleDeviceWithName(DEVICE_NAME);

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

        inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "MySimpleName", Instant.now());
    }

    @Test
    @Transactional
    public void testGatewayTypeMethodsForHAN() {
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Collections.singletonList(DeviceProtocolCapabilities.PROTOCOL_MASTER));
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("GatewayTypeMethodsForHAN", deviceProtocolPluggableClass);
        DeviceType.DeviceConfigurationBuilder config = deviceType.newConfiguration("some config").gatewayType(GatewayType.HOME_AREA_NETWORK);
        DeviceConfiguration deviceConfiguration = config.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "name", Instant.now());

        assertThat(device.getConfigurationGatewayType()).isEqualTo(GatewayType.HOME_AREA_NETWORK);
    }

    @Test
    @Transactional
    public void testGatewayTypeMethodsForLAN() {
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Collections.singletonList(DeviceProtocolCapabilities.PROTOCOL_MASTER));
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("GatewayTypeMethodsForLAN", deviceProtocolPluggableClass);
        DeviceType.DeviceConfigurationBuilder config = deviceType.newConfiguration("some config").gatewayType(GatewayType.LOCAL_AREA_NETWORK);
        DeviceConfiguration deviceConfiguration = config.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "name", Instant.now());

        assertThat(device.getConfigurationGatewayType()).isEqualTo(GatewayType.LOCAL_AREA_NETWORK);
    }

    @Test
    @Transactional
    public void testGatewayTypeMethodsForNonConcentrator() {
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Collections.singletonList(DeviceProtocolCapabilities.PROTOCOL_MASTER));
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("GatewayTypeMethodsForNonConcentrator", deviceProtocolPluggableClass);
        DeviceType.DeviceConfigurationBuilder config = deviceType.newConfiguration("some config");
        DeviceConfiguration deviceConfiguration = config.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "name", Instant.now());

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
        LocalDateTime localDateTime = LocalDateTime.ofInstant(inMemoryPersistence.getClock().instant(), ZoneOffset.UTC);
        localDateTime = localDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
        Instant dayStart = localDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant(ZoneOffset.UTC); // Fri, 01 Aug 2014 00:00:00 GMT
        Instant dayEnd = localDateTime.plus(1, ChronoUnit.DAYS).toInstant(ZoneOffset.UTC); // Sat, 02 Aug 2014 00:00:00 GMT
        Instant nineOClock = localDateTime.withHour(9).withMinute(0).withSecond(0).withNano(0).toInstant(ZoneOffset.UTC);  // 1/8/2014 9:00 <==  meterActivation starts at nine
        Instant quarterPastNine = localDateTime.withHour(9).withMinute(15).withSecond(0).withNano(0).toInstant(ZoneOffset.UTC); // 1/8/2014 9:00 <== end first interval
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, nineOClock);
        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder()
                .period(TimeAttribute.MINUTE15)
                .code();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(code);
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(dayEnd, readingValue));
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
        intervalBlock2.addIntervalReading(IntervalReadingImpl.of(nineOClock, BigDecimal.ZERO));
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(intervalBlock);
        meterReading.addIntervalBlock(intervalBlock2);
        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        List<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(Ranges.openClosed(dayStart, dayEnd));
        assertThat(readings).describedAs("There should be no data(holders) for the interval 00:00-> 09:00").hasSize(24 * 4 - 4 * 9);
        assertThat(readings.get(0).getRange().upperEndpoint()).isEqualTo(dayEnd);
        assertThat(readings.get(readings.size() - 1).getRange().lowerEndpoint()).isEqualTo(nineOClock);
        assertThat(readings.get(readings.size() - 1).getRange().upperEndpoint()).isEqualTo(quarterPastNine);
        for (LoadProfileReading reading : readings) { // Only 1 channel will contain a value for a single interval
            if (reading.getRange().upperEndpoint().equals(dayEnd)) {
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
        LocalDateTime localDateTime = LocalDateTime.ofInstant(inMemoryPersistence.getClock().instant(), ZoneOffset.UTC);
        localDateTime = localDateTime.withHour(0)
                .withMinute(0)
                .withSecond(0);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, localDateTime.toInstant(ZoneOffset.UTC));

        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder()
                .period(TimeAttribute.MINUTE15)
                .code();
        IntervalBlockImpl intervalBlock1 = IntervalBlockImpl.of(code);
        intervalBlock1.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), 0, 15, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
        intervalBlock2.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), 0, 30, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock3 = IntervalBlockImpl.of(code);
        intervalBlock3.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), 0, 45, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock4 = IntervalBlockImpl.of(code);
        intervalBlock4.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), 1, 0, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlockX = IntervalBlockImpl.of(code);
        intervalBlockX.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), 23, 45, 0).toInstant(ZoneOffset.UTC), readingValue));
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(intervalBlock1);
        meterReading.addIntervalBlock(intervalBlock2);
        meterReading.addIntervalBlock(intervalBlock3);
        meterReading.addIntervalBlock(intervalBlock4);
        meterReading.addIntervalBlock(intervalBlockX);
        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        Instant start = localDateTime.withHour(12).withMinute(0).withSecond(0).toInstant(ZoneOffset.UTC);
        Instant end = localDateTime.withHour(16).withMinute(0).withSecond(0).toInstant(ZoneOffset.UTC);
        List<LoadProfileReading> readings = reloadedDevice.getChannels().get(0).getChannelData(Ranges.openClosed(start, end));
        assertThat(readings).describedAs("There should be data(holders) for the interval 12:00->16:00 even though there are no meter readings").hasSize(4 * 4);
    }

    // JP-5583
    @Test
    @Transactional
    public void testGetLoadProfileDataIfRequestedIntervalHasNoReadingsButDataWasExpected() {
        BigDecimal readingValue = BigDecimal.valueOf(543232, 2);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(inMemoryPersistence.getClock().instant(), ZoneOffset.UTC).withHour(0).withHour(0).withSecond(0).withNano(0);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, localDateTime.toInstant(ZoneOffset.UTC));

        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder()
                .period(TimeAttribute.MINUTE15)
                .code();
        IntervalBlockImpl intervalBlock1 = IntervalBlockImpl.of(code);
        intervalBlock1.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), 0, 15, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
        intervalBlock2.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), 0, 30, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock3 = IntervalBlockImpl.of(code);
        intervalBlock3.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), 0, 45, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock4 = IntervalBlockImpl.of(code);
        intervalBlock4.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), 1, 0, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlockX = IntervalBlockImpl.of(code);
        intervalBlockX.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), 23, 45, 0).toInstant(ZoneOffset.UTC), readingValue));
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(intervalBlock1);
        meterReading.addIntervalBlock(intervalBlock2);
        meterReading.addIntervalBlock(intervalBlock3);
        meterReading.addIntervalBlock(intervalBlock4);
        meterReading.addIntervalBlock(intervalBlockX);
        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        Instant start = LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), 12, 0, 0).toInstant(ZoneOffset.UTC);
        Instant end = LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), 16, 0, 0).toInstant(ZoneOffset.UTC);
        List<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(Ranges.openClosed(start, end));
        assertThat(readings).describedAs("There should be data(holders) for the interval 12:00->16:00 even though there are no meter readings").hasSize(4 * 4);
    }

    // JP-5583
    @Test
    @Transactional
    public void testGetLoadProfileDataIfRequestedIntervalIsEmptyButDataWasExpected() {
        BigDecimal readingValue = BigDecimal.valueOf(543232, 2);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(inMemoryPersistence.getClock().instant(), ZoneOffset.UTC).withHour(0).withHour(0).withSecond(0).withNano(0);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, localDateTime.toInstant(ZoneOffset.UTC));

        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder()
                .period(TimeAttribute.MINUTE15)
                .code();
        IntervalBlockImpl intervalBlock1 = IntervalBlockImpl.of(code);
        intervalBlock1.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), 0, 15, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
        intervalBlock2.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), 0, 30, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock3 = IntervalBlockImpl.of(code);
        intervalBlock3.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), 0, 45, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlock4 = IntervalBlockImpl.of(code);
        intervalBlock4.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), 1, 0, 0).toInstant(ZoneOffset.UTC), readingValue));
        IntervalBlockImpl intervalBlockX = IntervalBlockImpl.of(code);
        intervalBlockX.addIntervalReading(IntervalReadingImpl.of(LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), 23, 45, 0).toInstant(ZoneOffset.UTC), readingValue));
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(intervalBlock1);
        meterReading.addIntervalBlock(intervalBlock2);
        meterReading.addIntervalBlock(intervalBlock3);
        meterReading.addIntervalBlock(intervalBlock4);
        meterReading.addIntervalBlock(intervalBlockX);
        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        Instant start = LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), 12, 5, 0).toInstant(ZoneOffset.UTC);
        Instant end = LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), 12, 10, 0).toInstant(ZoneOffset.UTC);
        List<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(Ranges.openClosed(start, end));
//        assertThat(readings).describedAs("There should be 1 data(holders) for the interval 12:05->12:10: 1x15 minute reading overlaps with the interval").hasSize(1);
        assertThat(readings).describedAs("Changed this behavior so we don't create duplicate entries when MeterActivations don't start/end at the interval boundary").hasSize(0);
    }

    @Test
    @Transactional
    // @see  JP-8514
    public void testGetLoadProfileDataAfterLastReading() {
        BigDecimal readingValue = BigDecimal.valueOf(543232, 2);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, Instant.now());

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
        LocalDateTime localDateTime = LocalDateTime.ofInstant(inMemoryPersistence.getClock().instant(), ZoneOffset.UTC).withHour(0).withMinute(0).withSecond(0).withNano(0);
        Instant requestIntervalStart = localDateTime.toInstant(ZoneOffset.UTC);
        Instant requestIntervalEnd = localDateTime.plus(1, ChronoUnit.DAYS).toInstant(ZoneOffset.UTC);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, requestIntervalStart);
        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder()
                .period(TimeAttribute.MINUTE15)
                .code();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(code);
        Instant readingTimeStamp = localDateTime.withHour(21).withMinute(0).withSecond(0).toInstant(ZoneOffset.UTC);
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(readingTimeStamp, readingValue));
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
        Instant previousReadingTimeStamp = localDateTime.withHour(0).withMinute(0).withSecond(0).toInstant(ZoneOffset.UTC);
        intervalBlock2.addIntervalReading(IntervalReadingImpl.of(previousReadingTimeStamp, BigDecimal.ZERO));
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(intervalBlock);
        meterReading.addIntervalBlock(intervalBlock2);
        device.store(meterReading);
        Instant lastReading = localDateTime.withHour(21).withMinute(0).withSecond(0).toInstant(ZoneOffset.UTC);
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
        when(inMemoryPersistence.getClock().instant()).thenAnswer(invocationOnMock -> Instant.ofEpochMilli(1385841600000L));

        BigDecimal readingValue = BigDecimal.valueOf(543232, 2);
        Instant meterActivation = Instant.ofEpochMilli(1385841600000L);
        Instant requestIntervalStart = Instant.ofEpochMilli(1104523200000L); // Fri, 31 Dec 2004 20:00:00 GMT
        Instant requestIntervalEnd = Instant.ofEpochMilli(1420056000000L); // Wed, 31 Dec 2014 20:00:00 GMT
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(TimeDuration.months(1));
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, meterActivation);

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
        when(inMemoryPersistence.getClock().instant()).thenAnswer(invocationOnMock -> Instant.ofEpochMilli(1385851500000L));

        Instant requestIntervalStart = Instant.ofEpochMilli(1385851500000L); //   11/30/2013, 11:45:00 PM (UTC)
        Instant requestIntervalEnd = Instant.ofEpochMilli(1420066800000L); // 1/1/2015, 12:00:00 AM (UTC)
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(TimeDuration.months(1));

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, requestIntervalStart);

        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder()
                .period(MacroPeriod.MONTHLY)
                .code();
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        Instant readingTimeStamp = Instant.ofEpochMilli(1385852400000L);//  Sat, 30 Nov 2013 23:00:00 GMT
        for (int i = 0; i < 13; i++) {
            IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
            intervalBlock2.addIntervalReading(IntervalReadingImpl.of(readingTimeStamp, BigDecimal.ZERO));
            meterReading.addIntervalBlock(intervalBlock2);
            readingTimeStamp = readingTimeStamp.atZone(ZoneId.of(UTC.getID())).plus(1, ChronoUnit.MONTHS).toInstant();
        }

        device.store(meterReading);

        Instant lastReading = Instant.ofEpochMilli(1420802100000L); // Fri, 09 Jan 2015 11:15:00 GMT
        device.getLoadProfileUpdaterFor(device.getLoadProfiles().get(0)).setLastReading(lastReading).update();

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(device.getLoadProfiles().get(0).getLastReading().toString()).isEqualTo(lastReading);
        List<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(Ranges.openClosed(requestIntervalStart, requestIntervalEnd));
        assertThat(readings.size()).isEqualTo(13);
        assertThat(readings.get(12).getRange().upperEndpoint()).isEqualTo(Instant.ofEpochMilli(1385852400000L)); // Sat, 31 Dec 2014 23:00:00 GMT
    }

    @Test
    @Transactional
    public void testGetLoadProfileDataIfExternalMeterActivationDoesNotAlignWithChannelIntervalBoundary() {
        BigDecimal readingValue = BigDecimal.valueOf(543232, 2);
        when(inMemoryPersistence.getClock().instant()).thenReturn(Instant.ofEpochMilli(1420761600000L));
        Instant requestIntervalStart = Instant.ofEpochMilli(1420761600000L); // Fri, 09 Jan 2015 00:00:00 GMT
        Instant requestIntervalEnd = Instant.ofEpochMilli(1420848000000L); //  Sat, 10 Jan 2015 00:00:00 GMT
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, Instant.ofEpochMilli(1420801085000L));

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
        when(inMemoryPersistence.getClock().instant()).thenReturn(Instant.ofEpochMilli(1406851200000L));
        Instant dayStart = Instant.ofEpochMilli(1406851200000L); // Fri, 01 Aug 2014 00:00:00 GMT
        Instant dayEnd = Instant.ofEpochMilli(1406937600000L); // Sat, 02 Aug 2014 00:00:00 GMT
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, dayStart);

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
        LocalDateTime localDateTime = LocalDateTime.ofInstant(inMemoryPersistence.getClock().instant(), ZoneOffset.UTC).withHour(0).withMinute(0).withSecond(0).withNano(0);
        Instant readingTimeStamp_A = localDateTime.withHour(0).withMinute(15).withSecond(0).withNano(0).toInstant(ZoneOffset.UTC); // 3/30/2014, 01:15:00 AM
        Instant readingTimeStamp_B = localDateTime.withHour(5).withMinute(0).withSecond(0).withNano(0).toInstant(ZoneOffset.UTC); // 3/30/2014, 05:15:00 AM
        Instant dayStart = localDateTime.toInstant(ZoneOffset.UTC); // 3/30/2014, 1:00:00 AM
        Instant dayEnd = localDateTime.withHour(6).withMinute(0).withSecond(0).withNano(0).toInstant(ZoneOffset.UTC); // 3/30/2014, 8:00:00 AM !! 6 hours later!!
        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().period(TimeAttribute.MINUTE15).code();
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock1 = IntervalBlockImpl.of(code);
        intervalBlock1.addIntervalReading(IntervalReadingImpl.of(readingTimeStamp_A, readingValue));
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
        intervalBlock2.addIntervalReading(IntervalReadingImpl.of(readingTimeStamp_B, readingValue));
        meterReading.addIntervalBlock(intervalBlock1);
        meterReading.addIntervalBlock(intervalBlock2);

        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, dayStart);

        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        List<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(Ranges.openClosed(dayStart, dayEnd));
        assertThat(readings).hasSize(20);  // only readings from 01:15:00 to 05:00:00, shouldn't have readings for period 05:00:00 - 08:00:00
    }

    @Test
    @Transactional
    public void testGetLoadProfileDataHasValidationState() {
        BigDecimal readingValue = BigDecimal.valueOf(543232, 2);
        when(inMemoryPersistence.getClock().instant()).thenReturn(Instant.ofEpochMilli(1406851200000L));
        Instant dayStart = Instant.ofEpochMilli(1406851200000L); // Fri, 01 Aug 2014 00:00:00 GMT
        Instant dayEnd = Instant.ofEpochMilli(1406937600000L); // Sat, 02 Aug 2014 00:00:00 GMT
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, dayStart);

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
        LocalDateTime localDateTime = LocalDateTime.ofInstant(inMemoryPersistence.getClock().instant(), ZoneOffset.UTC);
        Instant dayStart = localDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant(ZoneOffset.UTC);
        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, dayStart);

        device.activate(dayStart);
        device.deactivate(dayStart.plus(10, ChronoUnit.MINUTES));
        device.activate(dayStart.plus(10, ChronoUnit.MINUTES));

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

        IntervalReadingRecord updatedReading;
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, Instant.now());

        Device reloadedDevice = getReloadedDevice(device);
        Collection<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(Ranges.openClosed(dayStart, dayEnd));
        assertThat(readings).isEmpty();
    }

    @Test
    @Transactional
    public void testGetLoadProfileDataDST() {
        BigDecimal readingValue = new BigDecimal(5432.32);
        when(inMemoryPersistence.getClock().instant()).thenReturn(Instant.ofEpochMilli(1396137600000L));
        Instant dayStart = Instant.ofEpochMilli(1396137600000L); // 3/30/2014, 1:00:00 AM
        Instant dayEnd = Instant.ofEpochMilli(1396159200000L); // 3/30/2014, 8:00:00 AM !! 6 hours later!!
        String code = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().period(TimeAttribute.MINUTE15).code();
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(code);

        intervalBlock.addIntervalReading(IntervalReadingImpl.of(dayEnd, readingValue));
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(code);
        Instant previousReadingTimeStamp = Instant.ofEpochMilli(1396136700000L); // 3/30/2014, 0:45:00 AM
        intervalBlock2.addIntervalReading(IntervalReadingImpl.of(previousReadingTimeStamp, BigDecimal.ZERO));
        meterReading.addIntervalBlock(intervalBlock);
        meterReading.addIntervalBlock(intervalBlock2);

        DeviceConfiguration deviceConfiguration = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, dayStart);

        device.store(meterReading);

        Device reloadedDevice = getReloadedDevice(device);
        List<LoadProfileReading> readings = reloadedDevice.getLoadProfiles().get(0).getChannelData(Ranges.openClosed(dayStart, dayEnd));
        assertThat(readings).hasSize(4 * 6);  // 4 per hour, during 6 hours
    }

    @Test
    @Transactional
    public void aNewDeviceHasMeterActivation() {
        Instant initialStart = inMemoryPersistence.getClock().instant();

        // Business method
        Device device = this.createSimpleDeviceWithName(DEVICE_NAME, initialStart);

        // Asserts
        assertThat(device.getCurrentMeterActivation()).isPresent();
        assertThat(device.getCurrentMeterActivation().get().getStart()).isEqualTo(initialStart.truncatedTo(ChronoUnit.MINUTES));
    }

    @Test
    @Transactional
    public void activateMeterWhenStillActive() {
        Instant initialStart = Instant.ofEpochMilli(1000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(initialStart);
        Device device = this.createSimpleDeviceWithName(DEVICE_NAME, initialStart);
        Instant end = Instant.ofEpochMilli(2000L);

        // Business method
        device.activate(end);

        // Asserts
        assertThat(device.getCurrentMeterActivation()).isPresent();
        assertThat(device.getMeterActivationsMostRecentFirst()).hasSize(2);
        assertThat(device.getCurrentMeterActivation().get().getStart()).isEqualTo(end.truncatedTo(ChronoUnit.MINUTES));
    }

    @Test
    @Transactional
    public void deactivateNowOnMeterThatWasNotActive() {
        Device device = this.createSimpleDeviceWithName(DEVICE_NAME);

        // Business method
        device.deactivateNow();

        // Asserts
        assertThat(device.getCurrentMeterActivation()).isEmpty();
    }

    @Test
    @Transactional
    public void deactivateMeter() {
        Instant initialStart = Instant.ofEpochMilli(100000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(initialStart);
        Device device = this.createSimpleDeviceWithName(DEVICE_NAME, initialStart);

        Instant end = Instant.ofEpochMilli(200000L);
        // Business method
        device.deactivate(end);
        when(inMemoryPersistence.getClock().instant()).thenReturn(Instant.ofEpochMilli(300000L));
        // Asserts
        assertThat(device.getCurrentMeterActivation()).isEmpty();
    }

    @Test
    @Transactional
    public void reactivateMeter() {
        when(inMemoryPersistence.getClock().instant()).thenReturn(Instant.ofEpochMilli(100000L));
        Instant initialStart = Instant.ofEpochMilli(100000L);
        Device device = this.createSimpleDeviceWithName(DEVICE_NAME, initialStart);
        Instant end = Instant.ofEpochMilli(200000L);
        device.deactivate(end);

        Instant expectedStart = Instant.ofEpochMilli(300000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(Instant.ofEpochMilli(300000L));
        device.activate(expectedStart);

        // Asserts
        assertThat(device.getCurrentMeterActivation()).isPresent();
        assertThat(device.getCurrentMeterActivation().get().getStart()).isEqualTo(expectedStart.truncatedTo(ChronoUnit.MINUTES));
    }

    @Test
    @Transactional
    public void updateCimLifecycleDates() {
        Instant expectedInstalledDate = inMemoryPersistence.getClock().instant();
        Instant expectedManufacturedDate = expectedInstalledDate.plusSeconds(1L);
        Instant expectedPurchasedDate = expectedInstalledDate.plusSeconds(2L);
        Instant expectedReceivedDate = expectedInstalledDate.plusSeconds(3L);
        Instant expectedRetiredDate = expectedInstalledDate.plusSeconds(4L);
        Instant expectedRemovedDate = expectedInstalledDate.plusSeconds(5L);
        Device device = this.createSimpleDeviceWithName(DEVICE_NAME, expectedReceivedDate);

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
        EndDevice endDevice = inMemoryPersistence.getMeteringService().findEndDeviceByMRID(device.getmRID()).get();
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
    public void newDeviceHasMultiplierOne() {
        DeviceConfiguration deviceConfiguration = createSetupWithMultiplierRegisterSpec();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "setMultiplierTest", Instant.now());

        device = getReloadedDevice(device);
        assertThat(device.getMultiplier()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    @Transactional
    public void setMultiplierTest() {
        DeviceConfiguration deviceConfiguration = createSetupWithMultiplierRegisterSpec();
        freezeClock(2016, 1, 2);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "setMultiplierTest2", inMemoryPersistence.getClock().instant());
        freezeClock(2016, 1, 3);
        device.setMultiplier(BigDecimal.TEN);
        device.save();
        assertThat(device.getMeterActivationsMostRecentFirst()).hasSize(2);

        device = getReloadedDevice(device);
        assertThat(device.getMultiplier()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    @Transactional
    public void setMultiplierToOneOnNewDevice() {
        DeviceConfiguration deviceConfiguration = createSetupWithMultiplierRegisterSpec();
        freezeClock(2016, 1, 2);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "setMultiplierTest3", inMemoryPersistence.getClock().instant());
        freezeClock(2016, 1, 3);
        device.setMultiplier(BigDecimal.ONE);
        device.save();
        assertThat(device.getMeterActivationsMostRecentFirst()).hasSize(1);    // no new Meter Activation

        device = getReloadedDevice(device);
        assertThat(device.getMultiplier()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    @Transactional
    public void setSameMultiplierDoesNotCreateNewMeterActivationTest() {
        DeviceConfiguration deviceConfiguration = createSetupWithMultiplierRegisterSpec();

        Instant initialStart = freezeClock(2015, 11, 25);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "setMultiplierTest4", initialStart);

        assertThat(device.getMeterActivationsMostRecentFirst()).hasSize(1);
        assertThat(device.getMultiplier()).isEqualTo(BigDecimal.ONE);

        BigDecimal multiplier = BigDecimal.TEN;
        initialStart = freezeClock(2015, 11, 26);
        device.setMultiplier(multiplier);
        device.save();
        assertThat(device.getMeterActivationsMostRecentFirst()).hasSize(2);

        Instant fiveDaysLater = freezeClock(2015, 11, 30);
        device.setMultiplier(BigDecimal.valueOf(10), fiveDaysLater);
        device.save();
        assertThat(device.getMeterActivationsMostRecentFirst()).hasSize(2);
        assertThat(device.getMultiplier()).isEqualTo(BigDecimal.valueOf(10));
        assertThat(device.getCurrentMeterActivation().get().getStart()).isEqualTo(initialStart);
    }

    @Test
    @Transactional
    public void setMultiplierCreatesNewMeterActivationTest() {
        DeviceConfiguration deviceConfiguration = createSetupWithMultiplierRegisterSpec();

        Instant initialStart = freezeClock(2015, 11, 25);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "setMultiplierTest4", initialStart);

        assertThat(device.getMeterActivationsMostRecentFirst()).hasSize(1);
        assertThat(device.getMultiplier()).isEqualTo(BigDecimal.ONE);

        BigDecimal multiplier = BigDecimal.TEN;
        initialStart = freezeClock(2015, 11, 26);
        device.setMultiplier(multiplier);
        device.save();
        assertThat(device.getMeterActivationsMostRecentFirst()).hasSize(2);

        Instant fiveDaysLater = freezeClock(2015, 11, 30);
        device.setMultiplier(BigDecimal.valueOf(100), fiveDaysLater);
        device.save();
        assertThat(device.getMeterActivationsMostRecentFirst()).hasSize(3);
        assertThat(device.getMultiplier()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(device.getCurrentMeterActivation().get().getStart()).isEqualTo(fiveDaysLater);
    }

    @Test
    @Transactional
    public void setMultiplierOfOneAlsoCreatesMeterActivationTest() {
        DeviceConfiguration deviceConfiguration = createSetupWithMultiplierRegisterSpec();

        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "setMultiplierOfOneAlsoCreatesMeterActivationTest", Instant.now());

        assertThat(device.getMeterActivationsMostRecentFirst()).hasSize(1);

        BigDecimal multiplier = BigDecimal.ONE;
        device.setMultiplier(multiplier);
        assertThat(device.getMeterActivationsMostRecentFirst()).hasSize(1);
        assertThat(device.getMultiplier()).isEqualTo(multiplier);
    }

    @Test
    @Transactional
    public void settingMultiplierBackToOneShouldRemovePreviouslyDefinedMultiplierTest() {
        // removing is not really the case anymore. If MultiSense indicates that a multiplier will be used, we still set the value of ONE on the meterActivation
        DeviceConfiguration deviceConfiguration = createSetupWithMultiplierRegisterSpec();

        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "settingMultiplierBackToOneShouldRemovePreviouslyDefinedMultiplierTest", Instant.now());

        assertThat(device.getMeterActivationsMostRecentFirst()).hasSize(1);
        device.setMultiplier(BigDecimal.TEN);
        device.setMultiplier(BigDecimal.ONE);
        device.setMultiplier(BigDecimal.ONE);
        device.setMultiplier(BigDecimal.ONE);
        device.setMultiplier(BigDecimal.ONE);
        assertThat(device.getMeterActivationsMostRecentFirst()).hasSize(1);
        assertThat(device.getMultiplier()).isEqualTo(BigDecimal.ONE);
        assertThat(device.getCurrentMeterActivation().get().getMultipliers()).hasSize(1);
        assertThat(new ArrayList<>(device.getCurrentMeterActivation().get().getMultipliers().values()).get(0)).isEqualTo(BigDecimal.ONE);
    }

    @Test
    @Transactional
    @Expected(value = MultiplierConfigurationException.class, message = "The multiplier should be larger than zero")
    public void settingMultiplierWithValueZeroShouldFailTest() {
        DeviceConfiguration deviceConfiguration = createSetupWithMultiplierRegisterSpec();

        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "settingMultiplierWithValueZeroShouldFailTest", Instant.now());

        device.setMultiplier(BigDecimal.ZERO);
        device.save();
    }

    @Test
    @Transactional
    @Expected(value = MultiplierConfigurationException.class, message = "The multiplier exceeds the max value " + Integer.MAX_VALUE)
    public void settingMultiplierLargerThanMaxIntShouldFailTest() {
        DeviceConfiguration deviceConfiguration = createSetupWithMultiplierRegisterSpec();

        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "settingMultiplierLargerThanMaxIntShouleFailTest", Instant.now());

        device.setMultiplier(BigDecimal.valueOf(Long.MAX_VALUE));
        device.save();
    }

    @Test
    @Transactional
    public void createMeterConfigurationForMultipliedRegisterSpecTest() {
        DeviceConfiguration deviceConfiguration = createSetupWithMultiplierRegisterSpec();
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithMultiplierOnRegister", Instant.now());
        device.setMultiplier(BigDecimal.TEN);
        device.save();

        Optional<MeterConfiguration> meterConfigurationOptional = inMemoryPersistence.getMeteringService().findMeterById(device.getId()).get().getConfiguration(inMemoryPersistence.getClock().instant());
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
    public void headEndInterfaceCreatesCorrectKoreChannels() {
        freezeClock(2016, 6, 1);
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("LoadProfileType", loadProfileObisCode, TimeDuration.months(1), Arrays.asList(registerType1, registerType2));
        loadProfileType.save();
        ChannelType channelTypeForRegisterType1 = loadProfileType.findChannelType(registerType1).get();
        ChannelType channelTypeForRegisterType2 = loadProfileType.findChannelType(registerType2).get();
        deviceType.addLoadProfileType(loadProfileType);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("ConfigWithMultipliedChannels");
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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithMultipliers", inMemoryPersistence.getClock().instant());
        freezeClock(2016, 6, 2);
        device.setMultiplier(BigDecimal.TEN);
        device.save();
        Meter meter = inMemoryPersistence.getMeteringService().findMeterById(device.getId()).get();

        int channelSize = meter.getCurrentMeterActivation().get().getChannelsContainer().getChannels().size();
        assertThat(channelSize)
                .withFailMessage("You should have 2 channels, but you got " + channelSize + ". This is probably because you created your MeterActivation first before you updated the MeterConfiguration")
                .isEqualTo(2);
        assertThat(meter.getCurrentMeterActivation().get().getChannelsContainer().getChannels()).haveExactly(1, new Condition<com.elster.jupiter.metering.Channel>() {
            @Override
            public boolean matches(com.elster.jupiter.metering.Channel channel) {
                return channel.getMainReadingType().getMRID().equals(forwardDeltaPrimaryMonthlyEnergyReadingType.getMRID())
                        && channel.getBulkQuantityReadingType().isPresent() && channel.getBulkQuantityReadingType().get().getMRID().equals(channelTypeForRegisterType1.getReadingType().getMRID());
            }
        });
        assertThat(meter.getCurrentMeterActivation().get().getChannelsContainer().getChannels()).haveExactly(1, new Condition<com.elster.jupiter.metering.Channel>() {
            @Override
            public boolean matches(com.elster.jupiter.metering.Channel channel) {
                return channel.getMainReadingType().getMRID().equals(reverseDeltaSecondaryMonthlyEnergyReadingType.getMRID())
                        && channel.getBulkQuantityReadingType().isPresent() && channel.getBulkQuantityReadingType().get().getMRID().equals(channelTypeForRegisterType2.getReadingType().getMRID());
            }
        });
    }

    @Test
    @Transactional
    public void createMeterConfigurationForMultipliedRegisterSpecWithoutMultiplierSetOnDeviceTest() {
        DeviceConfiguration deviceConfiguration = createSetupWithMultiplierRegisterSpec();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithMultiplierOnRegister2", Instant.now());

        Optional<MeterConfiguration> meterConfigurationOptional = inMemoryPersistence.getMeteringService().findMeterById(device.getId()).get().getConfiguration(inMemoryPersistence.getClock().instant());
        assertThat(meterConfigurationOptional).isPresent();
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).hasSize(2);
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).haveExactly(1, new Condition<MeterReadingTypeConfiguration>() {
            @Override
            public boolean matches(MeterReadingTypeConfiguration value) {
                return value.getMeasured().getMRID().equals(forwardBulkSecondaryEnergyReadingType.getMRID()) &&
                        value.getCalculated().isPresent() && value.getCalculated().get().getMRID().equals(forwardBulkPrimaryEnergyReadingType.getMRID());
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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithMultiplierOnRegister", Instant.now());
        device.setMultiplier(BigDecimal.TEN);
        device.save();

        Optional<MeterConfiguration> meterConfigurationOptional = inMemoryPersistence.getMeteringService().findMeterById(device.getId()).get().getConfiguration(inMemoryPersistence.getClock().instant());
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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithMultiplierOnRegister", Instant.now());

        Optional<MeterConfiguration> meterConfigurationOptional = inMemoryPersistence.getMeteringService().findMeterById(device.getId()).get().getConfiguration(inMemoryPersistence.getClock().instant());
        assertThat(meterConfigurationOptional).isPresent();
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).hasSize(2);
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).haveExactly(1, new Condition<MeterReadingTypeConfiguration>() {
            @Override
            public boolean matches(MeterReadingTypeConfiguration value) {
                return value.getMeasured().getMRID().equals(forwardBulkSecondaryEnergyReadingType.getMRID()) &&
                        value.getCalculated().isPresent() && value.getCalculated().get().getMRID().equals(forwardBulkPrimaryEnergyReadingType.getMRID()); // we always use the calculated readingtype when the user defined to use it on config level
            }
        });
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).haveExactly(1, new Condition<MeterReadingTypeConfiguration>() {
            @Override
            public boolean matches(MeterReadingTypeConfiguration value) {
                return value.getMeasured().getMRID().equals(reverseBulkSecondaryEnergyReadingType.getMRID()) &&
                        value.getCalculated().isPresent() && value.getCalculated()
                        .get()
                        .getMRID()
                        .equals(reverseBulkPrimaryEnergyReadingType.getMRID()); // we always use the calculated readingtype when the user defined to use it on config level
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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithMultipliers", inMemoryPersistence.getClock().instant());
        device.setMultiplier(BigDecimal.TEN);
        device.save();

        Optional<MeterConfiguration> meterConfigurationOptional = inMemoryPersistence.getMeteringService().findMeterById(device.getId()).get().getConfiguration(inMemoryPersistence.getClock().instant());
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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithMultipliers", Instant.now());

        Optional<MeterConfiguration> meterConfigurationOptional = inMemoryPersistence.getMeteringService().findMeterById(device.getId()).get().getConfiguration(inMemoryPersistence.getClock().instant());
        assertThat(meterConfigurationOptional).isPresent();
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).hasSize(3);
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).haveExactly(1, new Condition<MeterReadingTypeConfiguration>() {
            @Override
            public boolean matches(MeterReadingTypeConfiguration value) {
                return value.getMeasured().getMRID().equals(forwardBulkSecondaryEnergyReadingType.getMRID()) &&
                        value.getCalculated().isPresent() && value.getCalculated()
                        .get()
                        .getMRID()
                        .equals(forwardBulkPrimaryEnergyReadingType.getMRID()); // we always use the calculated readingtype when the user defined to use it on config level
            }
        });
        assertThat(meterConfigurationOptional.get().getReadingTypeConfigs()).haveExactly(1, new Condition<MeterReadingTypeConfiguration>() {
            @Override
            public boolean matches(MeterReadingTypeConfiguration value) {
                return value.getMeasured().getMRID().equals(getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().period(MacroPeriod.MONTHLY).code()) &&
                        value.getCalculated().isPresent() && value.getCalculated().get().getMRID().equals(getForwardBulkPrimaryEnergyReadingType().period(MacroPeriod.MONTHLY).code()) && // we always use the calculated readingtype when the user defined to use it on config level
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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", inMemoryPersistence.getClock().instant());

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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", inMemoryPersistence.getClock().instant());

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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", inMemoryPersistence.getClock().instant());

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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", inMemoryPersistence.getClock().instant());

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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", inMemoryPersistence.getClock().instant());

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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", inMemoryPersistence.getClock().instant());
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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", inMemoryPersistence.getClock().instant());

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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", inMemoryPersistence.getClock().instant());

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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", inMemoryPersistence.getClock().instant());

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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", inMemoryPersistence.getClock().instant());

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

        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "noOverflowRequiredOnDeltaUpdateTest", inMemoryPersistence.getClock().instant());

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

        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "overruleOverflowOnDeltaWhenNoOverflowOnConfigTest", inMemoryPersistence.getClock().instant());

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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", inMemoryPersistence.getClock().instant());

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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "OverruleTest", inMemoryPersistence.getClock().instant());

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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, Instant.now());

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

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, Instant.now());

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getComTaskExecutions()).isNotEmpty(); // I expect a ComTaskExecution was created for comTask_2 (which was marked as ignoreNextExecutionSpecsForInbound)
        assertThat(reloadedDevice.getComTaskExecutions()).hasSize(1);
        //TODO: check this test
        assertThat(reloadedDevice.getComTaskExecutions().get(0).getComTask().equals(comTask_2.getId()));
    }

    @Test
    @Transactional
    public void activateDeviceOnUsagePoint() {
        Instant now = Instant.ofEpochMilli(50L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(now);
        Device device = this.createSimpleDeviceWithName("activateDeviceOnUsagePoint");
        UsagePoint usagePoint = this.createSimpleUsagePoint("UP001");
        Instant expectedStart = Instant.ofEpochMilli(907L);

        // Business method
        device.activate(expectedStart, usagePoint, defaultMeterRole);

        // Asserts
        assertThat(device.getCurrentMeterActivation()).isPresent();
        assertThat(device.getCurrentMeterActivation().get().getStart()).isEqualTo(expectedStart.truncatedTo(ChronoUnit.MINUTES));
        assertThat(device.getCurrentMeterActivation().get().getUsagePoint().get()).isEqualTo(usagePoint);
        assertThat(device.getCurrentMeterActivation().get().getMeterRole().get().getKey()).isEqualTo(DefaultMeterRole.DEFAULT.getKey());
    }

    @Test
    @Transactional
    public void reactivateDeviceOnUsagePoint() {
        Instant now = Instant.ofEpochMilli(50000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(now);
        Device device = this.createSimpleDeviceWithName("reactivateDeviceOnUsagePoint");
        UsagePoint usagePoint = this.createSimpleUsagePoint("UP001");
        Instant expectedStart = Instant.ofEpochMilli(97000L);
        Instant expectedStartWithUsagePoint = Instant.ofEpochMilli(980000L);

        // Business method
        device.activate(expectedStart);
        device.activate(expectedStartWithUsagePoint, usagePoint, defaultMeterRole);
        when(inMemoryPersistence.getClock().instant()).thenReturn(expectedStartWithUsagePoint.plus(1, ChronoUnit.MINUTES));

        // Asserts
        device = getReloadedDevice(device);

        assertThat(device.getCurrentMeterActivation()).isPresent();
        assertThat(device.getCurrentMeterActivation().get().getStart()).isEqualTo(expectedStartWithUsagePoint.truncatedTo(ChronoUnit.MINUTES));
        assertThat(device.getCurrentMeterActivation().get().getUsagePoint().get()).isEqualTo(usagePoint);
    }

    @Test
    @Transactional
    @Expected(UsagePointAlreadyLinkedToAnotherDeviceException.class)
    public void activateDeviceOnUsagePointAlreadyLinkedToAnotherDevice() {
        Instant now = Instant.ofEpochMilli(50L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(now);
        Device device = this.createSimpleDeviceWithName("activateDeviceOnUsagePointAlreadyLinkedToAnotherDevice");
        Device anotherDevice = this.createSimpleDeviceWithName("another device");
        UsagePoint usagePoint = this.createSimpleUsagePoint("UP001");
        Instant expectedStart = Instant.ofEpochMilli(97L);
        anotherDevice.activate(Instant.ofEpochMilli(96L), usagePoint, defaultMeterRole);
        usagePoint = inMemoryPersistence.getMeteringService().findUsagePointById(usagePoint.getId()).get();

        // Business method
        device.activate(expectedStart, usagePoint, defaultMeterRole);

        // Asserts
        //exception is thrown
    }

    @Test
    @Transactional
    public void activateDeviceOnUsagePointMissingReadingTypeRequirements() {
        freezeClock(2016, 6, 1);
        DeviceConfiguration deviceConfigurationWithTwoChannelSpecs = createDeviceConfigurationWithTwoChannelSpecs(interval);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfigurationWithTwoChannelSpecs, "DeviceWithChannels", inMemoryPersistence.getClock().instant());

        UsagePoint usagePoint = this.createSimpleUsagePoint("UP001");

        ReadingType monthlyDeltaAMinus = inMemoryPersistence.getMeteringService().getReadingType("13.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        ReadingType monthlyDeltaAPlus = inMemoryPersistence.getMeteringService().getReadingType("13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        ReadingType minutes15DeltaAPlus = inMemoryPersistence.getMeteringService().getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();

        UsagePointMetrologyConfiguration mc0 = createMetrologyConfiguration("mc0", Collections.singletonList(monthlyDeltaAMinus));
        UsagePointMetrologyConfiguration mc1 = createMetrologyConfiguration("mc1", Arrays.asList(monthlyDeltaAPlus, minutes15DeltaAPlus));
        UsagePointMetrologyConfiguration mc2 = createMetrologyConfiguration("mc2", Collections.singletonList(monthlyDeltaAPlus));

        Instant startMC0 = freezeClock(2016, 6, 2);
        Instant startMC1 = freezeClock(2016, 6, 3);
        Instant startMC2 = freezeClock(2016, 6, 4);

        usagePoint.apply(mc0, startMC0);
        usagePoint.apply(mc1, startMC1);
        usagePoint.apply(mc2, startMC2);

        expectedEx.expect(UnsatisfiedReadingTypeRequirementsOfUsagePointException.class);
        expectedEx.expectMessage(
                "This device doesn't have the following reading types that are specified " +
                        "in the metrology configurations of the selected usage point: " +
                        "'" + mc1.getName() + "' (" + monthlyDeltaAPlus.getName() + "), " +
                        "'" + mc2.getName() + "' (" + monthlyDeltaAPlus.getName() + ")");

        // Business method
        getReloadedDevice(device).activate(startMC1, usagePoint, defaultMeterRole);

        // Asserts
        //exception is thrown
    }

    @Test
    @Transactional
    public void activateDeviceOnUsagePointCopyMultiplier() {
        Instant now = Instant.ofEpochMilli(50L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(now);
        Device device = this.createSimpleDeviceWithName("activateDeviceOnUsagePointCopyMultiplier");
        BigDecimal multiplier = BigDecimal.valueOf(100);
        device.setMultiplier(multiplier, Instant.ofEpochMilli(96L));
        device.save();
        device = getReloadedDevice(device);

        UsagePoint usagePoint = this.createSimpleUsagePoint("UP001");
        Instant expectedStart = Instant.ofEpochMilli(97L);

        // Business method
        device.activate(expectedStart, usagePoint, defaultMeterRole);
        when(inMemoryPersistence.getClock().instant()).thenReturn(expectedStart.plus(1, ChronoUnit.MINUTES));

        // Asserts
        device = getReloadedDevice(device);
        assertThat(device.getCurrentMeterActivation().get().getUsagePoint().get()).isEqualTo(usagePoint);
        assertThat(device.getMultiplier()).isEqualTo(multiplier);
    }

    private UsagePoint createSimpleUsagePoint(String name) {
        return inMemoryPersistence.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get()
                .newUsagePoint(name, inMemoryPersistence.getClock().instant())
                .create();
    }

    private UsagePointMetrologyConfiguration createMetrologyConfiguration(String name, List<ReadingType> readingTypes) {
        ServiceCategory serviceCategory = inMemoryPersistence.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        MetrologyConfigurationService metrologyConfigurationService = inMemoryPersistence.getMetrologyConfigurationService();
        MetrologyPurpose purpose = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION).get();
        MeterRole meterRoleDefault = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey()).get();

        UsagePointMetrologyConfiguration mc = metrologyConfigurationService.newUsagePointMetrologyConfiguration(name, serviceCategory).create();
        mc.addMeterRole(meterRoleDefault);
        MetrologyContract metrologyContract = mc.addMandatoryMetrologyContract(purpose);
        for (ReadingType readingType : readingTypes) {
            FullySpecifiedReadingTypeRequirement fullySpecifiedReadingTypeRequirement = mc.newReadingTypeRequirement(readingType.getFullAliasName(), meterRoleDefault)
                    .withReadingType(readingType);
            ReadingTypeDeliverableBuilder builder = mc.newReadingTypeDeliverable(readingType.getFullAliasName(), readingType, Formula.Mode.AUTO);
            ReadingTypeDeliverable deliverable = builder.build(builder.requirement(fullySpecifiedReadingTypeRequirement));
            metrologyContract.addDeliverable(deliverable);
        }
        mc.activate();
        return mc;
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, inMemoryPersistence.getClock().instant());
        device.save();
        device.runStatusInformationTask(ComTaskExecution::runNow);
    }

    @Test
    @Transactional
    public void runStatusInformationComTaskTest() {
        createComTaskWithStatusInformation();
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, inMemoryPersistence.getClock().instant());
        device.save();
        assertThat(device.getComTaskExecutions()).hasSize(0);
        assertThat(device.getDeviceConfiguration().getComTaskEnablements()).hasSize(2);
        device.runStatusInformationTask(ComTaskExecution::runNow);
        assertThat(device.getComTaskExecutions()).hasSize(1);
        assertThat(device.getComTaskExecutions().get(0).getProtocolTasks()).hasSize(2);
        boolean containsClockTask = false;
        for (ProtocolTask protocolTask : device.getComTaskExecutions().get(0).getProtocolTasks()) {
            if (protocolTask instanceof ClockTask) {
                containsClockTask = true;
            }
        }
        assertThat(containsClockTask).isTrue();
    }

    @Test
    @Transactional
    public void runStatusInformationComTaskTwiceTest() {
        createComTaskWithStatusInformation();
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME, inMemoryPersistence.getClock().instant());
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
        public List<PropertySpec> getUPLPropertySpecs() {
            return getPropertySpecs().stream().map(ConnexoToUPLPropertSpecAdapter::new).collect(Collectors.toList());
        }

        @Override
        public String getDeviceProtocolDialectDisplayName() {
            return "It's a Dell Display";
        }

        @Override
        public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
            return Optional.empty();
        }

    }
}
