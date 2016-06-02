package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * Test covering the data transfer between channels when slave is linked to data logger, or
 * when slave is unlinked from data logger.
 *
 * Copyrights EnergyICT
 * Date: 25/05/2016
 * Time: 9:04
 */
public class DataLoggerReferenceImplTest extends PersistenceIntegrationTest {

    private final static Unit kiloWattHours = Unit.get("kWh");

    private DeviceConfiguration dataLoggerConfiguration, configurationForSlaveWithLoadProfiles, configurationForSlaveWithRegisters;

    private ReadingType channelReadingType1, channelReadingType2, channelReadingType3, channelReadingType4, channelReadingType5, channelReadingType6;
    private ReadingType registerReadingType1, registerReadingType3, registerReadingType4;


    private void setUpForDataLoggerEnabledDevice() {
        // set up for first loadProfile
        registerReadingType1 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.0.255"), kiloWattHours);
        ReadingType registerReadingType2 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.2.8.0.255"), kiloWattHours);
        // set up for second loadProfile
        registerReadingType3 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.1.255"), kiloWattHours);
        registerReadingType4 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.2.255"), kiloWattHours);
        ReadingType registerReadingType5 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.2.8.1.255"), kiloWattHours);
        ReadingType registerReadingType6 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.2.8.2.255"), kiloWattHours);

        // set up for first loadProfile
        channelReadingType1 = inMemoryPersistence.getReadingTypeUtilService().getIntervalAppliedReadingType(registerReadingType1,Optional.of(TimeDuration.minutes(15)), ObisCode.fromString("1.0.1.8.0.255")).get();
        channelReadingType2 = inMemoryPersistence.getReadingTypeUtilService().getIntervalAppliedReadingType(registerReadingType2,Optional.of(TimeDuration.minutes(15)), ObisCode.fromString("1.0.2.8.0.255")).get();
        // set up for second loadProfile
        channelReadingType3 = inMemoryPersistence.getReadingTypeUtilService().getIntervalAppliedReadingType(registerReadingType3,Optional.of(TimeDuration.minutes(15)), ObisCode.fromString("1.0.1.8.1.255")).get();
        channelReadingType4 = inMemoryPersistence.getReadingTypeUtilService().getIntervalAppliedReadingType(registerReadingType4,Optional.of(TimeDuration.minutes(15)), ObisCode.fromString("1.0.1.8.2.255")).get();
        channelReadingType5 = inMemoryPersistence.getReadingTypeUtilService().getIntervalAppliedReadingType(registerReadingType5,Optional.of(TimeDuration.minutes(15)), ObisCode.fromString("1.0.2.8.1.255")).get();
        channelReadingType6 = inMemoryPersistence.getReadingTypeUtilService().getIntervalAppliedReadingType(registerReadingType6,Optional.of(TimeDuration.minutes(15)), ObisCode.fromString("1.0.2.8.2.255")).get();


        RegisterType registerTypeForChannel1 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(registerReadingType1)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(registerReadingType1, ObisCode.fromString("1.0.1.8.0.255")));
        RegisterType registerTypeForChannel2 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(registerReadingType2)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(registerReadingType2, ObisCode.fromString("1.0.2.8.0.255")));
        RegisterType registerTypeForChannel3 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(registerReadingType3)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(registerReadingType3, ObisCode.fromString("1.0.1.8.1.255")));
        registerTypeForChannel3.save();
        RegisterType registerTypeForChannel4 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(registerReadingType4)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(registerReadingType4, ObisCode.fromString("1.0.1.8.2.255")));
        registerTypeForChannel4.save();
        RegisterType registerTypeForChannel5 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(registerReadingType5)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(registerReadingType5, ObisCode.fromString("1.0.2.8.1.255")));
        registerTypeForChannel5.save();
        RegisterType registerTypeForChannel6 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(registerReadingType6)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(registerReadingType6, ObisCode.fromString("1.0.2.8.2.255")));
        registerTypeForChannel6.save();

        LoadProfileType lpt1 = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("15min Electricity Total", ObisCode.fromString("1.0.99.1.0.255"), TimeDuration.minutes(15), Arrays.asList(registerTypeForChannel1, registerTypeForChannel2));
        LoadProfileType lpt2 = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("15min Electricity tariff", ObisCode.fromString("1.0.99.2.0.255"), TimeDuration.minutes(15), Arrays.asList(registerTypeForChannel3, registerTypeForChannel4, registerTypeForChannel5, registerTypeForChannel6));

        lpt1.save();
        lpt2.save();

        DeviceType dataLoggerDeviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("dataLoggerDeviceType", deviceProtocolPluggableClass);
        dataLoggerDeviceType.addRegisterType(registerTypeForChannel1);
        dataLoggerDeviceType.addRegisterType(registerTypeForChannel2);
        dataLoggerDeviceType.addRegisterType(registerTypeForChannel3);
        dataLoggerDeviceType.addRegisterType(registerTypeForChannel4);
        dataLoggerDeviceType.addRegisterType(registerTypeForChannel5);
        dataLoggerDeviceType.addRegisterType(registerTypeForChannel6);
        dataLoggerDeviceType.addLoadProfileType(lpt1);
        dataLoggerDeviceType.addLoadProfileType(lpt2);
        dataLoggerDeviceType.update();

        DeviceType.DeviceConfigurationBuilder dataLoggerEnabledDeviceConfigurationBuilder = dataLoggerDeviceType.newConfiguration("Default");
        dataLoggerEnabledDeviceConfigurationBuilder.isDirectlyAddressable(true);
        dataLoggerEnabledDeviceConfigurationBuilder.dataloggerEnabled(true);
        dataLoggerConfiguration = dataLoggerEnabledDeviceConfigurationBuilder.add();

        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder1 = dataLoggerConfiguration.createLoadProfileSpec(lpt1);
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder2 = dataLoggerConfiguration.createLoadProfileSpec(lpt2);
        LoadProfileSpec loadProfileSpec1 = loadProfileSpecBuilder1.add();
        LoadProfileSpec loadProfileSpec2 = loadProfileSpecBuilder2.add();

        dataLoggerConfiguration.createChannelSpec(lpt1.findChannelType(registerTypeForChannel1).get(), loadProfileSpec1).overflow(new BigDecimal(1000000L)).add();
        dataLoggerConfiguration.createChannelSpec(lpt1.findChannelType(registerTypeForChannel2).get(), loadProfileSpec1).overflow(new BigDecimal(1000000L)).add();
        dataLoggerConfiguration.createChannelSpec(lpt2.findChannelType(registerTypeForChannel3).get(), loadProfileSpec2).interval(TimeDuration.minutes(15)).overflow(new BigDecimal(1000000L)).add();
        dataLoggerConfiguration.createChannelSpec(lpt2.findChannelType(registerTypeForChannel4).get(), loadProfileSpec2).interval(TimeDuration.minutes(15)).overflow(new BigDecimal(1000000L)).add();
        dataLoggerConfiguration.createChannelSpec(lpt2.findChannelType(registerTypeForChannel5).get(), loadProfileSpec2).interval(TimeDuration.minutes(15)).overflow(new BigDecimal(1000000L)).add();
        dataLoggerConfiguration.createChannelSpec(lpt2.findChannelType(registerTypeForChannel6).get(), loadProfileSpec2).interval(TimeDuration.minutes(15)).overflow(new BigDecimal(1000000L)).add();

        dataLoggerConfiguration.createNumericalRegisterSpec(registerTypeForChannel1).noMultiplier().overflowValue(new BigDecimal(999999L)).numberOfFractionDigits(0).add();
        dataLoggerConfiguration.createNumericalRegisterSpec(registerTypeForChannel2).noMultiplier().overflowValue(new BigDecimal(999999L)).numberOfFractionDigits(0).add();
        dataLoggerConfiguration.createNumericalRegisterSpec(registerTypeForChannel3).noMultiplier().overflowValue(new BigDecimal(999999L)).numberOfFractionDigits(0).add();
        dataLoggerConfiguration.createNumericalRegisterSpec(registerTypeForChannel4).noMultiplier().overflowValue(new BigDecimal(999999L)).numberOfFractionDigits(0).add();
        dataLoggerConfiguration.createNumericalRegisterSpec(registerTypeForChannel5).noMultiplier().overflowValue(new BigDecimal(999999L)).numberOfFractionDigits(0).add();
        dataLoggerConfiguration.createNumericalRegisterSpec(registerTypeForChannel6).noMultiplier().overflowValue(new BigDecimal(999999L)).numberOfFractionDigits(0).add();

        deviceMessageIds.stream().forEach(dataLoggerConfiguration::createDeviceMessageEnablement);
        dataLoggerConfiguration.activate();
    }

    private void setUpForSlaveHavingLoadProfiles() {
        ReadingType registerReadingType1 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.0.255"), kiloWattHours);
        ReadingType registerReadingType2 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.1.255"), kiloWattHours);
        ReadingType registerReadingType3 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.2.255"), kiloWattHours);

        RegisterType registerTypeForChannel1 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(registerReadingType1)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(registerReadingType1, ObisCode.fromString("1.0.1.8.0.255")));
        registerTypeForChannel1.save();
        RegisterType registerTypeForChannel2 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(registerReadingType2)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(registerReadingType2, ObisCode.fromString("1.0.1.8.1.255")));
        registerTypeForChannel2.save();
        RegisterType registerTypeForChannel3 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(registerReadingType3)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(registerReadingType3, ObisCode.fromString("1.0.1.8.2.255")));
        registerTypeForChannel3.save();

        LoadProfileType lpt = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("15min Electricity Slave1", ObisCode.fromString("1.0.99.1.0.255"), TimeDuration.minutes(15), Arrays.asList(registerTypeForChannel1, registerTypeForChannel2, registerTypeForChannel3));
        lpt.save();

        DeviceType slaveDeviceType = inMemoryPersistence.getDeviceConfigurationService()
                .newDataloggerSlaveDeviceTypeBuilder("slave1DeviceType", inMemoryPersistence.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get())
                .create();
        slaveDeviceType.addRegisterType(registerTypeForChannel1);
        slaveDeviceType.addRegisterType(registerTypeForChannel2);
        slaveDeviceType.addRegisterType(registerTypeForChannel3);
        slaveDeviceType.addLoadProfileType(lpt);
        slaveDeviceType.update();

        configurationForSlaveWithLoadProfiles = slaveDeviceType.newConfiguration("Default").add();
        LoadProfileSpec lpSpec = configurationForSlaveWithLoadProfiles.createLoadProfileSpec(lpt).add();

        configurationForSlaveWithLoadProfiles.createChannelSpec(lpt.findChannelType(registerTypeForChannel1).get(), lpSpec).overflow(new BigDecimal(1000000L)).add();
        configurationForSlaveWithLoadProfiles.createChannelSpec(lpt.findChannelType(registerTypeForChannel2).get(), lpSpec).overflow(new BigDecimal(1000000L)).add();
        configurationForSlaveWithLoadProfiles.createChannelSpec(lpt.findChannelType(registerTypeForChannel3).get(), lpSpec).overflow(new BigDecimal(1000000L)).add();

        deviceMessageIds.stream().forEach(configurationForSlaveWithLoadProfiles::createDeviceMessageEnablement);
        configurationForSlaveWithLoadProfiles.activate();

    }

    private void setUpForSlaveHavingRegisters() {
        ReadingType registerReadingType1 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.0.255"), kiloWattHours);
        ReadingType registerReadingType2 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.1.255"), kiloWattHours);
        ReadingType registerReadingType3 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.2.255"), kiloWattHours);

        RegisterType registerTypeForChannel1 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(registerReadingType1)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(registerReadingType1, ObisCode.fromString("1.0.1.8.0.255")));
        registerTypeForChannel1.save();
        RegisterType registerTypeForChannel2 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(registerReadingType2)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(registerReadingType2, ObisCode.fromString("1.0.1.8.1.255")));
        registerTypeForChannel2.save();
        RegisterType registerTypeForChannel3 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(registerReadingType3)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(registerReadingType3, ObisCode.fromString("1.0.1.8.2.255")));
        registerTypeForChannel3.save();

        DeviceType slaveDeviceType = inMemoryPersistence.getDeviceConfigurationService()
                .newDataloggerSlaveDeviceTypeBuilder("slave1DeviceType", inMemoryPersistence.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get())
                .create();
        slaveDeviceType.addRegisterType(registerTypeForChannel1);
        slaveDeviceType.addRegisterType(registerTypeForChannel2);
        slaveDeviceType.addRegisterType(registerTypeForChannel3);
        slaveDeviceType.update();

        configurationForSlaveWithRegisters = slaveDeviceType.newConfiguration("Default").add();
        configurationForSlaveWithRegisters.createNumericalRegisterSpec(registerTypeForChannel1).noMultiplier().overflowValue(new BigDecimal(999999L)).numberOfFractionDigits(0).add();
        configurationForSlaveWithRegisters.createNumericalRegisterSpec(registerTypeForChannel2).noMultiplier().overflowValue(new BigDecimal(999999L)).numberOfFractionDigits(0).add();
        configurationForSlaveWithRegisters.createNumericalRegisterSpec(registerTypeForChannel3).noMultiplier().overflowValue(new BigDecimal(999999L)).numberOfFractionDigits(0).add();

        deviceMessageIds.stream().forEach(configurationForSlaveWithRegisters::createDeviceMessageEnablement);
        configurationForSlaveWithRegisters.activate();

    }

    protected Device createDataLoggerDevice(String name, Instant start) {
        return inMemoryPersistence.getDeviceService().newDevice(dataLoggerConfiguration, name, name + "MrId", start);
    }

    private Device createSlaveWithProfiles(String name, Instant start) {
        return inMemoryPersistence.getDeviceService().newDevice(configurationForSlaveWithLoadProfiles, name, name + "MrId", start);
    }

    private Device createSlaveWithRegisters(String name, Instant start) {
        return inMemoryPersistence.getDeviceService().newDevice(configurationForSlaveWithRegisters, name, name + "MrId", start);
    }

    private void addProfileDataToDevice(Device device, Instant start, Instant end){
        MeterReadingImpl reading = MeterReadingImpl.newInstance();

        IntervalBlockImpl intervalBlock1 = IntervalBlockImpl.of(channelReadingType1.getMRID());
        intervalBlock1.addAllIntervalReadings(profileOfQuarterlyIntervals(start, end , BigDecimal.ONE));
        reading.addIntervalBlock(intervalBlock1);
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(channelReadingType2.getMRID());
        intervalBlock2.addAllIntervalReadings(profileOfQuarterlyIntervals(start, end, new BigDecimal(2)));
        reading.addIntervalBlock(intervalBlock2);
        IntervalBlockImpl intervalBlock3 = IntervalBlockImpl.of(channelReadingType3.getMRID());
        intervalBlock3.addAllIntervalReadings(profileOfQuarterlyIntervals(start, end, new BigDecimal(3)));
        reading.addIntervalBlock(intervalBlock3);
        IntervalBlockImpl intervalBlock4 = IntervalBlockImpl.of(channelReadingType4.getMRID());
        intervalBlock4.addAllIntervalReadings(profileOfQuarterlyIntervals(start, end, new BigDecimal(4)));
        reading.addIntervalBlock(intervalBlock4);
        IntervalBlockImpl intervalBlock5 = IntervalBlockImpl.of(channelReadingType5.getMRID());
        intervalBlock5.addAllIntervalReadings(profileOfQuarterlyIntervals(start, end, new BigDecimal(5)));
        reading.addIntervalBlock(intervalBlock5);
        IntervalBlockImpl intervalBlock6 = IntervalBlockImpl.of(channelReadingType6.getMRID());
        intervalBlock6.addAllIntervalReadings(profileOfQuarterlyIntervals(start, end, new BigDecimal(6)));
        reading.addIntervalBlock(intervalBlock6);

        device.store(reading);
    }

    // We will create a 2 month profile with quarterly readings
    private List<IntervalReading> profileOfQuarterlyIntervals(Instant start, Instant end, BigDecimal value){
        List<IntervalReading> profile = new ArrayList<>();

        Instant readingTime = start;
        do {
           readingTime = readingTime.plus(15,ChronoUnit.MINUTES);
           // ProfileStatus set to Test so to easily filter them
           profile.add(IntervalReadingImpl.of(readingTime, value, ProfileStatus.of(ProfileStatus.Flag.TEST)));
        }
        while(readingTime.isBefore(end));
        return profile;
    }

    private MeterReadingImpl addRegisterDataToDevice(Device device, Instant start, Instant end){
        MeterReadingImpl reading = MeterReadingImpl.newInstance();
        device.getRegisters().forEach((each) -> this.addRandomRegisterReadings(reading ,each, start, end));
        device.store(reading);
        return reading;
    }

    private void addRandomRegisterReadings(MeterReadingImpl meterReading, Register register, Instant start, Instant end){
        Instant readingDate= start.plusSeconds(new Double(randomBetween(86400, 345600)).longValue());
        do{
            meterReading.addReading(ReadingImpl.of(register.getReadingType().getMRID(), BigDecimal.valueOf(randomBetween(110, 1120)), readingDate));
            readingDate = readingDate.plusSeconds(new Double(randomBetween(86400, 345600)).longValue());
        }while(readingDate.isBefore(end));
    }

    private double randomBetween(double minValue, double maxValue) {
        return (Math.random() * (maxValue - minValue)) + minValue;
    }

    @Test
    @Transactional
    public void testLinkSlaveWithoutData(){
        setUpForDataLoggerEnabledDevice();
        setUpForSlaveHavingLoadProfiles();

        Instant start = LocalDateTime.of(2016, 4, 1, 0, 0).toInstant(ZoneOffset.UTC);
        Instant startLink =  LocalDateTime.of(2016, 5, 1, 0, 0).toInstant(ZoneOffset.UTC);
        Instant firstOfJune =  LocalDateTime.of(2016, 6, 1, 0, 0).toInstant(ZoneOffset.UTC);

        Device slave= createSlaveWithProfiles("slave1", start);
        Device dataLogger = createDataLoggerDevice("dataLogger", start);

        HashMap<Channel, Channel> channelMapping = new HashMap<>();

        channelMapping.put(slave.getChannels().get(0), dataLogger.getChannels().get(0));
        channelMapping.put(slave.getChannels().get(1), dataLogger.getChannels().get(1));
        channelMapping.put(slave.getChannels().get(2), dataLogger.getChannels().get(2));
        HashMap<Register, Register> registerMapping = new HashMap<>();

        inMemoryPersistence.getTopologyService().setDataLogger(slave, dataLogger,startLink, channelMapping, registerMapping );

        List<DataLoggerReferenceImpl> dataLoggerReferences = inMemoryPersistence.getTopologyService().dataModel().query(DataLoggerReferenceImpl.class).select(Condition.TRUE);
        assertThat(dataLoggerReferences).hasSize(1);
        assertThat(dataLoggerReferences.get(0).getRange().lowerEndpoint()).isEqualTo(startLink);
        assertThat(dataLoggerReferences.get(0).getRange().hasUpperBound()).isFalse();

        assertThat(dataLoggerReferences.get(0).existsFor(startLink)).isTrue();
        assertThat(dataLoggerReferences.get(0).isEffectiveAt(startLink)).isTrue();

        assertThat(dataLoggerReferences.get(0).existsFor(start)).isFalse();
        assertThat(dataLoggerReferences.get(0).isEffectiveAt(start)).isFalse();

        assertThat(dataLoggerReferences.get(0).existsFor(firstOfJune)).isTrue();
        assertThat(dataLoggerReferences.get(0).isEffectiveAt(firstOfJune)).isTrue();

        assertThat(dataLoggerReferences.get(0).getOrigin().getId()).isEqualTo(slave.getId());
        assertThat(dataLoggerReferences.get(0).getGateway().getId()).isEqualTo(dataLogger.getId());
        assertThat(dataLoggerReferences.get(0).getDataLoggerChannelUsages()).hasSize(3);

        List<Device> slaves = inMemoryPersistence.getTopologyService().findDataLoggerSlaves(dataLogger);
        assertThat(slaves).hasSize(1);
        assertThat(slaves.get(0).getId()).isEqualTo(slave.getId());

        assertThat(inMemoryPersistence.getTopologyService().findAllEffectiveDataLoggerSlaveDevices().find()).hasSize(1);

        assertThat(slave.hasData()).isFalse();
    }

    @Test
    @Transactional
    public void testLinkSlaveWithRegistersWithoutData(){
        setUpForDataLoggerEnabledDevice();
        setUpForSlaveHavingRegisters();

        Instant start = LocalDateTime.of(2016, 4, 1, 0, 0).toInstant(ZoneOffset.UTC);
        Instant startLink =  LocalDateTime.of(2016, 5, 1, 0, 0).toInstant(ZoneOffset.UTC);
        Instant firstOfJune =  LocalDateTime.of(2016, 6, 1, 0, 0).toInstant(ZoneOffset.UTC);

        Device slave = createSlaveWithRegisters("slave1", start);
        Device dataLogger = createDataLoggerDevice("dataLogger", start);

        HashMap<Channel, Channel> channelMapping = new HashMap<>();
        HashMap<Register, Register> registerMapping = new HashMap<>();
        registerMapping.put(slave.getRegisters().get(0), dataLogger.getRegisters().get(0));
        registerMapping.put(slave.getRegisters().get(1), dataLogger.getRegisters().get(1));
        registerMapping.put(slave.getRegisters().get(2), dataLogger.getRegisters().get(2));

        inMemoryPersistence.getTopologyService().setDataLogger(slave, dataLogger,startLink, channelMapping, registerMapping );

        List<DataLoggerReferenceImpl> dataLoggerReferences = inMemoryPersistence.getTopologyService().dataModel().query(DataLoggerReferenceImpl.class).select(Condition.TRUE);
        assertThat(dataLoggerReferences).hasSize(1);
        assertThat(dataLoggerReferences.get(0).getRange().lowerEndpoint()).isEqualTo(startLink);
        assertThat(dataLoggerReferences.get(0).getRange().hasUpperBound()).isFalse();

        assertThat(dataLoggerReferences.get(0).existsFor(startLink)).isTrue();
        assertThat(dataLoggerReferences.get(0).isEffectiveAt(startLink)).isTrue();

        assertThat(dataLoggerReferences.get(0).existsFor(start)).isFalse();
        assertThat(dataLoggerReferences.get(0).isEffectiveAt(start)).isFalse();

        assertThat(dataLoggerReferences.get(0).existsFor(firstOfJune)).isTrue();
        assertThat(dataLoggerReferences.get(0).isEffectiveAt(firstOfJune)).isTrue();

        assertThat(dataLoggerReferences.get(0).getOrigin().getId()).isEqualTo(slave.getId());
        assertThat(dataLoggerReferences.get(0).getGateway().getId()).isEqualTo(dataLogger.getId());
        assertThat(dataLoggerReferences.get(0).getDataLoggerChannelUsages()).hasSize(3);

        List<Device> slaves = inMemoryPersistence.getTopologyService().findDataLoggerSlaves(dataLogger);
        assertThat(slaves).hasSize(1);
        assertThat(slaves.get(0).getId()).isEqualTo(slave.getId());

        assertThat(inMemoryPersistence.getTopologyService().findAllEffectiveDataLoggerSlaveDevices().find()).hasSize(1);

        assertThat(slave.hasData()).isFalse();
    }

     @Test
     @Transactional
     public void testLinkSlaveWithProfileData() {
         setUpForDataLoggerEnabledDevice();
         setUpForSlaveHavingLoadProfiles();

         Instant start = LocalDateTime.of(2016, 4, 1, 0, 0).toInstant(ZoneOffset.UTC);
         Instant startLink = LocalDateTime.of(2016, 5, 1, 0, 0).toInstant(ZoneOffset.UTC);
         Instant endOfData = LocalDateTime.of(2016, 6, 1, 0, 0).toInstant(ZoneOffset.UTC);
         Instant firstOfJuli = LocalDateTime.of(2016, 7, 1, 0, 0).toInstant(ZoneOffset.UTC);

         Device slave= createSlaveWithProfiles("slave1", start);
         assertThat(slave.getChannels().get(0).getLastReading().isPresent()).isFalse();
         assertThat(slave.getChannels().get(1).getLastReading().isPresent()).isFalse();
         assertThat(slave.getChannels().get(2).getLastReading().isPresent()).isFalse();
         Device dataLogger = createDataLoggerDevice("dataLogger", start);
         addProfileDataToDevice(dataLogger, start, endOfData);
         // Make sure the data on the data logger is present
         assertThat(dataLogger.getChannels().get(0).hasData()).isTrue();
         assertThat(dataLogger.getChannels().get(0).getLastDateTime().get()).isEqualTo(endOfData);
         assertThat(dataLogger.getChannels().get(0).getChannelData(Range.closedOpen(start,firstOfJuli))).hasSize(5856); //mont april: 30*24*4 + month may: 31*24*4

         HashMap<Channel, Channel> channelMapping = new HashMap<>();

         channelMapping.put(slave.getChannels().get(0), dataLogger.getChannels().get(0));
         channelMapping.put(slave.getChannels().get(1), dataLogger.getChannels().get(1));
         channelMapping.put(slave.getChannels().get(2), dataLogger.getChannels().get(2));
         HashMap<Register, Register> registerMapping = new HashMap<>();

         inMemoryPersistence.getTopologyService().setDataLogger(slave, dataLogger, startLink, channelMapping, registerMapping );

         assertThat(slave.hasData()).isTrue();
         List<LoadProfileReading> intervals1 = slave.getChannels().get(0).getChannelData(Range.open(start,firstOfJuli)).stream().filter((loadProfileReading -> loadProfileReading.getFlags().contains(ProfileStatus.Flag.TEST))).collect(Collectors.toList());
         List<LoadProfileReading> intervals2 = slave.getChannels().get(0).getChannelData(Range.open(start,firstOfJuli)).stream().filter((loadProfileReading -> loadProfileReading.getFlags().contains(ProfileStatus.Flag.TEST))).collect(Collectors.toList());
         List<LoadProfileReading> intervals3 = slave.getChannels().get(0).getChannelData(Range.open(start,firstOfJuli)).stream().filter((loadProfileReading -> loadProfileReading.getFlags().contains(ProfileStatus.Flag.TEST))).collect(Collectors.toList());

         assertThat(intervals1).hasSize(2976); //month may: 31*24*4
         assertThat(intervals2).hasSize(2976); //month may: 31*24*4
         assertThat(intervals3).hasSize(2976); //month may: 31*24*4

         assertThat(slave.getChannels().get(0).getLastReading().get()).isEqualTo(endOfData);
         assertThat(slave.getChannels().get(1).getLastReading().get()).isEqualTo(endOfData);
         assertThat(slave.getChannels().get(2).getLastReading().get()).isEqualTo(endOfData);
         assertThat(slave.getChannels().get(0).getLastDateTime().get()).isEqualTo(endOfData);
         assertThat(slave.getChannels().get(1).getLastDateTime().get()).isEqualTo(endOfData);
         assertThat(slave.getChannels().get(2).getLastDateTime().get()).isEqualTo(endOfData);

     }

    @Test
    @Transactional
    public void testLinkSlaveWithRegisterData(){
        setUpForDataLoggerEnabledDevice();
        setUpForSlaveHavingRegisters();

        Instant start = LocalDateTime.of(2016, 4, 1, 0, 0).toInstant(ZoneOffset.UTC);
        Instant startLink = LocalDateTime.of(2016, 5, 1, 0, 0).toInstant(ZoneOffset.UTC);
        Instant firstOfJune = LocalDateTime.of(2016, 6, 1, 0, 0).toInstant(ZoneOffset.UTC);

        Device slave = createSlaveWithRegisters("slave1", start);
        Device dataLogger = createDataLoggerDevice("dataLogger", start);
        Register dataLoggerR1 = dataLogger.getRegisterWithDeviceObisCode(ObisCode.fromString("1.0.1.8.0.255"));
        Register dataLoggerR2 = dataLogger.getRegisterWithDeviceObisCode(ObisCode.fromString("1.0.1.8.1.255"));
        Register dataLoggerR3 = dataLogger.getRegisterWithDeviceObisCode(ObisCode.fromString("1.0.1.8.2.255"));


        MeterReadingImpl meterReading = addRegisterDataToDevice(dataLogger, start, firstOfJune);
        //Making sure the data is available
        List<Reading> readingsDataLoggerR1 = meterReading.getReadings().stream().filter((each) -> each.getReadingTypeCode().equals(registerReadingType1.getMRID())).collect(Collectors.toList());
        List<Reading> readingsDataLoggerR2 = meterReading.getReadings().stream().filter((each) -> each.getReadingTypeCode().equals(registerReadingType3.getMRID())).collect(Collectors.toList());
        List<Reading> readingsDataLoggerR3 = meterReading.getReadings().stream().filter((each) -> each.getReadingTypeCode().equals(registerReadingType4.getMRID())).collect(Collectors.toList());

        assertThat(dataLoggerR1.getReadings(Interval.of(Range.openClosed(start, firstOfJune)))).hasSize(readingsDataLoggerR1.size());
        assertThat(dataLoggerR2.getReadings(Interval.of(Range.openClosed(start, firstOfJune)))).hasSize(readingsDataLoggerR2.size());
        assertThat(dataLoggerR3.getReadings(Interval.of(Range.openClosed(start, firstOfJune)))).hasSize(readingsDataLoggerR3.size());

        HashMap<Channel, Channel> channelMapping = new HashMap<>();
        HashMap<Register, Register> registerMapping = new HashMap<>();
        registerMapping.put(slave.getRegisters().get(0), dataLoggerR1);
        registerMapping.put(slave.getRegisters().get(1), dataLoggerR2);
        registerMapping.put(slave.getRegisters().get(2), dataLoggerR3);

        inMemoryPersistence.getTopologyService().setDataLogger(slave, dataLogger,startLink, channelMapping, registerMapping );

        List<DataLoggerReferenceImpl> dataLoggerReferences = inMemoryPersistence.getTopologyService().dataModel().query(DataLoggerReferenceImpl.class).select(Condition.TRUE);
        assertThat(dataLoggerReferences).hasSize(1);
        assertThat(dataLoggerReferences.get(0).getRange().lowerEndpoint()).isEqualTo(startLink);
        assertThat(dataLoggerReferences.get(0).getRange().hasUpperBound()).isFalse();

        assertThat(slave.hasData()).isTrue();
        assertThat(slave.getRegisters().get(0).hasData()).isTrue();
        assertThat(slave.getRegisters().get(1).hasData()).isTrue();
        assertThat(slave.getRegisters().get(2).hasData()).isTrue();

        assertThat(slave.getRegisters().get(0).getLastReadingDate()).isEqualTo(dataLoggerR1.getLastReadingDate()); // all data on data logger after link date copied
        assertThat(slave.getRegisters().get(1).getLastReadingDate()).isEqualTo(dataLoggerR2.getLastReadingDate());
        assertThat(slave.getRegisters().get(2).getLastReadingDate()).isEqualTo(dataLoggerR3.getLastReadingDate());

        assertThat(slave.getRegisters().get(0).getReadings(Interval.of(Range.atLeast(startLink)))).hasSize(new Long(readingsDataLoggerR1.stream().filter((reading) -> Range.atLeast(startLink).contains(reading.getTimeStamp())).count()).intValue());
        assertThat(slave.getRegisters().get(1).getReadings(Interval.of(Range.atLeast(startLink)))).hasSize(new Long(readingsDataLoggerR2.stream().filter((reading) -> Range.atLeast(startLink).contains(reading.getTimeStamp())).count()).intValue());
        assertThat(slave.getRegisters().get(2).getReadings(Interval.of(Range.atLeast(startLink)))).hasSize(new Long(readingsDataLoggerR3.stream().filter((reading) -> Range.atLeast(startLink).contains(reading.getTimeStamp())).count()).intValue());

        assertThat(slave.getRegisters().get(0).getReadings(Interval.of(Range.openClosed(start, startLink)))).isEmpty(); // No data before link date
        assertThat(slave.getRegisters().get(1).getReadings(Interval.of(Range.openClosed(start, startLink)))).isEmpty();
        assertThat(slave.getRegisters().get(2).getReadings(Interval.of(Range.openClosed(start, startLink)))).isEmpty();
    }


    @Test
    @Transactional
    public void testUnLinkSlaveWithoutData(){
        setUpForDataLoggerEnabledDevice();
        setUpForSlaveHavingLoadProfiles();

        Instant start = LocalDateTime.of(2016, 4, 1, 0, 0).toInstant(ZoneOffset.UTC);
        Instant startLink =  LocalDateTime.of(2016, 5, 1, 0, 0).toInstant(ZoneOffset.UTC);
        Instant midMay =  LocalDateTime.of(2016, 5, 15, 0, 0).toInstant(ZoneOffset.UTC);

        Device slave = createSlaveWithProfiles("slave1", start);
        assertThat(slave.getMeterActivationsMostRecentFirst()).hasSize(1);
        Device dataLogger = createDataLoggerDevice("dataLogger", start);

        HashMap<Channel, Channel> channelMapping = new HashMap<>();

        channelMapping.put(slave.getChannels().get(0), dataLogger.getChannels().get(0));
        channelMapping.put(slave.getChannels().get(1), dataLogger.getChannels().get(1));
        channelMapping.put(slave.getChannels().get(2), dataLogger.getChannels().get(2));
        HashMap<Register, Register> registerMapping = new HashMap<>();

        inMemoryPersistence.getTopologyService().setDataLogger(slave, dataLogger,startLink, channelMapping, registerMapping );

        List<Device> slaves = inMemoryPersistence.getTopologyService().findDataLoggerSlaves(dataLogger);
        assertThat(slaves).hasSize(1);
        assertThat(slaves.get(0).getId()).isEqualTo(slave.getId());

        inMemoryPersistence.getTopologyService().clearDataLogger(slaves.get(0) ,midMay);

        assertThat(inMemoryPersistence.getTopologyService().findAllEffectiveDataLoggerSlaveDevices().find()).isEmpty();
        assertThat(inMemoryPersistence.getTopologyService().findDataLoggerSlaves(dataLogger)).isEmpty();

        List<MeterActivation> meterActivations = slaves.get(0).getMeterActivationsMostRecentFirst();
        assertThat(meterActivations).hasSize(2); // A new MeterActivation was started
        assertThat(meterActivations.get(0).getChannels()).hasSize(3); // The new MeterActivation has channels
        assertThat(meterActivations.get(0).getChannels().get(0).hasData()).isFalse();
        assertThat(meterActivations.get(0).getChannels().get(1).hasData()).isFalse();
        assertThat(meterActivations.get(0).getChannels().get(2).hasData()).isFalse();

    }

    @Test
    @Transactional
    public void testUnLinkSlaveWithProfileData(){
        setUpForDataLoggerEnabledDevice();
        setUpForSlaveHavingLoadProfiles();

        Instant start = LocalDateTime.of(2016, 4, 1, 0, 0).toInstant(ZoneOffset.UTC);
        Instant startLink =  LocalDateTime.of(2016, 5, 1, 0, 0).toInstant(ZoneOffset.UTC);
        Instant endLink =  LocalDateTime.of(2016, 5, 15, 0, 0).toInstant(ZoneOffset.UTC);
        Instant endOfData =   LocalDateTime.of(2016, 6, 1, 0, 0).toInstant(ZoneOffset.UTC);
        Instant firstOfJuli =  LocalDateTime.of(2016, 7, 1, 0, 0).toInstant(ZoneOffset.UTC);

        Device slave = createSlaveWithProfiles("slave1", start);
        Device dataLogger = createDataLoggerDevice("dataLogger", start);
        addProfileDataToDevice(dataLogger, start, endOfData);
        // Make sure the data on the data logger is present
        assertThat(dataLogger.getChannels().get(0).hasData()).isTrue();
        assertThat(dataLogger.getChannels().get(0).getLastDateTime().get()).isEqualTo(endOfData);
        assertThat(dataLogger.getChannels().get(0).getChannelData(Range.closedOpen(start,firstOfJuli))).hasSize(5856); //mont april: 30*24*4 + month may: 31*24*4

        HashMap<Channel, Channel> channelMapping = new HashMap<>();

        channelMapping.put(slave.getChannels().get(0), dataLogger.getChannels().get(0));
        channelMapping.put(slave.getChannels().get(1), dataLogger.getChannels().get(1));
        channelMapping.put(slave.getChannels().get(2), dataLogger.getChannels().get(2));
        HashMap<Register, Register> registerMapping = new HashMap<>();

        inMemoryPersistence.getTopologyService().setDataLogger(slave, dataLogger, startLink, channelMapping, registerMapping );

        assertThat(slave.hasData()).isTrue();
        // Making sure all data is in the slave channels until 2016/06/01
        assertThat(slave.getChannels().get(0).getLastDateTime().get()).isEqualTo(endOfData);
        assertThat(slave.getChannels().get(1).getLastDateTime().get()).isEqualTo(endOfData);
        assertThat(slave.getChannels().get(2).getLastDateTime().get()).isEqualTo(endOfData);

        inMemoryPersistence.getTopologyService().clearDataLogger(slave ,endLink);

        assertThat(inMemoryPersistence.getTopologyService().findAllEffectiveDataLoggerSlaveDevices().find()).isEmpty();
        assertThat(inMemoryPersistence.getTopologyService().findDataLoggerSlaves(dataLogger)).isEmpty();

        List<MeterActivation> meterActivations = slave.getMeterActivationsMostRecentFirst();
        assertThat(meterActivations).hasSize(2); // A new MeterActivation was started
        assertThat(meterActivations.get(0).isEffectiveAt(endLink)).isTrue();
        assertThat(meterActivations.get(1).getRange().upperEndpoint()).isEqualTo(endLink);
        assertThat(meterActivations.get(0).getChannels()).hasSize(3); // The new MeterActivation has channels
        assertThat(meterActivations.get(0).getChannels().get(0).hasData()).isFalse();  // No data on new MeterActivation
        assertThat(meterActivations.get(0).getChannels().get(1).hasData()).isFalse();
        assertThat(meterActivations.get(0).getChannels().get(2).hasData()).isFalse();
        assertThat(meterActivations.get(1).getChannels().get(0).hasData()).isTrue();  // Still data on previous MeterActivation
        assertThat(meterActivations.get(1).getChannels().get(1).hasData()).isTrue();
        assertThat(meterActivations.get(1).getChannels().get(2).hasData()).isTrue();
    }

    @Test
    @Transactional
    public void testUnLinkSlaveWithRegisterData(){
        setUpForDataLoggerEnabledDevice();
        setUpForSlaveHavingRegisters();

        Instant start = LocalDateTime.of(2016, 4, 1, 0, 0).toInstant(ZoneOffset.UTC);
        Instant startLink = LocalDateTime.of(2016, 5, 1, 0, 0).toInstant(ZoneOffset.UTC);
        Instant firstOfJune = LocalDateTime.of(2016, 6, 1, 0, 0).toInstant(ZoneOffset.UTC);
        Instant endLink =  LocalDateTime.of(2016, 5, 15, 0, 0).toInstant(ZoneOffset.UTC);

        Device slave = createSlaveWithRegisters("slave1", start);
        Device dataLogger = createDataLoggerDevice("dataLogger", start);
        Register dataLoggerR1 = dataLogger.getRegisterWithDeviceObisCode(ObisCode.fromString("1.0.1.8.0.255"));
        Register dataLoggerR2 = dataLogger.getRegisterWithDeviceObisCode(ObisCode.fromString("1.0.1.8.1.255"));
        Register dataLoggerR3 = dataLogger.getRegisterWithDeviceObisCode(ObisCode.fromString("1.0.1.8.2.255"));

        MeterReadingImpl meterReading = addRegisterDataToDevice(dataLogger, start, firstOfJune);
        //Making sure the data is available
        List<Reading> readingsDataLoggerR1 = meterReading.getReadings().stream().filter((each) -> each.getReadingTypeCode().equals(registerReadingType1.getMRID())).collect(Collectors.toList());
        List<Reading> readingsDataLoggerR2 = meterReading.getReadings().stream().filter((each) -> each.getReadingTypeCode().equals(registerReadingType3.getMRID())).collect(Collectors.toList());
        List<Reading> readingsDataLoggerR3 = meterReading.getReadings().stream().filter((each) -> each.getReadingTypeCode().equals(registerReadingType4.getMRID())).collect(Collectors.toList());

        HashMap<Channel, Channel> channelMapping = new HashMap<>();
        HashMap<Register, Register> registerMapping = new HashMap<>();
        registerMapping.put(slave.getRegisters().get(0), dataLoggerR1);
        registerMapping.put(slave.getRegisters().get(1), dataLoggerR2);
        registerMapping.put(slave.getRegisters().get(2), dataLoggerR3);

        inMemoryPersistence.getTopologyService().setDataLogger(slave, dataLogger,startLink, channelMapping, registerMapping );
        // Making sure data has been transferred form data logge<r to slave
        assertThat(slave.getRegisters().get(0).getReadings(Interval.of(Range.atLeast(startLink)))).hasSize(new Long(readingsDataLoggerR1.stream().filter((reading) -> Range.atLeast(startLink).contains(reading.getTimeStamp())).count()).intValue());
        assertThat(slave.getRegisters().get(1).getReadings(Interval.of(Range.atLeast(startLink)))).hasSize(new Long(readingsDataLoggerR2.stream().filter((reading) -> Range.atLeast(startLink).contains(reading.getTimeStamp())).count()).intValue());
        assertThat(slave.getRegisters().get(2).getReadings(Interval.of(Range.atLeast(startLink)))).hasSize(new Long(readingsDataLoggerR3.stream().filter((reading) -> Range.atLeast(startLink).contains(reading.getTimeStamp())).count()).intValue());

        inMemoryPersistence.getTopologyService().clearDataLogger(slave ,endLink);

        assertThat(inMemoryPersistence.getTopologyService().findAllEffectiveDataLoggerSlaveDevices().find()).isEmpty();
        assertThat(inMemoryPersistence.getTopologyService().findDataLoggerSlaves(dataLogger)).isEmpty();

        List<MeterActivation> meterActivations = slave.getMeterActivationsMostRecentFirst();
        assertThat(meterActivations).hasSize(2); // A new MeterActivation was started
        assertThat(meterActivations.get(0).isEffectiveAt(endLink)).isTrue();
        assertThat(meterActivations.get(1).getRange().upperEndpoint()).isEqualTo(endLink);
        assertThat(meterActivations.get(0).getChannels()).hasSize(3); // The new MeterActivation has channels
        assertThat(meterActivations.get(0).getChannels().get(0).hasData()).isFalse();  // No data on new MeterActivation
        assertThat(meterActivations.get(0).getChannels().get(1).hasData()).isFalse();
        assertThat(meterActivations.get(0).getChannels().get(2).hasData()).isFalse();
        assertThat(meterActivations.get(1).getChannels().get(0).getReadings(Range.openClosed(startLink, endLink))).hasSize(new Long(readingsDataLoggerR1.stream().filter((reading) -> Range.openClosed(startLink, endLink).contains(reading.getTimeStamp())).count()).intValue());  // Still data on previous MeterActivation
        assertThat(meterActivations.get(1).getChannels().get(1).getReadings(Range.openClosed(startLink, endLink))).hasSize(new Long(readingsDataLoggerR2.stream().filter((reading) -> Range.openClosed(startLink, endLink).contains(reading.getTimeStamp())).count()).intValue());
        assertThat(meterActivations.get(1).getChannels().get(2).getReadings(Range.openClosed(startLink, endLink))).hasSize(new Long(readingsDataLoggerR3.stream().filter((reading) -> Range.openClosed(startLink, endLink).contains(reading.getTimeStamp())).count()).intValue());

    }




}
