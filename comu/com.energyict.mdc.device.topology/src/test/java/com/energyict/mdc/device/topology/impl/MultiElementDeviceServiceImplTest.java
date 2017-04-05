/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceTypePurpose;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.topology.impl.multielement.MultiElementDeviceLinkException;
import com.energyict.mdc.device.topology.impl.multielement.MultiElementDeviceReferenceImpl;
import com.energyict.mdc.device.topology.multielement.MultiElementDeviceService;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.device.BaseDevice;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link TopologyServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (10:00)
 */
public class MultiElementDeviceServiceImplTest extends PersistenceIntegrationTest {

    private static final String MULTI_ELEMENT_ENABLED_DEVICE_TYPE_NAME = "Multi-element enabled DeviceType";
    private static final String MULTI_ELEMENT_ENABLED_DEVICE_CONFIGURATION_NAME = "Multi-element enabled DeviceConfig";
    private static final String MULTI_ELEMENT_SLAVE_DEVICE_TYPE = "Multi-element slave DeviceType";

    private DeviceType multiElementEnabledDeviceType;
    private DeviceType multiElementSlaveDeviceType;
    protected DeviceConfiguration multiElementEnabledDeviceConfiguration;
    protected DeviceConfiguration multiElementSlaveDeviceConfiguration;

    @Before
    public void initializeMocks() {
        super.initializeMocks();
        multiElementEnabledDeviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(MULTI_ELEMENT_ENABLED_DEVICE_TYPE_NAME, deviceProtocolPluggableClass);

        multiElementSlaveDeviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(MULTI_ELEMENT_SLAVE_DEVICE_TYPE, deviceProtocolPluggableClass);
        multiElementSlaveDeviceType.setDeviceTypePurpose(DeviceTypePurpose.MULTI_ELEMENT_SLAVE);
        multiElementSlaveDeviceType.update();

        DeviceType.DeviceConfigurationBuilder multiElementEnabledDeviceConfigurationBuilder = multiElementEnabledDeviceType.newConfiguration(MULTI_ELEMENT_ENABLED_DEVICE_CONFIGURATION_NAME);
        multiElementEnabledDeviceConfigurationBuilder.isDirectlyAddressable(true);
        multiElementEnabledDeviceConfigurationBuilder.multiElementEnabled(true);

        multiElementEnabledDeviceConfiguration = multiElementEnabledDeviceConfigurationBuilder.add();
        deviceMessageIds.stream().forEach(multiElementEnabledDeviceConfiguration::createDeviceMessageEnablement);
        ReadingType activeEnergy = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.1.8.0.255"), Unit.get("kWh"));
        RegisterType registerType1 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(activeEnergy).get();
        ReadingType reactiveEnergy = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeFrom(ObisCode.fromString("1.0.2.8.0.255"), Unit.get("kWh"));
        RegisterType registerType2 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(reactiveEnergy).get();
        multiElementEnabledDeviceType.addRegisterType(registerType1);
        multiElementEnabledDeviceType.addRegisterType(registerType2);
        multiElementEnabledDeviceConfiguration.createNumericalRegisterSpec(registerType1).overflowValue(BigDecimal.valueOf(1000L)).numberOfFractionDigits(0).add();
        multiElementEnabledDeviceConfiguration.createNumericalRegisterSpec(registerType2).overflowValue(BigDecimal.valueOf(1000L)).numberOfFractionDigits(0).add();
        multiElementEnabledDeviceConfiguration.activate();

        DeviceType.DeviceConfigurationBuilder multiElementSlaveDeviceConfigurationBuilder = multiElementSlaveDeviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        multiElementSlaveDeviceConfigurationBuilder.isDirectlyAddressable(true);
        multiElementSlaveDeviceConfiguration = multiElementSlaveDeviceConfigurationBuilder.add();
        multiElementSlaveDeviceType.addRegisterType(registerType1);
        deviceMessageIds.stream().forEach(multiElementSlaveDeviceConfiguration::createDeviceMessageEnablement);
        multiElementSlaveDeviceConfiguration.createNumericalRegisterSpec(registerType1).overflowValue(BigDecimal.valueOf(1000L)).numberOfFractionDigits(0).add();
        multiElementSlaveDeviceConfiguration.activate();
    }

    @Test
    @Transactional
    @Expected(MultiElementDeviceLinkException.class)
    public void removeSlaveWhenIsNoSlaveTest() {
        Device origin = createSlaveDevice("Origin");

        // Business method
        this.getMultiElementDeviceService().removeSlave(origin, Instant.now());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(strict=false, messageId = "{" + MessageSeeds.Keys.DEVICE_CANNOT_BE_MULTI_ELEMENT_METER_FOR_ITSELF + "}")
    public void setMultiElementDeviceSameAsOriginDeviceTest() {
        Device slave = createMultiElementDevice("Multi-Element device");

        // Business method
        this.getMultiElementDeviceService().addSlave(slave, slave, Instant.now(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(strict=false, messageId = "{" + MessageSeeds.Keys.NOT_A_MULTI_ELEMENT_SUBMETER_DEVICE + "}")
    public void originNotAMultiElementSlaveDeviceTest() {
        Device slave = createSimpleDeviceWithName("Not a multi-element slave");
        Device multiElementDevice = createMultiElementDevice("Multi-element enabled");
        // Business method
        this.getMultiElementDeviceService().addSlave(slave, multiElementDevice, Instant.now(), Collections.emptyMap(), Collections.emptyMap());
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(strict=false, messageId = "{" + MessageSeeds.Keys.GATEWAY_NOT_MULTI_ELEMENT_ENABLED + "}")
    public void setNotMultiElementDeviceAsMasterTest() {
        Device slave = createSlaveDevice("Slave");
        Device multiElementDevice = createSimpleDeviceWithName("Multi-element");
        // Business method
        this.getMultiElementDeviceService().addSlave(slave, multiElementDevice, Instant.now(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Test
    @Transactional
    public void addSlaveTest() {
        Device slave = createSlaveDevice("Slave2");
        Device multiElementDevice = createMultiElementDevice("Multi-element enabled");

        Instant now = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(now);

        // Business method
        Map<Register, Register> slaveDataLoggerRegisterMap = new HashMap<>();
        slaveDataLoggerRegisterMap.put(slave.getRegisters().get(0), multiElementDevice.getRegisters().get(0));
        this.getMultiElementDeviceService().addSlave(slave, multiElementDevice, now, Collections.emptyMap(), slaveDataLoggerRegisterMap);

        List<MultiElementDeviceReferenceImpl> gatewayReferences = inMemoryPersistence.getTopologyDataModel().query(MultiElementDeviceReferenceImpl.class).select(com.elster.jupiter.util.conditions.Condition.TRUE);
        assertThat(gatewayReferences).hasSize(1);
        assertThat(gatewayReferences.get(0)).isInstanceOf(MultiElementDeviceReferenceImpl.class);
        MultiElementDeviceReferenceImpl multiElementDeviceReference = gatewayReferences.get(0);
        assertThat(multiElementDeviceReference.getOrigin().getId()).isEqualTo(slave.getId());
        assertThat(multiElementDeviceReference.getGateway().getId()).isEqualTo(multiElementDevice.getId());
        assertThat(multiElementDeviceReference.getRange().lowerEndpoint()).isEqualTo(now);
        assertThat(multiElementDeviceReference.getDataLoggerChannelUsages()).hasSize(1);
    }

    @Test
    @Transactional
    public void findMultiElementSlavesOnMultiElementDeviceTest() {
        Device multiElementDevice = createMultiElementDevice("Multi-elementDevice");
        Device slave1 = createSlaveDevice("Slave1");
        Device slave2 = createSlaveDevice("Slave2");

        Instant now = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(now);

        // Business method
        Map<Register, Register> slaveDataLoggerRegisterMap1 = new HashMap<>();
        slaveDataLoggerRegisterMap1.put(slave1.getRegisters().get(0), multiElementDevice.getRegisters().get(0));
        Map<Register, Register> slaveDataLoggerRegisterMap2 = new HashMap<>();
        slaveDataLoggerRegisterMap2.put(slave2.getRegisters().get(0), multiElementDevice.getRegisters().get(1));
        this.getMultiElementDeviceService().addSlave(slave1, multiElementDevice, clock.instant(), Collections.emptyMap(), slaveDataLoggerRegisterMap1);
        this.getMultiElementDeviceService().addSlave(slave2, multiElementDevice, clock.instant(), Collections.emptyMap(), slaveDataLoggerRegisterMap2);

        // Business method
        List<Device> slaves = this.getMultiElementDeviceService().findMultiElementSlaves(multiElementDevice);

        // Asserts
        assertThat(slaves).hasSize(2);
        assertThat(slaves).has(new Condition<List<? extends Device>>() {
            @Override
            public boolean matches(List<? extends Device> value) {
                boolean bothMatch = true;
                for (BaseDevice baseDevice : value) {
                    bothMatch &= ((baseDevice.getId() == slave1.getId()) || (baseDevice.getId() == slave2.getId()));
                }
                return bothMatch;
            }
        });
    }


    @Test
    @Transactional
    public void getMultiElementSlaveRegisterTimeLineWithoutSlavesTest() {
        Device multiElementDevice = createMultiElementDevice("Multi-elementDevice");
        Instant now = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        Instant lower = LocalDateTime.of(2013, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(now);

        Register<?, ?> multiElementDeviceRegister = multiElementDevice.getRegisters().get(0);
        Range<Instant> multiElementDeviceRange = Range.atLeast(lower);
        List<Pair<Register, Range<Instant>>> dataLoggerRegisterTimeLine = getMultiElementDeviceService().getMultiElementSlaveRegisterTimeLine(multiElementDeviceRegister, multiElementDeviceRange);
        assertThat(dataLoggerRegisterTimeLine).hasSize(1);
        assertThat(dataLoggerRegisterTimeLine.get(0).getFirst()).isEqualTo(multiElementDeviceRegister);
        assertThat(dataLoggerRegisterTimeLine.get(0).getLast()).isEqualTo(multiElementDeviceRange);
    }

    @Test
    @Transactional
    public void getMultiElementSlaveRegisterTimeLineWithSingleUnlinkedSlaveTest() {
        Device multiElementDevice = createMultiElementDevice("Multi-elementDevice");
        Device slave1 = createSlaveDevice("Slave1");

        Instant now = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        Instant linkingDate = LocalDateTime.of(2014, 1, 1, 0, 0).toInstant(ZoneOffset.UTC);
        Instant unlinkingDate = LocalDateTime.of(2014, 5, 4, 0, 0).toInstant(ZoneOffset.UTC);
        Instant lower = LocalDateTime.of(2013, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);

        when(clock.instant()).thenReturn(now);

        Register<?, ?> multiElementDeviceRegister = multiElementDevice.getRegisters().get(0);
        // Business method
        getMultiElementDeviceService().addSlave(slave1, multiElementDevice, linkingDate, Collections.emptyMap(), Collections.singletonMap(slave1.getRegisters().get(0), multiElementDeviceRegister));
        getMultiElementDeviceService().removeSlave(slave1, unlinkingDate);

        Range<Instant> multiElementDeviceRange = Range.atLeast(lower);
        List<Pair<Register, Range<Instant>>> dataLoggerRegisterTimeLine = getMultiElementDeviceService().getMultiElementSlaveRegisterTimeLine(multiElementDeviceRegister, multiElementDeviceRange);
        assertThat(dataLoggerRegisterTimeLine).hasSize(3);
        assertThat(dataLoggerRegisterTimeLine.get(0).getFirst()).isEqualTo(multiElementDeviceRegister);
        assertThat(dataLoggerRegisterTimeLine.get(0).getLast()).isEqualTo(Range.atLeast(unlinkingDate));
        assertThat(dataLoggerRegisterTimeLine.get(1).getFirst().getDevice().getmRID()).isEqualTo(slave1.getmRID());
        assertThat(dataLoggerRegisterTimeLine.get(1).getFirst().getRegisterSpecId()).isEqualTo(slave1.getRegisters().get(0).getRegisterSpecId());
        assertThat(dataLoggerRegisterTimeLine.get(1).getLast()).isEqualTo(Range.openClosed(linkingDate, unlinkingDate));
        assertThat(dataLoggerRegisterTimeLine.get(2).getFirst()).isEqualTo(multiElementDeviceRegister);
        assertThat(dataLoggerRegisterTimeLine.get(2).getLast()).isEqualTo(Range.openClosed(lower, linkingDate));
    }

    @Test
    @Transactional
    public void getDataLoggerRegisterTimeLineWithSingleLinkedSlaveTest() {
        Instant now = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        Instant linkingDate = LocalDateTime.of(2014, 1, 1, 0, 0).toInstant(ZoneOffset.UTC);
        Instant lower = LocalDateTime.of(2013, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);

        Device multiElementDevice = createMultiElementDevice("Multi-elementDevice");
        Device slave1 = createSlaveDevice("Slave1");
        when(clock.instant()).thenReturn(now);

        Register<?, ?> multiElementDeviceRegister = multiElementDevice.getRegisters().get(0);
        // Business method
        getMultiElementDeviceService().addSlave(slave1, multiElementDevice, linkingDate, Collections.emptyMap(), Collections.singletonMap(slave1.getRegisters().get(0), multiElementDeviceRegister));

        Range<Instant> dataLoggerRange = Range.atLeast(lower);
        List<Pair<Register, Range<Instant>>> dataLoggerRegisterTimeLine = getMultiElementDeviceService().getMultiElementSlaveRegisterTimeLine(multiElementDeviceRegister, dataLoggerRange);
        assertThat(dataLoggerRegisterTimeLine).hasSize(2);
        assertThat(dataLoggerRegisterTimeLine.get(0).getFirst().getDevice().getmRID()).isEqualTo(slave1.getmRID());
        assertThat(dataLoggerRegisterTimeLine.get(0).getFirst().getRegisterSpecId()).isEqualTo(slave1.getRegisters().get(0).getRegisterSpecId());
        assertThat(dataLoggerRegisterTimeLine.get(0).getLast()).isEqualTo(Range.atLeast(linkingDate));
        assertThat(dataLoggerRegisterTimeLine.get(1).getFirst()).isEqualTo(multiElementDeviceRegister);
        assertThat(dataLoggerRegisterTimeLine.get(1).getLast()).isEqualTo(Range.openClosed(lower, linkingDate));
    }

    @Test
    @Transactional
    public void getDataLoggerRegisterTimeLineWithMultipleSlavesTest() {
        Instant now = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        Instant linkDate1 = LocalDateTime.of(2014, 1, 1, 0, 0).toInstant(ZoneOffset.UTC);
        Instant unLinkDate1 = LocalDateTime.of(2014, 1, 15, 0, 0).toInstant(ZoneOffset.UTC);
        Instant linkDate2 = LocalDateTime.of(2014, 2, 3, 0, 0).toInstant(ZoneOffset.UTC);
        Instant unLinkDate2 = LocalDateTime.of(2014, 2, 5, 0, 0).toInstant(ZoneOffset.UTC);
        Instant lower = LocalDateTime.of(2013, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);

        Device multiElementDevice = createMultiElementDevice("Multi-elementDevice");
        Device slave1 = createSlaveDevice("Slave1");
        Device slave2 = createSlaveDevice("Slave2");
        when(clock.instant()).thenReturn(now);

        Register<?, ?> multiElementDeviceRegister = multiElementDevice.getRegisters().get(0);
        // Business method
        getMultiElementDeviceService().addSlave(slave1, multiElementDevice, linkDate1, Collections.emptyMap(), Collections.singletonMap(slave1.getRegisters().get(0), multiElementDeviceRegister));
        getMultiElementDeviceService().removeSlave(slave1, unLinkDate1);
        getMultiElementDeviceService().addSlave(slave2, multiElementDevice, linkDate2, Collections.emptyMap(), Collections.singletonMap(slave2.getRegisters().get(0), multiElementDeviceRegister));
        getMultiElementDeviceService().removeSlave(slave2, unLinkDate2);

        Range<Instant> multiElementDeviceRange = Range.closedOpen(lower, now);
        List<Pair<Register, Range<Instant>>> dataLoggerRegisterTimeLine = getMultiElementDeviceService().getMultiElementSlaveRegisterTimeLine(multiElementDeviceRegister, multiElementDeviceRange);
        assertThat(dataLoggerRegisterTimeLine).hasSize(5);
        assertThat(dataLoggerRegisterTimeLine.get(0).getFirst()).isEqualTo(multiElementDeviceRegister);
        assertThat(dataLoggerRegisterTimeLine.get(0).getLast()).isEqualTo(Range.openClosed(unLinkDate2, now));
        assertThat(dataLoggerRegisterTimeLine.get(1).getFirst().getDevice().getmRID()).isEqualTo(slave2.getmRID());
        assertThat(dataLoggerRegisterTimeLine.get(1).getFirst().getRegisterSpecId()).isEqualTo(slave2.getRegisters().get(0).getRegisterSpecId());
        assertThat(dataLoggerRegisterTimeLine.get(1).getLast()).isEqualTo(Range.openClosed(linkDate2, unLinkDate2));
        assertThat(dataLoggerRegisterTimeLine.get(2).getFirst()).isEqualTo(multiElementDeviceRegister);
        assertThat(dataLoggerRegisterTimeLine.get(2).getLast()).isEqualTo(Range.openClosed(unLinkDate1, linkDate2));
        assertThat(dataLoggerRegisterTimeLine.get(3).getFirst().getDevice().getmRID()).isEqualTo(slave1.getmRID());
        assertThat(dataLoggerRegisterTimeLine.get(3).getFirst().getRegisterSpecId()).isEqualTo(slave1.getRegisters().get(0).getRegisterSpecId());
        assertThat(dataLoggerRegisterTimeLine.get(3).getLast()).isEqualTo(Range.openClosed(linkDate1, unLinkDate1));
        assertThat(dataLoggerRegisterTimeLine.get(4).getFirst()).isEqualTo(multiElementDeviceRegister);
        assertThat(dataLoggerRegisterTimeLine.get(4).getLast()).isEqualTo(Range.openClosed(lower, linkDate1));
    }

    private MultiElementDeviceService getMultiElementDeviceService() {
        return inMemoryPersistence.getMultiElementDeviceService();
    }

    private Device createMultiElementDevice(String name){
        return inMemoryPersistence.getDeviceService().newDevice(multiElementEnabledDeviceConfiguration, name, name + "MrId", clock.instant());
    }

    protected Device createSlaveDevice(String name){
        return inMemoryPersistence.getDeviceService().newDevice(multiElementSlaveDeviceConfiguration, name, name + "MrId", clock.instant());
    }

}