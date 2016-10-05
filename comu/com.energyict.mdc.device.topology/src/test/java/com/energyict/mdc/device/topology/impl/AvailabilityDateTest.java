package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * Copyrights EnergyICT
 * Date: 22/06/2016
 * Time: 15:37
 */
public class AvailabilityDateTest extends PersistenceIntegrationTest {

    private final static Unit kiloWattHours = Unit.get("kWh");

    private DeviceConfiguration dataLoggerConfiguration, configurationForSlaveWithLoadProfiles, configurationForSlaveWithRegisters;

    private void setUpForDataLoggerEnabledDevice() {
        // set up for first loadProfile
        ReadingType registerReadingType1 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.0.255"), kiloWattHours);
        ReadingType registerReadingType2 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.2.8.0.255"), kiloWattHours);
        // set up for second loadProfile
        ReadingType registerReadingType3 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.1.255"), kiloWattHours);
        ReadingType registerReadingType4 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.2.255"), kiloWattHours);
        ReadingType registerReadingType5 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.2.8.1.255"), kiloWattHours);
        ReadingType registerReadingType6 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.2.8.2.255"), kiloWattHours);

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

    private Device createDataLoggerDevice(String name, Instant start) {
        return inMemoryPersistence.getDeviceService().newDevice(dataLoggerConfiguration, name, name + "MrId", start);
    }

    private Device createSlaveWithProfiles(String name, Instant start) {
        return inMemoryPersistence.getDeviceService().newDevice(configurationForSlaveWithLoadProfiles, name, name + "MrId", start);
    }

    private Device createSlaveWithRegisters(String name, Instant start) {
        return inMemoryPersistence.getDeviceService().newDevice(configurationForSlaveWithRegisters, name, name + "MrId", start);
    }

    @Test
    @Transactional
    public void testUnlinkedDataLogger() {
        setUpForDataLoggerEnabledDevice();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(inMemoryPersistence.getClock().instant(), ZoneOffset.UTC).withHour(1).withMinute(0).withSecond(0).withNano(0);
        Instant start = localDateTime.toInstant(ZoneOffset.UTC);
        Device dataLogger = createDataLoggerDevice("dataLogger", start);

        List<Instant> availabilityOfChannels = dataLogger.getChannels().stream().map((each)-> inMemoryPersistence.getTopologyService().availabilityDate(each).get()).distinct().collect(Collectors.toList());
        assertThat(availabilityOfChannels).hasSize(1);
        assertThat((availabilityOfChannels).get(0)).isEqualTo(start); // Never linked

        List<Instant> availabilityOfRegisters = dataLogger.getRegisters().stream().map((each)-> inMemoryPersistence.getTopologyService().availabilityDate(each).get()).distinct().collect(Collectors.toList());
        assertThat(availabilityOfRegisters).hasSize(1);
        assertThat((availabilityOfRegisters).get(0)).isEqualTo(start); // Never linked
    }

    @Test
    @Transactional
    public void testLinkedDataLoggerChannelAvailability() {
        setUpForDataLoggerEnabledDevice();
        setUpForSlaveHavingLoadProfiles();

        LocalDateTime localDateTime = LocalDateTime.ofInstant(inMemoryPersistence.getClock().instant(), ZoneOffset.UTC).withHour(1).withMinute(0).withSecond(0).withNano(0);
        Instant start = localDateTime.toInstant(ZoneOffset.UTC);
        Instant startLink = start.plus(1, ChronoUnit.DAYS);

        Device slave = createSlaveWithProfiles("slave1", start);
        Device dataLogger = createDataLoggerDevice("dataLogger", start);

        HashMap<Channel, Channel> channelMapping = new HashMap<>();

        channelMapping.put(slave.getChannels().get(0), dataLogger.getChannels().get(0));
        channelMapping.put(slave.getChannels().get(1), dataLogger.getChannels().get(1));
        channelMapping.put(slave.getChannels().get(2), dataLogger.getChannels().get(2));
        HashMap<Register, Register> registerMapping = new HashMap<>();

        inMemoryPersistence.getTopologyService().setDataLogger(slave, dataLogger, startLink, channelMapping, registerMapping);

        // first 3 data logger channels are not available for linking to a slave channel
        Optional<Instant> availabilityChannel0 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(0));
        assertThat(availabilityChannel0.isPresent()).isFalse();
        Optional<Instant> availabilityChannel1 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(1));
        assertThat(availabilityChannel1.isPresent()).isFalse();
        Optional<Instant> availabilityChannel2 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(2));
        assertThat(availabilityChannel2.isPresent()).isFalse();
        // The other 3 channels were never linked and can be linked from the 'shipmentDate' of the datalogger
        Optional<Instant> availabilityChannel3 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(3));
        assertThat(availabilityChannel3.get()).isEqualTo(start);
        Optional<Instant> availabilityChannel4 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(4));
        assertThat(availabilityChannel4.get()).isEqualTo(start);
        Optional<Instant> availabilityChannel5 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(5));
        assertThat(availabilityChannel5.get()).isEqualTo(start);
    }

    @Test
    @Transactional
    public void testUnLinkedDataLoggerChannelAvailability() {
        setUpForDataLoggerEnabledDevice();
        setUpForSlaveHavingLoadProfiles();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(inMemoryPersistence.getClock().instant(), ZoneOffset.UTC);
        Instant start = localDateTime.withHour(1).withMinute(0).withSecond(0).withNano(0).toInstant(ZoneOffset.UTC);
        Instant startLink = start.plus(1, ChronoUnit.DAYS);
        Instant endLink = start.plus(6, ChronoUnit.DAYS);

        Device slave = createSlaveWithProfiles("slave1", start);
        Device dataLogger = createDataLoggerDevice("dataLogger", start);

        HashMap<Channel, Channel> channelMapping = new HashMap<>();

        channelMapping.put(slave.getChannels().get(0), dataLogger.getChannels().get(0));
        channelMapping.put(slave.getChannels().get(1), dataLogger.getChannels().get(1));
        channelMapping.put(slave.getChannels().get(2), dataLogger.getChannels().get(2));
        HashMap<Register, Register> registerMapping = new HashMap<>();

        inMemoryPersistence.getTopologyService().setDataLogger(slave, dataLogger, startLink, channelMapping, registerMapping);
        inMemoryPersistence.getTopologyService().clearDataLogger(slave, endLink);

        // first 3 data logger channels are available since endLink
        Optional<Instant> availabilityChannel0 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(0));
        assertThat(availabilityChannel0.get()).isEqualTo(endLink);
        Optional<Instant> availabilityChannel1 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(1));
        assertThat(availabilityChannel1.get()).isEqualTo(endLink);
        Optional<Instant> availabilityChannel2 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(2));
        assertThat(availabilityChannel2.get()).isEqualTo(endLink);
        // The other 3 channels were never linked and can be linked from the 'shipmentDate' of the datalogger
        Optional<Instant> availabilityChannel3 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(3));
        assertThat(availabilityChannel3.get()).isEqualTo(start);
        Optional<Instant> availabilityChannel4 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(4));
        assertThat(availabilityChannel4.get()).isEqualTo(start);
        Optional<Instant> availabilityChannel5 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(5));
        assertThat(availabilityChannel5.get()).isEqualTo(start);
    }

    @Test
    @Transactional
    public void testMultipleLinkedDataLoggerChannelAvailability() {
        setUpForDataLoggerEnabledDevice();
        setUpForSlaveHavingLoadProfiles();

        LocalDateTime localDateTime = LocalDateTime.ofInstant(inMemoryPersistence.getClock().instant(), ZoneOffset.UTC).withHour(1).withMinute(0).withSecond(0).withNano(0);
        Instant start = localDateTime.toInstant(ZoneOffset.UTC);
        Instant startLink = start.plus(1, ChronoUnit.DAYS);
        Instant endLink = start.plus(6, ChronoUnit.DAYS);
        Instant startLink2 = start.plus(8, ChronoUnit.DAYS);
        Instant endLink2 = start.plus(10, ChronoUnit.DAYS);

        Device slave = createSlaveWithProfiles("slave1", start);
        Device dataLogger = createDataLoggerDevice("dataLogger", start);

        HashMap<Channel, Channel> channelMapping = new HashMap<>();

        channelMapping.put(slave.getChannels().get(0), dataLogger.getChannels().get(0));
        channelMapping.put(slave.getChannels().get(1), dataLogger.getChannels().get(1));
        channelMapping.put(slave.getChannels().get(2), dataLogger.getChannels().get(2));
        HashMap<Register, Register> registerMapping = new HashMap<>();

        inMemoryPersistence.getTopologyService().setDataLogger(slave, dataLogger, startLink, channelMapping, registerMapping);
        inMemoryPersistence.getTopologyService().clearDataLogger(slave, endLink);

        channelMapping = new HashMap<>();
        channelMapping.put(slave.getChannels().get(0), dataLogger.getChannels().get(3));
        channelMapping.put(slave.getChannels().get(1), dataLogger.getChannels().get(4));
        channelMapping.put(slave.getChannels().get(2), dataLogger.getChannels().get(5));
        inMemoryPersistence.getTopologyService().setDataLogger(slave, dataLogger, startLink2, channelMapping, registerMapping);

        // first 3 data logger channels are available since
        Optional<Instant> availabilityChannel0 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(0));
        assertThat(availabilityChannel0.get()).isEqualTo(endLink);
        Optional<Instant> availabilityChannel1 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(1));
        assertThat(availabilityChannel1.get()).isEqualTo(endLink);
        Optional<Instant> availabilityChannel2 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(2));
        assertThat(availabilityChannel2.get()).isEqualTo(endLink);
        // The other 3 channels are still linked and not available
        Optional<Instant> availabilityChannel3 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(3));
        assertThat(availabilityChannel3.isPresent()).isFalse();
        Optional<Instant> availabilityChannel4 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(4));
        assertThat(availabilityChannel4.isPresent()).isFalse();
        Optional<Instant> availabilityChannel5 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(5));
        assertThat(availabilityChannel5.isPresent()).isFalse();

        inMemoryPersistence.getTopologyService().clearDataLogger(slave, endLink2);
        // The last 3 channels are again available since endLink2
        availabilityChannel3 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(3));
        assertThat(availabilityChannel3.get()).isEqualTo(endLink2);
        availabilityChannel4 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(4));
        assertThat(availabilityChannel4.get()).isEqualTo(endLink2);
        availabilityChannel5 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getChannels().get(5));
        assertThat(availabilityChannel5.get()).isEqualTo(endLink2);
    }

    @Test
    @Transactional
    public void testLinkedDataLoggerRegisterAvailability() {
        setUpForDataLoggerEnabledDevice();
        setUpForSlaveHavingRegisters();

        LocalDateTime localDateTime = LocalDateTime.ofInstant(inMemoryPersistence.getClock().instant(), ZoneOffset.UTC).withHour(1).withMinute(0).withSecond(0).withNano(0);
        Instant start = localDateTime.toInstant(ZoneOffset.UTC);
        Instant startLink = start.plus(1, ChronoUnit.DAYS);

        Device slave = createSlaveWithRegisters("slave1", start);
        Device dataLogger = createDataLoggerDevice("dataLogger", start);

        HashMap<Channel, Channel> channelMapping = new HashMap<>();
        HashMap<Register, Register> registerMapping = new HashMap<>();

        registerMapping.put(slave.getRegisters().get(0), dataLogger.getRegisters().get(0));
        registerMapping.put(slave.getRegisters().get(1), dataLogger.getRegisters().get(1));
        registerMapping.put(slave.getRegisters().get(2), dataLogger.getRegisters().get(2));

        inMemoryPersistence.getTopologyService().setDataLogger(slave, dataLogger, startLink, channelMapping, registerMapping);

        // first 3 data logger channels are not available for linking to a slave channel
        Optional<Instant> availabilityRegister0 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getRegisters().get(0));
        assertThat(availabilityRegister0.isPresent()).isFalse();
        Optional<Instant> availabilityRegister1 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getRegisters().get(1));
        assertThat(availabilityRegister1.isPresent()).isFalse();
        Optional<Instant> availabilityRegister2 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getRegisters().get(2));
        assertThat(availabilityRegister2.isPresent()).isFalse();

    }

    @Test
    @Transactional
    public void testUnLinkedDataLoggerRegisterAvailability() {
        setUpForDataLoggerEnabledDevice();
        setUpForSlaveHavingRegisters();

        LocalDateTime localDateTime = LocalDateTime.ofInstant(inMemoryPersistence.getClock().instant(), ZoneOffset.UTC).withHour(1).withMinute(0).withSecond(0).withNano(0);
        Instant start = localDateTime.toInstant(ZoneOffset.UTC);
        Instant startLink = start.plus(1, ChronoUnit.DAYS);
        Instant endLink = start.plus(6, ChronoUnit.DAYS);

        Device slave = createSlaveWithRegisters("slave1", start);
        Device dataLogger = createDataLoggerDevice("dataLogger", start);

        HashMap<Channel, Channel> channelMapping = new HashMap<>();
        HashMap<Register, Register> registerMapping = new HashMap<>();
        registerMapping.put(slave.getRegisters().get(0), dataLogger.getRegisters().get(0));
        registerMapping.put(slave.getRegisters().get(1), dataLogger.getRegisters().get(1));
        registerMapping.put(slave.getRegisters().get(2), dataLogger.getRegisters().get(2));

        inMemoryPersistence.getTopologyService().setDataLogger(slave, dataLogger, startLink, channelMapping, registerMapping);
        inMemoryPersistence.getTopologyService().clearDataLogger(slave, endLink);

        // first 3 data logger registers are available since endLink
        Optional<Instant> availabilityRegister0 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getRegisters().get(0));
        assertThat(availabilityRegister0.get()).isEqualTo(endLink);
        Optional<Instant> availabilityRegister1 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getRegisters().get(1));
        assertThat(availabilityRegister1.get()).isEqualTo(endLink);
        Optional<Instant> availabilityRegister2 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getRegisters().get(2));
        assertThat(availabilityRegister2.get()).isEqualTo(endLink);
    }

    @Test
    @Transactional
    public void testMultipleLinkedDataLoggerRegisterAvailability() {
        setUpForDataLoggerEnabledDevice();
        setUpForSlaveHavingRegisters();

        LocalDateTime localDateTime = LocalDateTime.ofInstant(inMemoryPersistence.getClock().instant(), ZoneOffset.UTC).withHour(1).withMinute(0).withSecond(0).withNano(0);
        Instant start = localDateTime.toInstant(ZoneOffset.UTC);
        Instant startLink = start.plus(1, ChronoUnit.DAYS);
        Instant endLink = start.plus(6, ChronoUnit.DAYS);
        Instant startLink2 = start.plus(8, ChronoUnit.DAYS);
        Instant endLink2 = start.plus(10, ChronoUnit.DAYS);

        Device slave = createSlaveWithRegisters("slave1", start);
        Device dataLogger = createDataLoggerDevice("dataLogger", start);

        HashMap<Channel, Channel> channelMapping = new HashMap<>();
        HashMap<Register, Register> registerMapping = new HashMap<>();
        registerMapping.put(slave.getRegisters().get(0), dataLogger.getRegisters().get(0));
        registerMapping.put(slave.getRegisters().get(1), dataLogger.getRegisters().get(1));
        registerMapping.put(slave.getRegisters().get(2), dataLogger.getRegisters().get(2));

        inMemoryPersistence.getTopologyService().setDataLogger(slave, dataLogger, startLink, channelMapping, registerMapping);
        inMemoryPersistence.getTopologyService().clearDataLogger(slave, endLink);

        registerMapping = new HashMap<>();
        registerMapping.put(slave.getRegisters().get(0), dataLogger.getRegisters().get(3));
        registerMapping.put(slave.getRegisters().get(1), dataLogger.getRegisters().get(4));
        registerMapping.put(slave.getRegisters().get(2), dataLogger.getRegisters().get(5));

        inMemoryPersistence.getTopologyService().setDataLogger(slave, dataLogger, startLink2, channelMapping, registerMapping);

        // first 3 data logger registers are available since
        Optional<Instant> availabilityRegister0 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getRegisters().get(0));
        assertThat(availabilityRegister0.get()).isEqualTo(endLink);
        Optional<Instant> availabilityRegister1 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getRegisters().get(1));
        assertThat(availabilityRegister1.get()).isEqualTo(endLink);
        Optional<Instant> availabilityRegister2 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getRegisters().get(2));
        assertThat(availabilityRegister2.get()).isEqualTo(endLink);
        // The other 3 channels are still linked and not available
        Optional<Instant> availabilityRegister3 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getRegisters().get(3));
        assertThat(availabilityRegister3.isPresent()).isFalse();
        Optional<Instant> availabilityRegister4 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getRegisters().get(4));
        assertThat(availabilityRegister4.isPresent()).isFalse();
        Optional<Instant> availabilityRegister5 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getRegisters().get(5));
        assertThat(availabilityRegister5.isPresent()).isFalse();

        inMemoryPersistence.getTopologyService().clearDataLogger(slave, endLink2);
        // The last 3 channels are again available since endLink2
        availabilityRegister3 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getRegisters().get(3));
        assertThat(availabilityRegister3.get()).isEqualTo(endLink2);
        availabilityRegister4 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getRegisters().get(4));
        assertThat(availabilityRegister4.get()).isEqualTo(endLink2);
        availabilityRegister5 = inMemoryPersistence.getTopologyService().availabilityDate(dataLogger.getRegisters().get(5));
        assertThat(availabilityRegister5.get()).isEqualTo(endLink2);
    }


}
