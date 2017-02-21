/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.conditions.Condition;
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

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataLoggerChannelUsageImplTest extends PersistenceIntegrationTest {

    private final static Unit kiloWattHours = Unit.get("kWh");

    private DeviceConfiguration dataLoggerConfiguration, slave1DeviceConfiguration, slave2DeviceConfiguration;

    private ReadingType readingTypeForChannel1, readingTypeForChannel2, readingTypeForChannel3, readingTypeForChannel4, readingTypeForChannel5, readingTypeForChannel6;

    private void setUpForDataLoggerEnabledDevice() {
        // set up for first loadProfile
        readingTypeForChannel1 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.0.255"), kiloWattHours);
        readingTypeForChannel2 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.2.8.0.255"), kiloWattHours);
        // set up for second loadProfile
        readingTypeForChannel3 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.1.255"), kiloWattHours);
        readingTypeForChannel4 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.2.255"), kiloWattHours);
        readingTypeForChannel5 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.2.8.1.255"), kiloWattHours);
        readingTypeForChannel6 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.2.8.2.255"), kiloWattHours);

        RegisterType registerTypeForChannel1 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingTypeForChannel1)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(readingTypeForChannel1, ObisCode.fromString("1.0.1.8.0.255")));
        RegisterType registerTypeForChannel2 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingTypeForChannel2)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(readingTypeForChannel2, ObisCode.fromString("1.0.2.8.0.255")));
        RegisterType registerTypeForChannel3 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingTypeForChannel3)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(readingTypeForChannel3, ObisCode.fromString("1.0.1.8.1.255")));
        registerTypeForChannel3.save();
        RegisterType registerTypeForChannel4 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingTypeForChannel4)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(readingTypeForChannel4, ObisCode.fromString("1.0.1.8.2.255")));
        registerTypeForChannel4.save();
        RegisterType registerTypeForChannel5 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingTypeForChannel5)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(readingTypeForChannel5, ObisCode.fromString("1.0.2.8.1.255")));
        registerTypeForChannel5.save();
        RegisterType registerTypeForChannel6 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingTypeForChannel6)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(readingTypeForChannel6, ObisCode.fromString("1.0.2.8.2.255")));
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

        deviceMessageIds.stream().forEach(dataLoggerConfiguration::createDeviceMessageEnablement);
        dataLoggerConfiguration.activate();
    }

    private void setUpForSlave1Device() {
        ReadingType readingTypeForChannel1 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.0.255"), kiloWattHours);
        ReadingType readingTypeForChannel2 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.1.255"), kiloWattHours);
        ReadingType readingTypeForChannel3 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.2.255"), kiloWattHours);

        RegisterType registerTypeForChannel1 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingTypeForChannel1)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(readingTypeForChannel1, ObisCode.fromString("1.0.1.8.0.255")));
        registerTypeForChannel1.save();
        RegisterType registerTypeForChannel2 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingTypeForChannel2)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(readingTypeForChannel2, ObisCode.fromString("1.0.1.8.1.255")));
        registerTypeForChannel2.save();
        RegisterType registerTypeForChannel3 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingTypeForChannel3)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(readingTypeForChannel3, ObisCode.fromString("1.0.1.8.2.255")));
        registerTypeForChannel3.save();

        LoadProfileType lpt = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("15min Electricity Slave1", ObisCode.fromString("1.0.99.1.0.255"), TimeDuration.minutes(15), Arrays.asList(registerTypeForChannel1, registerTypeForChannel2, registerTypeForChannel3));
        lpt.save();

        DeviceType slave1DeviceType = inMemoryPersistence.getDeviceConfigurationService()
                .newDataloggerSlaveDeviceTypeBuilder("slave1DeviceType", inMemoryPersistence.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get())
                .create();
        slave1DeviceType.addRegisterType(registerTypeForChannel1);
        slave1DeviceType.addRegisterType(registerTypeForChannel2);
        slave1DeviceType.addRegisterType(registerTypeForChannel3);
        slave1DeviceType.addLoadProfileType(lpt);
        slave1DeviceType.update();

        slave1DeviceConfiguration = slave1DeviceType.newConfiguration("Default").add();
        LoadProfileSpec lpSpec = slave1DeviceConfiguration.createLoadProfileSpec(lpt).add();

        slave1DeviceConfiguration.createChannelSpec(lpt.findChannelType(registerTypeForChannel1).get(), lpSpec).overflow(new BigDecimal(1000000L)).add();
        slave1DeviceConfiguration.createChannelSpec(lpt.findChannelType(registerTypeForChannel2).get(), lpSpec).overflow(new BigDecimal(1000000L)).add();
        slave1DeviceConfiguration.createChannelSpec(lpt.findChannelType(registerTypeForChannel3).get(), lpSpec).overflow(new BigDecimal(1000000L)).add();

        deviceMessageIds.stream().forEach(slave1DeviceConfiguration::createDeviceMessageEnablement);
        slave1DeviceConfiguration.activate();

    }

    private void setUpForSlave2Device() {
        ReadingType readingTypeForChannel1 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.2.8.0.255"), kiloWattHours);

        RegisterType registerTypeForChannel1 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingTypeForChannel1)
                .orElse(inMemoryPersistence.getMasterDataService().newRegisterType(readingTypeForChannel1, ObisCode.fromString("1.0.1.8.0.255")));
        registerTypeForChannel1.save();

        LoadProfileType lpt = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("15min Electricity Slave2", ObisCode.fromString("1.0.99.1.0.255"), TimeDuration.minutes(15), Collections.singletonList(registerTypeForChannel1));
        lpt.save();

        DeviceType slave2DeviceType = inMemoryPersistence.getDeviceConfigurationService()
                .newDataloggerSlaveDeviceTypeBuilder("slave2DeviceType", inMemoryPersistence.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get())
                .create();
        slave2DeviceType.addRegisterType(registerTypeForChannel1);
        slave2DeviceType.addLoadProfileType(lpt);
        slave2DeviceType.update();

        DeviceType.DeviceConfigurationBuilder slave2DeviceConfigurationBuilder = slave2DeviceType.newConfiguration("Default");
        slave2DeviceConfiguration = slave2DeviceConfigurationBuilder.add();

        LoadProfileSpec.LoadProfileSpecBuilder lpSpecBuilder = slave2DeviceConfiguration.createLoadProfileSpec(lpt);
        LoadProfileSpec lpSpec = lpSpecBuilder.add();

        slave2DeviceConfiguration.createChannelSpec(lpt.findChannelType(registerTypeForChannel1).get(), lpSpec).overflow(new BigDecimal(1000000L)).add();

        deviceMessageIds.stream().forEach(slave2DeviceConfiguration::createDeviceMessageEnablement);
        slave2DeviceConfiguration.activate();
    }

    @Override
    protected Device createDataLoggerDevice(String name) {
        return inMemoryPersistence.getDeviceService().newDevice(dataLoggerConfiguration, name, name + "MrId", Instant.now());
    }

    private Device createFirstSlave(String name) {
        return inMemoryPersistence.getDeviceService().newDevice(slave1DeviceConfiguration, name, name + "MrId", Instant.now());
    }

    private Device createSecondSlave(String name) {
        return inMemoryPersistence.getDeviceService().newDevice(slave2DeviceConfiguration, name, name + "MrId", Instant.now());
    }


    @Test
    @Transactional
    public void testDataLoggerConfiguration() {
        setUpForDataLoggerEnabledDevice();

        Device dataLogger = createDataLoggerDevice("dataLogger");
        assertThat(dataLogger.getLoadProfiles()).hasSize(2);
        assertThat(dataLogger.getChannels()).hasSize(6);

        ReadingType readingType1 = inMemoryPersistence.getReadingTypeUtilService()
                .getIntervalAppliedReadingType(readingTypeForChannel1, Optional.of(TimeDuration.minutes(15)), ObisCode.fromString("1.0.1.8.0.255"))
                .get();
        ReadingType readingType2 = inMemoryPersistence.getReadingTypeUtilService()
                .getIntervalAppliedReadingType(readingTypeForChannel2, Optional.of(TimeDuration.minutes(15)), ObisCode.fromString("1.0.2.8.0.255"))
                .get();
        ReadingType readingType3 = inMemoryPersistence.getReadingTypeUtilService()
                .getIntervalAppliedReadingType(readingTypeForChannel3, Optional.of(TimeDuration.minutes(15)), ObisCode.fromString("1.0.1.8.1.255"))
                .get();
        ReadingType readingType4 = inMemoryPersistence.getReadingTypeUtilService()
                .getIntervalAppliedReadingType(readingTypeForChannel4, Optional.of(TimeDuration.minutes(15)), ObisCode.fromString("1.0.1.8.2.255"))
                .get();
        ReadingType readingType5 = inMemoryPersistence.getReadingTypeUtilService()
                .getIntervalAppliedReadingType(readingTypeForChannel5, Optional.of(TimeDuration.minutes(15)), ObisCode.fromString("1.0.2.8.1.255"))
                .get();
        ReadingType readingType6 = inMemoryPersistence.getReadingTypeUtilService()
                .getIntervalAppliedReadingType(readingTypeForChannel6, Optional.of(TimeDuration.minutes(15)), ObisCode.fromString("1.0.2.8.2.255"))
                .get();

        assertThat(dataLogger.getChannels()
                .stream()
                .map(Channel::getReadingType)
                .collect(Collectors.toList())).containsExactly(readingType1, readingType2, readingType3, readingType4, readingType5, readingType6);
    }

    @Test
    @Transactional
    public void testSlave1Configuration() {
        setUpForDataLoggerEnabledDevice();
        setUpForSlave1Device();

        Device slave1 = inMemoryPersistence.getDeviceService().newDevice(slave1DeviceConfiguration, "slave1", "slave1_MrId", Instant.now());
        assertThat(slave1.getLoadProfiles()).hasSize(1);
        assertThat(slave1.getChannels()).hasSize(3);

        ReadingType readingType1 = inMemoryPersistence.getReadingTypeUtilService()
                .getIntervalAppliedReadingType(readingTypeForChannel1, Optional.of(TimeDuration.minutes(15)), ObisCode.fromString("1.0.1.8.0.255"))
                .get();
        ReadingType readingType2 = inMemoryPersistence.getReadingTypeUtilService()
                .getIntervalAppliedReadingType(readingTypeForChannel3, Optional.of(TimeDuration.minutes(15)), ObisCode.fromString("1.0.1.8.1.255"))
                .get();
        ReadingType readingType3 = inMemoryPersistence.getReadingTypeUtilService()
                .getIntervalAppliedReadingType(readingTypeForChannel4, Optional.of(TimeDuration.minutes(15)), ObisCode.fromString("1.0.1.8.2.255"))
                .get();

        assertThat(slave1.getChannels().stream().map(Channel::getReadingType).collect(Collectors.toList())).containsExactly(readingType1, readingType2, readingType3);
    }

    @Test
    @Transactional
    public void testSlave2Configuration() {
        setUpForDataLoggerEnabledDevice();
        setUpForSlave2Device();

        Device slave2 = inMemoryPersistence.getDeviceService().newDevice(slave2DeviceConfiguration, "slave2", "slave1_MrId", Instant.now());
        assertThat(slave2.getLoadProfiles()).hasSize(1);
        assertThat(slave2.getChannels()).hasSize(1);

        ReadingType readingType1 = inMemoryPersistence.getReadingTypeUtilService()
                .getIntervalAppliedReadingType(readingTypeForChannel2, Optional.of(TimeDuration.minutes(15)), ObisCode.fromString("1.0.2.8.0.255"))
                .get();

        assertThat(slave2.getChannels().stream().map(Channel::getReadingType).collect(Collectors.toList())).containsExactly(readingType1);
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    public void noSlaveChannelsLinkedTest() {
        setUpForDataLoggerEnabledDevice();
        setUpForSlave1Device();

        Device slave1 = createFirstSlave("slave1");
        Device dataLogger = createDataLoggerDevice("dataLogger");

        HashMap<Channel, Channel> channelMapping = new HashMap<>();
        HashMap<Register, Register> registerMapping = new HashMap<>();

        inMemoryPersistence.getTopologyService().setDataLogger(slave1, dataLogger,  Instant.now(), channelMapping, registerMapping);
    }


    @Test(expected = ConstraintViolationException.class)
    @Transactional
    public void notAllSlaveChannelsLinkedTest() {
        setUpForDataLoggerEnabledDevice();
        setUpForSlave1Device();

        Device slave1 = createFirstSlave("slave1");
        Device dataLogger = createDataLoggerDevice("dataLogger");

        HashMap<Channel, Channel> channelMapping = new HashMap<>();
        channelMapping.put(slave1.getChannels().get(0), dataLogger.getChannels().get(0));
        HashMap<Register, Register> registerMapping = new HashMap<>();

        inMemoryPersistence.getTopologyService().setDataLogger(slave1, dataLogger, Instant.now(), channelMapping, registerMapping);
    }

    @Test
    @Transactional
    public void succesfulLinkTest() {
        setUpForDataLoggerEnabledDevice();
        setUpForSlave2Device();

        Device slave2 = createSecondSlave("slave2");
        Device dataLogger = createDataLoggerDevice("dataLogger");

        HashMap<Channel, Channel> channelMapping = new HashMap<>();

        channelMapping.put(slave2.getChannels().get(0), dataLogger.getChannels().get(0));
        HashMap<Register, Register> registerMapping = new HashMap<>();

        inMemoryPersistence.getTopologyService().setDataLogger(slave2, dataLogger, Instant.now(), channelMapping, registerMapping );
    }

    @Test
    @Transactional
    public void deletingSlavesRemovesLinksTest() {
        setUpForDataLoggerEnabledDevice();
        setUpForSlave2Device();

        Device slave2 = createSecondSlave("slave2");
        Device dataLogger = createDataLoggerDevice("dataLogger");

        HashMap<Channel, Channel> channelMapping = new HashMap<>();
        HashMap<Register, Register> registerMapping = new HashMap<>();

        channelMapping.put(slave2.getChannels().get(0), dataLogger.getChannels().get(0));

        inMemoryPersistence.getTopologyService().setDataLogger(slave2, dataLogger, Instant.now(), channelMapping, registerMapping);

        slave2.delete();

        assertThat(inMemoryPersistence.getTopologyService().dataModel().query(DataLoggerChannelUsageImpl.class).select(Condition.TRUE).isEmpty());
        assertThat(inMemoryPersistence.getTopologyService().dataModel().query(DataLoggerReferenceImpl.class).select(Condition.TRUE).isEmpty());
    }

    @Test
    @Transactional
    public void deletingDataLoggerRemovesLinksTest() {
        setUpForDataLoggerEnabledDevice();
        setUpForSlave2Device();

        Device slave2 = createSecondSlave("slave2");
        Device dataLogger = createDataLoggerDevice("dataLogger");

        HashMap<Channel, Channel> channelMapping = new HashMap<>();
        channelMapping.put(slave2.getChannels().get(0), dataLogger.getChannels().get(0));
        HashMap<Register, Register> registerMapping = new HashMap<>();

        inMemoryPersistence.getTopologyService().setDataLogger(slave2, dataLogger, Instant.now(), channelMapping, registerMapping);

        dataLogger.delete();

        assertThat(inMemoryPersistence.getTopologyService().dataModel().query(DataLoggerChannelUsageImpl.class).select(Condition.TRUE).isEmpty());
        assertThat(inMemoryPersistence.getTopologyService().dataModel().query(DataLoggerReferenceImpl.class).select(Condition.TRUE).isEmpty());
    }

    @Test
    @Transactional
    public void multipleSuccesfulLinkTest() {
        setUpForDataLoggerEnabledDevice();
        setUpForSlave1Device();
        setUpForSlave2Device();

        Device slave1 = createFirstSlave("slave1");
        Device slave2 = createSecondSlave("slave2");
        Device dataLogger = createDataLoggerDevice("dataLogger");

        HashMap<Channel, Channel> channelMapping1 = new HashMap<>();
        channelMapping1.put(slave1.getChannels().get(0), dataLogger.getChannels().get(0));
        channelMapping1.put(slave1.getChannels().get(1), dataLogger.getChannels().get(1));
        channelMapping1.put(slave1.getChannels().get(2), dataLogger.getChannels().get(2));
        HashMap<Register, Register> registerMapping1 = new HashMap<>();
        inMemoryPersistence.getTopologyService().setDataLogger(slave1, dataLogger, Instant.now(), channelMapping1, registerMapping1);

        assertThat(inMemoryPersistence.getTopologyService().isReferenced(dataLogger.getChannels().get(0))).isTrue();

        HashMap<Channel, Channel> channelMapping2 = new HashMap<>();
        channelMapping2.put(slave2.getChannels().get(0), dataLogger.getChannels().get(4));
        HashMap<Register, Register> registerMapping2 = new HashMap<>();
        inMemoryPersistence.getTopologyService().setDataLogger(slave2, dataLogger, Instant.now(), channelMapping2, registerMapping2);

        slave1.delete();
        slave2.delete();

        assertThat(inMemoryPersistence.getTopologyService().dataModel().query(DataLoggerChannelUsageImpl.class).select(Condition.TRUE).isEmpty());
        assertThat(inMemoryPersistence.getTopologyService().dataModel().query(DataLoggerReferenceImpl.class).select(Condition.TRUE).isEmpty());
    }

    @Test
    @Transactional
    public void findDataLoggerSlavesTest() {
        setUpForDataLoggerEnabledDevice();
        setUpForSlave2Device();

        Device slave2 = createSecondSlave("slave2");
        Device dataLogger = createDataLoggerDevice("dataLogger");

        HashMap<Channel, Channel> channelMapping = new HashMap<>();
        channelMapping.put(slave2.getChannels().get(0), dataLogger.getChannels().get(0));
        HashMap<Register, Register> registerMapping = new HashMap<>();

        inMemoryPersistence.getTopologyService().setDataLogger(slave2, dataLogger, Instant.now(), channelMapping, registerMapping);
        List<Device> slaves = inMemoryPersistence.getTopologyService().findDataLoggerSlaves(dataLogger);

        assertThat(slaves.contains(slave2));
    }

    @Test
    @Transactional
    public void isDataLoggerReferencedTest() {
        setUpForDataLoggerEnabledDevice();
        setUpForSlave2Device();

        Device slave2 = createSecondSlave("slave2");
        Device dataLogger = createDataLoggerDevice("dataLogger");

        HashMap<Channel, Channel> channelMapping = new HashMap<>();
        channelMapping.put(slave2.getChannels().get(0), dataLogger.getChannels().get(0));
        HashMap<Register, Register> registerMapping = new HashMap<>();

        inMemoryPersistence.getTopologyService().setDataLogger(slave2, dataLogger, Instant.now(), channelMapping, registerMapping);

        assertThat(inMemoryPersistence.getTopologyService().isReferenced(dataLogger.getChannels().get(0))).isTrue();
    }

    @Test
    @Transactional
    public void getSlaveChannelTestForLinkedDataLoggerChannel(){
        setUpForDataLoggerEnabledDevice();
        setUpForSlave2Device();

        Device slave2 = createSecondSlave("slave2");
        Device dataLogger = createDataLoggerDevice("dataLogger");

        HashMap<Channel, Channel> channelMapping = new HashMap<>();
        channelMapping.put(slave2.getChannels().get(0), dataLogger.getChannels().get(0));
        HashMap<Register, Register> registerMapping = new HashMap<>();
        inMemoryPersistence.getTopologyService().setDataLogger(slave2, dataLogger, Instant.now(), channelMapping, registerMapping);

        assertThat(inMemoryPersistence.getTopologyService().getSlaveChannel(dataLogger.getChannels().get(0),  Instant.now())).isPresent();
    }

    @Test
    @Transactional
    public void getSlaveChannelTestForNotLinkedDataLoggerChannel(){
        setUpForDataLoggerEnabledDevice();
        setUpForSlave2Device();

        Device slave2 = createSecondSlave("slave2");
        Device dataLogger = createDataLoggerDevice("dataLogger");

        HashMap<Channel, Channel> channelMapping = new HashMap<>();
        channelMapping.put(slave2.getChannels().get(0), dataLogger.getChannels().get(0));
        HashMap<Register, Register> registerMapping = new HashMap<>();
        inMemoryPersistence.getTopologyService().setDataLogger(slave2, dataLogger,  Instant.now(), channelMapping, registerMapping);

        assertThat(inMemoryPersistence.getTopologyService().getSlaveChannel(dataLogger.getChannels().get(1),  Instant.now())).isEmpty();
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    public void dataLoggerChannelUsedTwiceTest() {
        setUpForDataLoggerEnabledDevice();
        setUpForSlave1Device();

        Device slave1 = createFirstSlave("slave1");
        Device dataLogger = createDataLoggerDevice("dataLogger");

        HashMap<Channel, Channel> channelMapping1 = new HashMap<>();
        channelMapping1.put(slave1.getChannels().get(0), dataLogger.getChannels().get(0));
        channelMapping1.put(slave1.getChannels().get(1), dataLogger.getChannels().get(1));
        channelMapping1.put(slave1.getChannels().get(2), dataLogger.getChannels().get(0));
        HashMap<Register, Register> registerMapping1 = new HashMap<>();
        inMemoryPersistence.getTopologyService().setDataLogger(slave1, dataLogger, Instant.now(), channelMapping1, registerMapping1);
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    public void dataLoggerChannelAlreadyReferencedTest() {
        setUpForDataLoggerEnabledDevice();
        setUpForSlave1Device();
        setUpForSlave2Device();

        Device slave1 = createFirstSlave("slave1");
        Device slave2 = createSecondSlave("slave2");
        Device dataLogger = createDataLoggerDevice("dataLogger");

        HashMap<Channel, Channel> channelMapping1 = new HashMap<>();
        channelMapping1.put(slave1.getChannels().get(0), dataLogger.getChannels().get(0));
        channelMapping1.put(slave1.getChannels().get(1), dataLogger.getChannels().get(1));
        channelMapping1.put(slave1.getChannels().get(2), dataLogger.getChannels().get(2));
        HashMap<Register, Register> registerMapping1 = new HashMap<>();
        inMemoryPersistence.getTopologyService().setDataLogger(slave1, dataLogger, Instant.now(), channelMapping1, registerMapping1);

        assertThat(inMemoryPersistence.getTopologyService().isReferenced(dataLogger.getChannels().get(0))).isTrue();

        HashMap<Channel, Channel> channelMapping2 = new HashMap<>();
        channelMapping2.put(slave2.getChannels().get(0), dataLogger.getChannels().get(0));
        HashMap<Register, Register> registerMapping2 = new HashMap<>();
        inMemoryPersistence.getTopologyService().setDataLogger(slave2, dataLogger,  Instant.now(), channelMapping2, registerMapping2);
    }

    @Test
    @Transactional
    public void clearDataLoggerTest(){
        Instant linkingDate = Instant.now();
        when(clock.instant()).thenReturn(linkingDate);

        setUpForDataLoggerEnabledDevice();
        setUpForSlave1Device();
        setUpForSlave2Device();

        Device slave1 = createFirstSlave("slave1");
        Device slave2 = createSecondSlave("slave2");
        Device dataLogger = createDataLoggerDevice("dataLogger");

        HashMap<Channel, Channel> channelMapping1 = new HashMap<>();
        channelMapping1.put(slave1.getChannels().get(0), dataLogger.getChannels().get(0));
        channelMapping1.put(slave1.getChannels().get(1), dataLogger.getChannels().get(1));
        channelMapping1.put(slave1.getChannels().get(2), dataLogger.getChannels().get(2));
        HashMap<Register, Register> registerMapping1 = new HashMap<>();
        inMemoryPersistence.getTopologyService().setDataLogger(slave1, dataLogger, linkingDate, channelMapping1, registerMapping1);

        HashMap<Channel, Channel> channelMapping2 = new HashMap<>();
        channelMapping2.put(slave2.getChannels().get(0), dataLogger.getChannels().get(4));
        HashMap<Register, Register> registerMapping2 = new HashMap<>();
        inMemoryPersistence.getTopologyService().setDataLogger(slave2, dataLogger, linkingDate, channelMapping2, registerMapping2);

        assertThat(inMemoryPersistence.getTopologyService().isReferenced(dataLogger.getChannels().get(0))).isTrue();
        assertThat(inMemoryPersistence.getTopologyService().isReferenced(dataLogger.getChannels().get(4))).isTrue();

        Instant unlinkDate = linkingDate.plus(1, ChronoUnit.HOURS);
        when(clock.instant()).thenReturn(unlinkDate);
        inMemoryPersistence.getTopologyService().clearDataLogger(slave1, unlinkDate);

        assertThat(inMemoryPersistence.getTopologyService().isReferenced(dataLogger.getChannels().get(0))).isFalse();
        assertThat(inMemoryPersistence.getTopologyService().isReferenced(dataLogger.getChannels().get(4))).isTrue();

        assertThat(inMemoryPersistence.getTopologyService().dataModel().query(DataLoggerChannelUsageImpl.class).select(Condition.TRUE)).hasSize(4);
        assertThat(inMemoryPersistence.getTopologyService().dataModel().query(DataLoggerReferenceImpl.class).select(Condition.TRUE)).hasSize(2);

        inMemoryPersistence.getTopologyService().clearDataLogger(slave2, unlinkDate);
        assertThat(inMemoryPersistence.getTopologyService().isReferenced(dataLogger.getChannels().get(4))).isFalse();
    }
}
