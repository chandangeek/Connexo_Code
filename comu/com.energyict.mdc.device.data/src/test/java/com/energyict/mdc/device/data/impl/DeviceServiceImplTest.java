/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceServiceImplTest extends PersistenceIntegrationTest {

    private static final String DEVICENAME = "deviceName";
    private static final ObisCode loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");
    private final TimeZone testDefaultTimeZone = TimeZone.getTimeZone("Canada/East-Saskatchewan");
    private final BigDecimal overflowValue = BigDecimal.valueOf(1234567);

    private ReadingType forwardBulkSecondaryEnergyReadingType;
    private ReadingType forwardDeltaPrimaryMonthlyEnergyReadingType;
    private ReadingType reverseBulkSecondaryEnergyReadingType;
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
        NlsMessageFormat format = mock(NlsMessageFormat.class);
        when(inMemoryPersistence.getMockedThesaurus().getFormat(any(MessageSeed.class))).thenAnswer(invocationOnMock -> {
            when(format.format(anyVararg())).thenReturn(((MessageSeed) invocationOnMock.getArguments()[0]).getDefaultFormat());
            return format;
        });
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
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, name, mRID, inMemoryPersistence.getClock().instant());
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
        inMemoryPersistence.getMeteringService()
                .getReadingType(getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().accumulate(Accumulation.DELTADELTA).code())
                .get();
        inMemoryPersistence.getMeteringService().getReadingType(getForwardBulkPrimaryEnergyReadingType().code()).get();
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
        inMemoryPersistence.getMeteringService().getReadingType(reverseBulkPrimaryCode).get();
        this.reverseEnergyObisCode = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeInformationFor(reverseBulkSecondaryEnergyReadingType).getObisCode();
        String averageForwardEnergyReadingTypeMRID = ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .aggregate(Aggregate.AVERAGE).period(MacroPeriod.DAILY)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
        inMemoryPersistence.getReadingTypeUtilService().getReadingTypeInformationFor(averageForwardEnergyReadingTypeMRID).getObisCode();
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
    @Expected(value = VetoUpdateObisCodeOnConfiguration.class, message = "You can not change the OBIS code, you already have devices with an overridden value for this OBIS code: [findDeviceWithOverruledObisCodeForOtherThanRegisterSpecTest]")
    public void findDeviceWithOverruledObisCodeForOtherThanRegisterSpecTest() {
        final int nbrOfFractionDigits = 3;
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        deviceType.addRegisterType(registerType1);
        deviceType.addRegisterType(registerType2);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("findDeviceWithOverruledObisCodeForOtherThanRegisterSpecTest");
        NumericalRegisterSpec.Builder registerSpecBuilder1 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.numberOfFractionDigits(nbrOfFractionDigits);
        registerSpecBuilder1.overflowValue(overflowValue);
        NumericalRegisterSpec.Builder registerSpecBuilder2 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType2);
        registerSpecBuilder2.numberOfFractionDigits(nbrOfFractionDigits);
        registerSpecBuilder2.overflowValue(overflowValue);
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "findDeviceWithOverruledObisCodeForOtherThanRegisterSpecTest", "findDeviceWithOverruledObisCodeForOtherThanRegisterSpecTest", inMemoryPersistence
                        .getClock()
                        .instant());
        device.save();

        ((EventServiceImpl) inMemoryPersistence.getEventService()).addTopicHandler(inMemoryPersistence.getRegisterSpecUpdateHandler());

        ObisCode overruledObisCode = ObisCode.fromString("1.2.3.4.5.6");
        String registerReadingType = forwardBulkSecondaryEnergyReadingType.getMRID();
        Register<?, ?> register = device.getRegisters().stream().filter(sRegister -> sRegister.getReadingType().getMRID().equals(registerReadingType)).findFirst().get();
        device.getRegisterUpdaterFor(register).setObisCode(overruledObisCode).update();

        NumericalRegisterSpec.Updater registerSpecUpdater = deviceConfiguration.getRegisterSpecUpdaterFor(
                (NumericalRegisterSpec) deviceConfiguration.getRegisterSpecs()
                        .stream()
                        .filter(registerSpec -> registerSpec.getReadingType().getMRID().equals(reverseBulkSecondaryEnergyReadingType.getMRID()))
                        .findFirst()
                        .get());
        registerSpecUpdater.overruledObisCode(overruledObisCode);
        registerSpecUpdater.update();
    }

    @Test
    @Transactional
    public void overruleConfigObisCodeWhileAlreadyOverruledOnDeviceIsOkTest() {
        final int nbrOfFractionDigits = 3;
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        deviceType.addRegisterType(registerType1);
        deviceType.addRegisterType(registerType2);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("findDeviceWithOverruledObisCodeForOtherThanRegisterSpecTest");
        NumericalRegisterSpec.Builder registerSpecBuilder1 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.numberOfFractionDigits(nbrOfFractionDigits);
        registerSpecBuilder1.overflowValue(overflowValue);
        NumericalRegisterSpec.Builder registerSpecBuilder2 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType2);
        registerSpecBuilder2.numberOfFractionDigits(nbrOfFractionDigits);
        registerSpecBuilder2.overflowValue(overflowValue);
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "findDeviceWithOverruledObisCodeForOtherThanRegisterSpecTest", "findDeviceWithOverruledObisCodeForOtherThanRegisterSpecTest", inMemoryPersistence
                        .getClock()
                        .instant());
        device.save();

        ((EventServiceImpl) inMemoryPersistence.getEventService()).addTopicHandler(inMemoryPersistence.getRegisterSpecUpdateHandler());

        ObisCode overruledObisCode = ObisCode.fromString("1.2.3.4.5.6");
        String registerReadingType = forwardBulkSecondaryEnergyReadingType.getMRID();
        Register<?, ?> register = device.getRegisters().stream().filter(sRegister -> sRegister.getReadingType().getMRID().equals(registerReadingType)).findFirst().get();
        device.getRegisterUpdaterFor(register).setObisCode(overruledObisCode).update();
        Register reloadedRegister = getReloadedDevice(device).getRegisters().stream().filter(sRegister -> sRegister.getReadingType().getMRID().equals(registerReadingType)).findFirst().get();
        assertThat(reloadedRegister.getDeviceObisCode()).isEqualTo(overruledObisCode);

        NumericalRegisterSpec.Updater registerSpecUpdater = deviceConfiguration.getRegisterSpecUpdaterFor(
                (NumericalRegisterSpec) deviceConfiguration.getRegisterSpecs()
                        .stream()
                        .filter(registerSpec -> registerSpec.getReadingType().getMRID().equals(registerReadingType))
                        .findFirst()
                        .get());
        registerSpecUpdater.overruledObisCode(overruledObisCode);
        registerSpecUpdater.update();

        reloadedRegister = getReloadedDevice(device).getRegisters().stream().filter(sRegister -> sRegister.getReadingType().getMRID().equals(registerReadingType)).findFirst().get();
        assertThat(reloadedRegister.getDeviceObisCode()).isEqualTo(overruledObisCode);
        assertThat(reloadedRegister.getRegisterSpec().getDeviceObisCode()).isEqualTo(overruledObisCode);
    }

    @Test
    @Transactional
    public void overruleRegisterSpecObisCodeWhileOverruledOnAChannel() {
        final int nbrOfFractionDigits = 3;
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("LoadProfileType", loadProfileObisCode, TimeDuration.months(1), Arrays.asList(registerType1, registerType2));
        loadProfileType.save();
        deviceType.addRegisterType(registerType1);
        deviceType.addRegisterType(registerType2);
        ChannelType channelTypeForRegisterType1 = loadProfileType.findChannelType(registerType1).get();
        ChannelType channelTypeForRegisterType2 = loadProfileType.findChannelType(registerType2).get();
        deviceType.addLoadProfileType(loadProfileType);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("overruleChannelObisCodeTest");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfigurationBuilder.newLoadProfileSpec(loadProfileType);
        BigDecimal overflow = BigDecimal.valueOf(9999);
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType1, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow)
                .useMultiplierWithCalculatedReadingType(forwardDeltaPrimaryMonthlyEnergyReadingType);
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType2, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow);
        NumericalRegisterSpec.Builder registerSpecBuilder1 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.numberOfFractionDigits(nbrOfFractionDigits);
        registerSpecBuilder1.overflowValue(overflowValue);
        NumericalRegisterSpec.Builder registerSpecBuilder2 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType2);
        registerSpecBuilder2.numberOfFractionDigits(nbrOfFractionDigits);
        registerSpecBuilder2.overflowValue(overflowValue);
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "overruleConfigObisCodeWhileOverruledOnAChannel", "overruleConfigObisCodeWhileOverruledOnAChannel", inMemoryPersistence
                        .getClock()
                        .instant());
        device.save();

        ((EventServiceImpl) inMemoryPersistence.getEventService()).addTopicHandler(inMemoryPersistence.getRegisterSpecUpdateHandler());

        ObisCode overruledObisCode = ObisCode.fromString("1.2.3.4.5.6");
        String registerReadingType = forwardBulkSecondaryEnergyReadingType.getMRID();
        String channelReadingType = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().period(MacroPeriod.MONTHLY).code();
        Channel channel = device.getLoadProfiles().get(0).getChannels().stream()
                .filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType)).findFirst().get();

        device.getChannelUpdaterFor(channel).setObisCode(overruledObisCode).update(); // overruled on channel

        NumericalRegisterSpec.Updater registerSpecUpdater = deviceConfiguration.getRegisterSpecUpdaterFor(
                (NumericalRegisterSpec) deviceConfiguration.getRegisterSpecs()
                        .stream()
                        .filter(registerSpec -> registerSpec.getReadingType().getMRID().equals(registerReadingType))
                        .findFirst()
                        .get());
        registerSpecUpdater.overruledObisCode(overruledObisCode);
        registerSpecUpdater.update();

        Device reloadedDevice = getReloadedDevice(device);
        Register reloadedRegister = reloadedDevice.getRegisters().stream().filter(sRegister -> sRegister.getReadingType().getMRID().equals(registerReadingType)).findFirst().get();
        assertThat(reloadedRegister.getDeviceObisCode()).isEqualTo(overruledObisCode); // the overruled value from the device config level
        assertThat(reloadedDevice.getLoadProfiles().get(0).getChannels().stream()
                .filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType)).findFirst().get().getObisCode()).isEqualTo(overruledObisCode);
    }

    @Test
    @Transactional
    @Expected(value = VetoUpdateObisCodeOnConfiguration.class, message = "You can not change the OBIS code, you already have devices with an overridden value for this OBIS code: [findDeviceWithOverruledObisCodeForOtherThanChannelSpecTest]")
    public void findDeviceWithOverruledObisCodeForOtherThanChannelSpecTest() {
        final int nbrOfFractionDigits = 3;
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("LoadProfileType", loadProfileObisCode, TimeDuration.months(1), Arrays.asList(registerType1, registerType2));
        loadProfileType.save();
        deviceType.addRegisterType(registerType1);
        deviceType.addRegisterType(registerType2);
        ChannelType channelTypeForRegisterType1 = loadProfileType.findChannelType(registerType1).get();
        ChannelType channelTypeForRegisterType2 = loadProfileType.findChannelType(registerType2).get();
        deviceType.addLoadProfileType(loadProfileType);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("overruleChannelObisCodeTest");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfigurationBuilder.newLoadProfileSpec(loadProfileType);
        BigDecimal overflow = BigDecimal.valueOf(9999);
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType1, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow)
                .useMultiplierWithCalculatedReadingType(forwardDeltaPrimaryMonthlyEnergyReadingType);
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType2, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow);
        NumericalRegisterSpec.Builder registerSpecBuilder1 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.numberOfFractionDigits(nbrOfFractionDigits);
        registerSpecBuilder1.overflowValue(overflowValue);
        NumericalRegisterSpec.Builder registerSpecBuilder2 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType2);
        registerSpecBuilder2.numberOfFractionDigits(nbrOfFractionDigits);
        registerSpecBuilder2.overflowValue(overflowValue);
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "findDeviceWithOverruledObisCodeForOtherThanChannelSpecTest", "findDeviceWithOverruledObisCodeForOtherThanChannelSpecTest", inMemoryPersistence
                        .getClock()
                        .instant());
        device.save();

        ((EventServiceImpl) inMemoryPersistence.getEventService()).addTopicHandler(inMemoryPersistence.getChannelSpecUpdateHandler());

        ObisCode overruledObisCode = ObisCode.fromString("1.2.3.4.5.6");
        String channelReadingType = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().period(MacroPeriod.MONTHLY).code();
        Channel channel = device.getLoadProfiles().get(0).getChannels().stream()
                .filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType)).findFirst().get();

        device.getChannelUpdaterFor(channel).setObisCode(overruledObisCode).update();

        String otherChannelReadingType = getReverseSecondaryBulkReadingTypeCodeBuilder().period(MacroPeriod.MONTHLY).code();
        ChannelSpec.ChannelSpecUpdater channelSpecUpdater = deviceConfiguration.getChannelSpecUpdaterFor(
                deviceConfiguration.getChannelSpecs()
                        .stream()
                        .filter(channelSpec -> channelSpec.getReadingType().getMRID().equals(otherChannelReadingType))
                        .findFirst()
                        .get());
        channelSpecUpdater.overruledObisCode(overruledObisCode);
        channelSpecUpdater.update();
    }

    @Test
    @Transactional
    public void overruleChannelSpecObisCodeWhileAlreadyOverruledOnDeviceTest() {
        final int nbrOfFractionDigits = 3;
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("LoadProfileType", loadProfileObisCode, TimeDuration.months(1), Arrays.asList(registerType1, registerType2));
        loadProfileType.save();
        deviceType.addRegisterType(registerType1);
        deviceType.addRegisterType(registerType2);
        ChannelType channelTypeForRegisterType1 = loadProfileType.findChannelType(registerType1).get();
        ChannelType channelTypeForRegisterType2 = loadProfileType.findChannelType(registerType2).get();
        deviceType.addLoadProfileType(loadProfileType);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("overruleChannelObisCodeTest");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfigurationBuilder.newLoadProfileSpec(loadProfileType);
        BigDecimal overflow = BigDecimal.valueOf(9999);
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType1, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow)
                .useMultiplierWithCalculatedReadingType(forwardDeltaPrimaryMonthlyEnergyReadingType);
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType2, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow);
        NumericalRegisterSpec.Builder registerSpecBuilder1 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.numberOfFractionDigits(nbrOfFractionDigits);
        registerSpecBuilder1.overflowValue(overflowValue);
        NumericalRegisterSpec.Builder registerSpecBuilder2 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType2);
        registerSpecBuilder2.numberOfFractionDigits(nbrOfFractionDigits);
        registerSpecBuilder2.overflowValue(overflowValue);
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "overruleChannelSpecObisCodeWhileOverruledOnDeviceLevelTest", "overruleChannelSpecObisCodeWhileOverruledOnDeviceLevelTest", inMemoryPersistence
                        .getClock()
                        .instant());
        device.save();

        ((EventServiceImpl) inMemoryPersistence.getEventService()).addTopicHandler(inMemoryPersistence.getChannelSpecUpdateHandler());

        ObisCode overruledObisCode = ObisCode.fromString("1.2.3.4.5.6");
        String channelReadingType = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().period(MacroPeriod.MONTHLY).code();
        Channel channel = device.getLoadProfiles().get(0).getChannels().stream()
                .filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType)).findFirst().get();

        device.getChannelUpdaterFor(channel).setObisCode(overruledObisCode).update();

        ChannelSpec.ChannelSpecUpdater channelSpecUpdater = deviceConfiguration.getChannelSpecUpdaterFor(
                deviceConfiguration.getChannelSpecs()
                        .stream()
                        .filter(channelSpec -> channelSpec.getReadingType().getMRID().equals(channelReadingType))
                        .findFirst()
                        .get());
        channelSpecUpdater.overruledObisCode(overruledObisCode);
        channelSpecUpdater.update();

        Device reloadedDevice = getReloadedDevice(device);
        Channel reloadedChannel = reloadedDevice.getChannels().stream().filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType)).findFirst().get();
        assertThat(reloadedChannel.getObisCode()).isEqualTo(overruledObisCode);
        assertThat(reloadedChannel.getChannelSpec().getDeviceObisCode()).isEqualTo(overruledObisCode);
    }

    @Test
    @Transactional
    public void overruleChannelSpecObisCodeWhileSameObisCodeOnARegisterOnDeviceTest() {
        final int nbrOfFractionDigits = 3;
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("LoadProfileType", loadProfileObisCode, TimeDuration.months(1), Arrays.asList(registerType1, registerType2));
        loadProfileType.save();
        deviceType.addRegisterType(registerType1);
        deviceType.addRegisterType(registerType2);
        ChannelType channelTypeForRegisterType1 = loadProfileType.findChannelType(registerType1).get();
        ChannelType channelTypeForRegisterType2 = loadProfileType.findChannelType(registerType2).get();
        deviceType.addLoadProfileType(loadProfileType);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("overruleChannelObisCodeTest");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfigurationBuilder.newLoadProfileSpec(loadProfileType);
        BigDecimal overflow = BigDecimal.valueOf(9999);
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType1, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow)
                .useMultiplierWithCalculatedReadingType(forwardDeltaPrimaryMonthlyEnergyReadingType);
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType2, loadProfileSpecBuilder)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow);
        NumericalRegisterSpec.Builder registerSpecBuilder1 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.numberOfFractionDigits(nbrOfFractionDigits);
        registerSpecBuilder1.overflowValue(overflowValue);
        NumericalRegisterSpec.Builder registerSpecBuilder2 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType2);
        registerSpecBuilder2.numberOfFractionDigits(nbrOfFractionDigits);
        registerSpecBuilder2.overflowValue(overflowValue);
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "overruleChannelSpecObisCodeWhileOverruledOnDeviceLevelTest", "overruleChannelSpecObisCodeWhileOverruledOnDeviceLevelTest", inMemoryPersistence
                        .getClock()
                        .instant());
        device.save();

        ((EventServiceImpl) inMemoryPersistence.getEventService()).addTopicHandler(inMemoryPersistence.getChannelSpecUpdateHandler());

        ObisCode overruledObisCode = ObisCode.fromString("1.2.3.4.5.6");
        String channelReadingType = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().period(MacroPeriod.MONTHLY).code();
        String registerReadingType = forwardBulkSecondaryEnergyReadingType.getMRID();
        Register<?, ?> register = device.getRegisters().stream().filter(sRegister -> sRegister.getReadingType().getMRID().equals(registerReadingType)).findFirst().get();
        device.getRegisterUpdaterFor(register).setObisCode(overruledObisCode).update();

        ChannelSpec.ChannelSpecUpdater channelSpecUpdater = deviceConfiguration.getChannelSpecUpdaterFor(
                deviceConfiguration.getChannelSpecs()
                        .stream()
                        .filter(channelSpec -> channelSpec.getReadingType().getMRID().equals(channelReadingType))
                        .findFirst()
                        .get());
        channelSpecUpdater.overruledObisCode(overruledObisCode);
        channelSpecUpdater.update();

        Device reloadedDevice = getReloadedDevice(device);
        Channel reloadedChannel = reloadedDevice.getChannels().stream().filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType)).findFirst().get();
        assertThat(reloadedChannel.getObisCode()).isEqualTo(overruledObisCode);
        assertThat(reloadedChannel.getChannelSpec().getDeviceObisCode()).isEqualTo(overruledObisCode);
        Register reloadedRegister = reloadedDevice.getRegisters().stream().filter(register1 -> register1.getReadingType().getMRID().equals(registerReadingType)).findAny().get();
        assertThat(reloadedRegister.getDeviceObisCode()).isEqualTo(overruledObisCode);
    }

    @Test
    @Transactional
    public void overruleChannelSpecWhileOverruledOnDeviceInOtherLoadProfileIsOkTest() {
        final int nbrOfFractionDigits = 3;
        RegisterType registerType1 = this.createRegisterTypeIfMissing(forwardEnergyObisCode, forwardBulkSecondaryEnergyReadingType);
        RegisterType registerType2 = createRegisterTypeIfMissing(reverseEnergyObisCode, reverseBulkSecondaryEnergyReadingType);
        LoadProfileType loadProfileType1 = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("LoadProfileType", loadProfileObisCode, TimeDuration.months(1), Collections.singletonList(registerType1));
        loadProfileType1.save();
        ObisCode otherLoadProfileObisCode = ObisCode.fromString("9.9.9.9.9.9");
        LoadProfileType loadProfileType2 = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("OtherLoadProfileType", otherLoadProfileObisCode, TimeDuration.months(1), Collections.singletonList(registerType2));
        loadProfileType2.save();
        deviceType.addRegisterType(registerType1);
        deviceType.addRegisterType(registerType2);

        ChannelType channelTypeForRegisterType1 = loadProfileType1.findChannelType(registerType1).get();

        ChannelType channelType2ForRegisterType2 = loadProfileType2.findChannelType(registerType2).get();

        deviceType.addLoadProfileType(loadProfileType1);
        deviceType.addLoadProfileType(loadProfileType2);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("overruleChannelObisCodeTest");
        BigDecimal overflow = BigDecimal.valueOf(9999);
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder1 = deviceConfigurationBuilder.newLoadProfileSpec(loadProfileType1);
        deviceConfigurationBuilder.newChannelSpec(channelTypeForRegisterType1, loadProfileSpecBuilder1)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow);

        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder2 = deviceConfigurationBuilder.newLoadProfileSpec(loadProfileType2);
        deviceConfigurationBuilder.newChannelSpec(channelType2ForRegisterType2, loadProfileSpecBuilder2)
                .nbrOfFractionDigits(nbrOfFractionDigits)
                .overflow(overflow);
        NumericalRegisterSpec.Builder registerSpecBuilder1 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType1);
        registerSpecBuilder1.numberOfFractionDigits(nbrOfFractionDigits);
        registerSpecBuilder1.overflowValue(overflowValue);
        NumericalRegisterSpec.Builder registerSpecBuilder2 = deviceConfigurationBuilder.newNumericalRegisterSpec(registerType2);
        registerSpecBuilder2.numberOfFractionDigits(nbrOfFractionDigits);
        registerSpecBuilder2.overflowValue(overflowValue);
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "overruleChannelSpecWhileOverruledOnDeviceInOtherLoadProfileIsOkTest", "overruleChannelSpecWhileOverruledOnDeviceInOtherLoadProfileIsOkTest", inMemoryPersistence
                        .getClock()
                        .instant());
        device.save();

        ((EventServiceImpl) inMemoryPersistence.getEventService()).addTopicHandler(inMemoryPersistence.getChannelSpecUpdateHandler());

        ObisCode overruledObisCode = ObisCode.fromString("1.2.3.4.5.6");
        String channelReadingType = getForwardBulkSecondaryEnergyReadingTypeCodeBuilder().period(MacroPeriod.MONTHLY).code();
        Channel channel = device.getLoadProfiles().stream().filter(loadProfile -> loadProfile.getDeviceObisCode().equals(loadProfileObisCode)).findAny().get().getChannels().stream()
                .filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType)).findFirst().get();

        device.getChannelUpdaterFor(channel).setObisCode(overruledObisCode).update();

        String channelReadingType2 = getReverseSecondaryBulkReadingTypeCodeBuilder().period(MacroPeriod.MONTHLY).code();
        ChannelSpec.ChannelSpecUpdater channelSpecUpdater = deviceConfiguration.getChannelSpecUpdaterFor(
                deviceConfiguration.getChannelSpecs()
                        .stream()
                        .filter(channelSpec -> channelSpec.getReadingType().getMRID().equals(channelReadingType2))
                        .findFirst()
                        .get());
        channelSpecUpdater.overruledObisCode(overruledObisCode);
        channelSpecUpdater.update();

        Device reloadedDevice = getReloadedDevice(device);
        Channel reloadedChannelOverruledOnDevice = reloadedDevice.getChannels().stream().filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType)).findFirst().get();
        assertThat(reloadedChannelOverruledOnDevice.getObisCode()).isEqualTo(overruledObisCode);
        assertThat(reloadedChannelOverruledOnDevice.getChannelSpec().getDeviceObisCode()).isNotEqualTo(overruledObisCode);

        Channel reloadedChannelOverruledOnConfig = reloadedDevice.getChannels().stream().filter(channel1 -> channel1.getReadingType().getMRID().equals(channelReadingType2)).findFirst().get();
        assertThat(reloadedChannelOverruledOnConfig.getObisCode()).isEqualTo(overruledObisCode);
        assertThat(reloadedChannelOverruledOnConfig.getChannelSpec().getDeviceObisCode()).isEqualTo(overruledObisCode);
    }

    @Test
    @Transactional
    public void findMultiplierTypeForFirstTime() {
        MeteringService meteringService = mock(MeteringService.class);
        MultiplierType multiplierType = mock(MultiplierType.class);
        when(meteringService.getMultiplierType(SyncDeviceWithKoreMeter.MULTIPLIER_TYPE)).thenReturn(Optional.of(multiplierType));
        DeviceServiceImpl service = new DeviceServiceImpl(inMemoryPersistence.getDeviceDataModelService(), meteringService, mock(QueryService.class), mock(Thesaurus.class), inMemoryPersistence.getClock());

        // Business method
        MultiplierType defaultMultiplierType = service.findDefaultMultiplierType();

        // Asserts
        verify(meteringService).getMultiplierType(SyncDeviceWithKoreMeter.MULTIPLIER_TYPE);
        verify(meteringService, never()).createMultiplierType(SyncDeviceWithKoreMeter.MULTIPLIER_TYPE);
        assertThat(defaultMultiplierType).isNotNull();
    }

    @Test
    @Transactional
    public void findMultiplierTypeForSecondTime() {
        MeteringService meteringService = mock(MeteringService.class);
        MultiplierType multiplierType = mock(MultiplierType.class);
        when(meteringService.getMultiplierType(SyncDeviceWithKoreMeter.MULTIPLIER_TYPE)).thenReturn(Optional.of(multiplierType));
        DeviceServiceImpl service = new DeviceServiceImpl(inMemoryPersistence.getDeviceDataModelService(), meteringService, mock(QueryService.class), mock(Thesaurus.class), inMemoryPersistence.getClock());
        // First call
        service.findDefaultMultiplierType();
        reset(meteringService);

        // Business method: second call
        MultiplierType defaultMultiplierType = service.findDefaultMultiplierType();

        // Asserts
        verify(meteringService, never()).getMultiplierType(SyncDeviceWithKoreMeter.MULTIPLIER_TYPE);
        verify(meteringService, never()).createMultiplierType(SyncDeviceWithKoreMeter.MULTIPLIER_TYPE);
        assertThat(defaultMultiplierType).isNotNull();
    }

}