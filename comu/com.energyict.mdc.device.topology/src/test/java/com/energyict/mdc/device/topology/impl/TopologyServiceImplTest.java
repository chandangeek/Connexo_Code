/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.topology.G3CommunicationPath;
import com.energyict.mdc.device.topology.G3CommunicationPathSegment;
import com.energyict.mdc.device.topology.G3DeviceAddressInformation;
import com.energyict.mdc.device.topology.G3Neighbor;
import com.energyict.mdc.device.topology.Modulation;
import com.energyict.mdc.device.topology.ModulationScheme;
import com.energyict.mdc.device.topology.PhaseInfo;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.TopologyTimeline;
import com.energyict.mdc.device.topology.TopologyTimeslice;
import com.energyict.mdc.protocol.api.device.BaseDevice;

import com.google.common.collect.Range;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.Condition;
import org.junit.Ignore;
import org.junit.Test;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link TopologyServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (10:00)
 */
public class TopologyServiceImplTest extends PersistenceIntegrationTest {

    @Test
    @Transactional
    public void clearPhysicalGatewayWhenThereIsNoGatewayTest() {
        Device origin = createSimpleDeviceWithName("Origin");

        // Business method
        this.getTopologyService().clearPhysicalGateway(origin);

        // Asserts: no exception should be thrown
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_CANNOT_BE_PHYSICAL_GATEWAY_FOR_ITSELF + "}")
    public void setPhysicalGatewaySameAsOriginDeviceTest() {
        Device origin = createSimpleDeviceWithName("Origin");

        // Business method
        this.getTopologyService().setPhysicalGateway(origin, origin);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_CANNOT_BE_PHYSICAL_GATEWAY_FOR_ITSELF + "}")
    public void updatePhysicalGatewayWithSameAsOriginDeviceTest() {
        Device physicalGateway = createSimpleDeviceWithName("PhysicalGateway");
        Device device = getDeviceService().newDevice(deviceConfiguration, "Slave", "SlaveMrid", Instant.now());
        device.save();
        getTopologyService().setPhysicalGateway(device, physicalGateway);
        Device reloadedDevice = getReloadedDevice(device);

        // Business method
        getTopologyService().setPhysicalGateway(reloadedDevice, reloadedDevice);
    }

    @Test
    @Transactional
    public void createWithPhysicalGatewayTest() {
        Device masterDevice = createSimpleDeviceWithName("Physical_MASTER");

        Device device = getDeviceService().newDevice(deviceConfiguration, "Slave", MRID, Instant.now());
        device.save();

        // Business method
        getTopologyService().setPhysicalGateway(device, masterDevice);

        // Asserts
        Device reloadedDevice = getReloadedDevice(device);
        Optional<Device> reloadedPhysicalGateway = getTopologyService().getPhysicalGateway(reloadedDevice);
        assertThat(reloadedPhysicalGateway.isPresent()).isTrue();
        assertThat(reloadedPhysicalGateway.get().getId()).isEqualTo(masterDevice.getId());
    }

    @Test
    @Transactional
    public void updateMultipleSlavesWithSameMasterTest() {
        Device masterDevice = createSimpleDeviceWithName("Physical_MASTER", "m");
        Device slaveDevice1 = createSimpleDeviceWithName("SLAVE_1", "s1");
        Device slaveDevice2 = createSimpleDeviceWithName("SLAVE_2", "s2");

        // Business methods
        getTopologyService().setPhysicalGateway(slaveDevice1, masterDevice);
        getTopologyService().setPhysicalGateway(slaveDevice2, masterDevice);

        // Asserts
        Device reloadedSlave1 = getReloadedDevice(slaveDevice1);
        Device reloadedSlave2 = getReloadedDevice(slaveDevice2);
        Optional<Device> reloadedMaster1 = getTopologyService().getPhysicalGateway(reloadedSlave1);
        Optional<Device> reloadedMaster2 = getTopologyService().getPhysicalGateway(reloadedSlave2);
        assertThat(reloadedMaster1.isPresent()).isTrue();
        assertThat(reloadedMaster2.isPresent()).isTrue();
        assertThat(reloadedMaster1.get().getId()).isEqualTo(reloadedMaster2.get().getId()).isEqualTo(masterDevice.getId());
    }

    @Test
    @Transactional
    public void switchToAnotherMaster() {
        Device masterDevice1 = createSimpleDeviceWithName("Physical_MASTER_1", "m1");
        Device masterDevice2 = createSimpleDeviceWithName("Physical_MASTER_2", "m2");
        Device slave = createSimpleDeviceWithName("Origin", "o");
        getTopologyService().setPhysicalGateway(slave, masterDevice1);

        // Business method
        getTopologyService().setPhysicalGateway(slave, masterDevice2);

        // Asserts
        Device reloadedSlave = getReloadedDevice(slave);
        Optional<Device> reloadedPhysicalGateway = getTopologyService().getPhysicalGateway(reloadedSlave);
        assertThat(reloadedPhysicalGateway.isPresent()).isTrue();
        assertThat(reloadedPhysicalGateway.get().getId()).isEqualTo(masterDevice2.getId());
    }

    @Test
    @Transactional
    public void clearPhysicalGatewayTest() {
        Device masterDevice = createSimpleDeviceWithName("Physical_MASTER", "m");
        Device slave = createSimpleDeviceWithName("SLAVE_1", "s");
        getTopologyService().setPhysicalGateway(slave, masterDevice);

        // Business method
        getTopologyService().clearPhysicalGateway(slave);

        // Asserts
        Device reloadedSlave = getReloadedDevice(slave);
        Optional<Device> reloadedPhysicalGateway = getTopologyService().getPhysicalGateway(reloadedSlave);
        assertThat(reloadedPhysicalGateway).isEmpty();
    }

    @Test
    @Transactional
    public void getDefaultPhysicalGatewayNullTest() {
        Device simpleDevice = createSimpleDevice();

        // Business method
        Optional<Device> gateway = this.getTopologyService().getPhysicalGateway(simpleDevice);

        // Asserts
        assertThat(gateway).isEmpty();
    }

    @Test
    @Transactional
    public void findDownstreamDevicesWhenNoneArePresentTest() {
        Device device = createSimpleDevice();

        // Business method
        List<Device> physicalConnectedDevices = this.getTopologyService().findPhysicalConnectedDevices(device);

        // Asserts
        assertThat(physicalConnectedDevices).isEmpty();
    }

    @Test
    @Transactional
    public void findPhysicalConnectedDevicesTest() {
        Device physicalMaster = createSimpleDeviceWithName("PhysicalMaster");
        Device device1 = this.getDeviceService().newDevice(deviceConfiguration, "Origin1", MRID, Instant.now());
        device1.save();
        this.getTopologyService().setPhysicalGateway(device1, physicalMaster);
        Device device2 = this.getDeviceService().newDevice(deviceConfiguration, "Origin2", MRID+"2", Instant.now());
        device2.save();
        this.getTopologyService().setPhysicalGateway(device2, physicalMaster);

        // Business method
        List<Device> downstreamDevices = this.getTopologyService().findPhysicalConnectedDevices(physicalMaster);

        // Asserts
        assertThat(downstreamDevices).hasSize(2);
        assertThat(downstreamDevices).has(new Condition<List<? extends Device>>() {
            @Override
            public boolean matches(List<? extends Device> value) {
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
    public void findDownstreamDevicesAfterRemovalOfOneTest() {
        Device physicalMaster = createSimpleDeviceWithName("PhysicalMaster");
        Device device1 = this.getDeviceService().newDevice(deviceConfiguration, "Origin1", "1", Instant.now());
        device1.save();
        this.getTopologyService().setPhysicalGateway(device1, physicalMaster);
        Device device2 = this.getDeviceService().newDevice(deviceConfiguration, "Origin2", "2", Instant.now());
        device2.save();
        this.getTopologyService().setPhysicalGateway(device2, physicalMaster);

        //business method
        device1.delete();

        // Asserts
        List<Device> downstreamDevices = this.getTopologyService().findPhysicalConnectedDevices(physicalMaster);
        assertThat(downstreamDevices).hasSize(1);
        assertThat(downstreamDevices.get(0).getId()).isEqualTo(device2.getId());
    }

    @Test
    @Transactional
    public void findDownstreamDevicesAfterRemovingGatewayReferenceTest() {
        Device physicalMaster = createSimpleDeviceWithName("PhysicalMaster");
        Device device1 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "Origin1", "1", Instant.now());
        device1.save();
        this.getTopologyService().setPhysicalGateway(device1, physicalMaster);
        Device device2 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "Origin2", "2", Instant.now());
        device2.save();
        this.getTopologyService().setPhysicalGateway(device2, physicalMaster);

        // Business method
        this.getTopologyService().clearPhysicalGateway(device1);

        // Asserts
        List<Device> downstreamDevices = this.getTopologyService().findPhysicalConnectedDevices(physicalMaster);
        assertThat(downstreamDevices).hasSize(1);
        assertThat(downstreamDevices.get(0).getId()).isEqualTo(device2.getId());
    }

    @Test
    @Transactional
    public void findDownstreamDevicesAfterSettingToOtherPhysicalGatewayTest() {
        Device physicalMaster = createSimpleDeviceWithName("PhysicalMaster","pm");
        Device otherPhysicalMaster = createSimpleDeviceWithName("OtherPhysicalMaster", "opm");
        Device device1 = this.getDeviceService().newDevice(deviceConfiguration, "Origin1", "1", Instant.now());
        device1.save();
        this.getTopologyService().setPhysicalGateway(device1, physicalMaster);
        Device device2 = this.getDeviceService().newDevice(deviceConfiguration, "Origin2", "2", Instant.now());
        device2.save();
        this.getTopologyService().setPhysicalGateway(device2, physicalMaster);

        //business method
        this.getTopologyService().setPhysicalGateway(device1, otherPhysicalMaster);
        List<Device> downstreamDevices = this.getTopologyService().findPhysicalConnectedDevices(physicalMaster);

        // Asserts
        assertThat(downstreamDevices).hasSize(1);
        assertThat(downstreamDevices.get(0).getId()).isEqualTo(device2.getId());
    }

    @Test
    @Transactional
    @Ignore // H2 can't handle the SQL queries
    public void testGetSortedPhysicalGatewayReferences() {
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device gateway = deviceService.newDevice(deviceConfiguration, "gateway", "physGateway", Instant.now());
        gateway.save();

        Device slave1 = deviceService.newDevice(deviceConfiguration, "slave1", "slave1", Instant.now());
        slave1.save();
        topologyService.setPhysicalGateway(slave1, gateway);

        Device slave2 = deviceService.newDevice(deviceConfiguration, "slave2", "slave2", Instant.now());
        slave2.save();
        topologyService.setPhysicalGateway(slave2, gateway);

        Device slave3 = deviceService.newDevice(deviceConfiguration, "slave3", "slave3", Instant.now());
        slave3.save();
        topologyService.setPhysicalGateway(slave3, gateway);

        Device slave4 = deviceService.newDevice(deviceConfiguration, "slave4", "slave4", Instant.now());
        slave4.save();
        topologyService.setPhysicalGateway(slave4, gateway);

        Device slave5 = deviceService.newDevice(deviceConfiguration, "slave5", "slave5", Instant.now());
        slave5.save();
        topologyService.setPhysicalGateway(slave5, gateway);

        TopologyTimeline timeline = inMemoryPersistence.getTopologyService().getPhysicalTopologyTimelineAdditions(gateway, 3);
        List<TopologyTimeslice> timeslices = timeline.getSlices();
        assertThat(timeslices).hasSize(1);
        TopologyTimeslice topologyTimeslice = timeslices.get(0);
        List<Device> devicesInTimeslice = topologyTimeslice.getDevices();
        assertThat(devicesInTimeslice).hasSize(3);
        Set<String> deviceNames = devicesInTimeslice.stream().map(Device::getName).collect(Collectors.toSet());
        assertThat(deviceNames).containsOnly("slave5", "slave4", "slave3");

        timeslices = inMemoryPersistence.getTopologyService().getPhysicalTopologyTimelineAdditions(gateway, 20).getSlices();
        assertThat(timeslices).hasSize(1);
        topologyTimeslice = timeslices.get(0);
        assertThat(topologyTimeslice.getDevices()).hasSize(5);
    }

    @Test
    @Transactional
    public void addFinalCommunicationPathSegment() {
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device gateway = deviceService.newDevice(deviceConfiguration, "gateway", "physGateway", Instant.now());
        gateway.save();
        Device slave = deviceService.newDevice(deviceConfiguration, "slave", "slave", Instant.now());
        slave.save();
        int expectedCost = 13;
        Duration expectedTimeToLive = Duration.ofMinutes(1);
        TopologyService.G3CommunicationPathSegmentBuilder segmentBuilder = topologyService.addCommunicationSegments(slave);

        // Business method
        segmentBuilder.add(gateway, gateway, expectedTimeToLive, expectedCost);
        List<G3CommunicationPathSegment> segments = segmentBuilder.complete();

        // Asserts
        assertThat(segments).hasSize(1);
        G3CommunicationPathSegment segment = segments.get(0);
        assertThat(segment).isNotNull();
        assertThat(segment.getCost()).isEqualTo(expectedCost);
        assertThat(segment.getTimeToLive()).isEqualTo(expectedTimeToLive);
        assertThat(segment.getNextHopDevice()).isNotNull();
        assertThat(segment.getNextHopDevice()).isEmpty();
    }

    @Test
    @Transactional
    public void addIntermediateCommunicationPathSegment() {
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device gateway = deviceService.newDevice(deviceConfiguration, "gateway", "physGateway", Instant.now());
        gateway.save();
        Device slave1 = deviceService.newDevice(deviceConfiguration, "slave1", "slave1", Instant.now());
        slave1.save();
        topologyService.setPhysicalGateway(slave1, gateway);
        Device slave2 = deviceService.newDevice(deviceConfiguration, "slave2", "slave2", Instant.now());
        slave2.save();
        int expectedCost = 17;
        Duration expectedTimeToLive = Duration.ofMinutes(1);
        TopologyService.G3CommunicationPathSegmentBuilder segmentBuilder = topologyService.addCommunicationSegments(slave1);

        // Business method
        segmentBuilder.add(gateway, slave2, expectedTimeToLive, expectedCost);
        List<G3CommunicationPathSegment> segments = segmentBuilder.complete();

        // Asserts
        assertThat(segments).hasSize(1);
        G3CommunicationPathSegment segment = segments.get(0);
        assertThat(segment).isNotNull();
        assertThat(segment.getCost()).isEqualTo(expectedCost);
        assertThat(segment.getTimeToLive()).isEqualTo(expectedTimeToLive);
        assertThat(segment.getNextHopDevice()).isNotNull();
        assertThat(segment.getNextHopDevice().isPresent()).isTrue();
        assertThat(segment.getNextHopDevice().get()).isEqualTo(slave2);
    }

    @Test
    @Transactional
    @Ignore // H2 can't handle the SQL queries
    public void addBuildCommunicationPath() {
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device gateway = deviceService.newDevice(deviceConfiguration, "gateway", "physGateway", Instant.now());
        gateway.save();
        Device slave1 = deviceService.newDevice(deviceConfiguration, "slave1", "slave1", Instant.now());
        slave1.save();
        topologyService.setPhysicalGateway(slave1, gateway);
        Device slave2 = deviceService.newDevice(deviceConfiguration, "slave2", "slave2", Instant.now());
        slave2.save();
        topologyService.setPhysicalGateway(slave2, gateway);
        Device slave3 = deviceService.newDevice(deviceConfiguration, "slave3", "slave3", Instant.now());
        slave3.save();
        topologyService.setPhysicalGateway(slave3, gateway);
        int cost = 17;
        Duration timeToLive = Duration.ofMinutes(1);
        topologyService.addCommunicationSegments(slave1).add(gateway, slave2, timeToLive, cost).complete();
        topologyService.addCommunicationSegments(slave2).add(gateway, slave3, timeToLive, cost).complete();
        topologyService.addCommunicationSegments(slave3).add(gateway, gateway, timeToLive, cost).complete();

        // Business method
        G3CommunicationPath communicationPath = topologyService.getCommunicationPath(slave1, gateway);

        // Asserts
        assertThat(communicationPath.getNumberOfHops()).isEqualTo(2);
    }

    @Test
    @Transactional
    public void buildNeigborhoodFromScratch() {
        Instant initialTimestamp = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(initialTimestamp);
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", initialTimestamp);
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1", initialTimestamp);
        neighbor1.save();
        Device neighbor2 = deviceService.newDevice(deviceConfiguration, "neighbor2", "neighbor2", initialTimestamp);
        neighbor2.save();
        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        neighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.DIFFERENTIAL, Modulation.D8PSK, PhaseInfo.INPHASE);
        neighborhoodBuilder.addNeighbor(neighbor2, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.DEGREE180);

        // Business method
        List<G3Neighbor> neighbors = neighborhoodBuilder.complete();

        // Asserts
        int expectedNumberOfNeighbors = 2;
        assertThat(neighbors).hasSize(expectedNumberOfNeighbors);
        for (G3Neighbor neighbor : neighbors) {
            if (neighbor.getNeighbor().getId() == neighbor1.getId()) {
                assertThat(neighbor.getModulationScheme()).isEqualTo(ModulationScheme.DIFFERENTIAL);
                assertThat(neighbor.getModulation()).isEqualTo(Modulation.D8PSK);
                assertThat(neighbor.getPhaseInfo()).isEqualTo(PhaseInfo.INPHASE);
                assertThat(neighbor.isEffectiveAt(initialTimestamp)).isTrue();
            }
            else {
                assertThat(neighbor.getModulationScheme()).isEqualTo(ModulationScheme.COHERENT);
                assertThat(neighbor.getModulation()).isEqualTo(Modulation.CBPSK);
                assertThat(neighbor.getPhaseInfo()).isEqualTo(PhaseInfo.DEGREE180);
                assertThat(neighbor.isEffectiveAt(initialTimestamp)).isTrue();
            }
        }
    }

    @Test
    @Transactional
    public void buildNeigborhoodFromScratchWithAllProperties() {
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", Instant.now());
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1", Instant.now());
        neighbor1.save();

        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        int expectedLinkQualityIndicator = 19;
        int expectedTimeToLiveSeconds = 127;
        int expectedToneMap = 31;
        int expectedTxGain = 3;
        int expectedTxResolution = 11;
        int expectedTxCoefficient = 121;
        neighborhoodBuilder
                .addNeighbor(neighbor1, ModulationScheme.DIFFERENTIAL, Modulation.D8PSK, PhaseInfo.INPHASE)
                .linkQualityIndicator(expectedLinkQualityIndicator)
                .timeToLiveSeconds(expectedTimeToLiveSeconds)
                .toneMap(expectedToneMap)
                .toneMapTimeToLiveSeconds(expectedTimeToLiveSeconds)
                .txGain(expectedTxGain)
                .txResolution(expectedTxResolution)
                .txCoefficient(expectedTxCoefficient);

        // Business method
        List<G3Neighbor> g3Neighbors = neighborhoodBuilder.complete();

        // Asserts
        assertThat(g3Neighbors).hasSize(1);
        G3Neighbor g3Neighbor1 = g3Neighbors.get(0);
        assertThat(g3Neighbor1.getModulationScheme()).isEqualTo(ModulationScheme.DIFFERENTIAL);
        assertThat(g3Neighbor1.getModulation()).isEqualTo(Modulation.D8PSK);
        assertThat(g3Neighbor1.getPhaseInfo()).isEqualTo(PhaseInfo.INPHASE);
        assertThat(g3Neighbor1.getLinkQualityIndicator()).isEqualTo(expectedLinkQualityIndicator);
        assertThat(g3Neighbor1.getTimeToLive()).isEqualTo(Duration.ofSeconds(expectedTimeToLiveSeconds));
        assertThat(g3Neighbor1.getToneMap()).isEqualTo(expectedToneMap);
        assertThat(g3Neighbor1.getToneMapTimeToLive()).isEqualTo(Duration.ofSeconds(expectedTimeToLiveSeconds));
        assertThat(g3Neighbor1.getTxGain()).isEqualTo(expectedTxGain);
        assertThat(g3Neighbor1.getTxResolution()).isEqualTo(expectedTxResolution);
        assertThat(g3Neighbor1.getTxCoefficient()).isEqualTo(expectedTxCoefficient);
    }

    @Test
    @Transactional
    public void findNeigborhoodDevices() {
        Instant initialTimestamp = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(initialTimestamp);
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", initialTimestamp);
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1", initialTimestamp);
        neighbor1.save();
        Device neighbor2 = deviceService.newDevice(deviceConfiguration, "neighbor2", "neighbor2", initialTimestamp);
        neighbor2.save();
        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        neighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.DIFFERENTIAL, Modulation.D8PSK, PhaseInfo.INPHASE);
        neighborhoodBuilder.addNeighbor(neighbor2, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.DEGREE180);
        List<G3Neighbor> neighbors = neighborhoodBuilder.complete();

        // Business method
        List<Device> devicesInG3Neighborhood = topologyService.findDevicesInG3Neighborhood(device);

        // Asserts
        int expectedNumberOfNeighbors = 2;
        assertThat(devicesInG3Neighborhood).hasSize(expectedNumberOfNeighbors);
        Set<Long> deviceIDs = devicesInG3Neighborhood.stream().map(Device::getId).collect(Collectors.toSet());
        assertThat(deviceIDs).containsOnly(neighbor1.getId(), neighbor2.getId());
        assertThat(neighbors).hasSize(expectedNumberOfNeighbors);
        assertThat(neighbors.get(0).isEffectiveAt(initialTimestamp)).isTrue();
        assertThat(neighbors.get(1).isEffectiveAt(initialTimestamp)).isTrue();
    }

    @Test
    @Transactional
    public void findNeigbors() {
        Instant initialTimestamp = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(initialTimestamp);
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", initialTimestamp);
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1", initialTimestamp);
        neighbor1.save();
        Device neighbor2 = deviceService.newDevice(deviceConfiguration, "neighbor2", "neighbor2", initialTimestamp);
        neighbor2.save();
        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        neighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.DIFFERENTIAL, Modulation.D8PSK, PhaseInfo.INPHASE);
        neighborhoodBuilder.addNeighbor(neighbor2, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.DEGREE180);
        neighborhoodBuilder.complete();

        // Business method
        List<G3Neighbor> neighbors = topologyService.findG3Neighbors(device);

        // Asserts
        int expectedNumberOfNeighbors = 2;
        assertThat(neighbors).hasSize(expectedNumberOfNeighbors);
        Set<Long> deviceIDs = neighbors.stream().map(G3Neighbor::getNeighbor).map(Device::getId).collect(Collectors.toSet());
        assertThat(deviceIDs).containsOnly(neighbor1.getId(), neighbor2.getId());
        assertThat(neighbors).hasSize(expectedNumberOfNeighbors);
        assertThat(neighbors.get(0).isEffectiveAt(initialTimestamp)).isTrue();
        assertThat(neighbors.get(1).isEffectiveAt(initialTimestamp)).isTrue();
    }

    @Test
    @Transactional
    public void findNoNeigborhoodDevicesInPast() {
        Instant past = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);
        Instant now = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(now);
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", now);
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1", now);
        neighbor1.save();
        Device neighbor2 = deviceService.newDevice(deviceConfiguration, "neighbor2", "neighbor2", now);
        neighbor2.save();
        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        neighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.DIFFERENTIAL, Modulation.D8PSK, PhaseInfo.INPHASE);
        neighborhoodBuilder.addNeighbor(neighbor2, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.DEGREE180);
        neighborhoodBuilder.complete();

        // Business method
        List<Device> devicesInG3Neighborhood = topologyService.findDevicesInG3Neighborhood(device, past);

        // Asserts
        assertThat(devicesInG3Neighborhood).isEmpty();
    }

    @Test
    @Transactional
    public void findNoNeigborsInPast() {
        Instant past = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);
        Instant now = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(inMemoryPersistence.getClock().instant()).thenReturn(now);
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", now);
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1", now);
        neighbor1.save();
        Device neighbor2 = deviceService.newDevice(deviceConfiguration, "neighbor2", "neighbor2", now);
        neighbor2.save();
        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        neighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.DIFFERENTIAL, Modulation.D8PSK, PhaseInfo.INPHASE);
        neighborhoodBuilder.addNeighbor(neighbor2, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.DEGREE180);
        neighborhoodBuilder.complete();

        // Business method
        List<G3Neighbor> neighbors = topologyService.findG3Neighbors(device, past);

        // Asserts
        assertThat(neighbors).isEmpty();
    }

    @Test
    @Transactional
    public void findNeigborhoodDevicesInFuture() {
        Instant now = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);
        Instant future = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(now);
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", now);
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1", now);
        neighbor1.save();
        Device neighbor2 = deviceService.newDevice(deviceConfiguration, "neighbor2", "neighbor2", now);
        neighbor2.save();
        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        neighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.DIFFERENTIAL, Modulation.D8PSK, PhaseInfo.INPHASE);
        neighborhoodBuilder.addNeighbor(neighbor2, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.DEGREE180);
        List<G3Neighbor> neighbors = neighborhoodBuilder.complete();

        // Business method
        List<Device> devicesInG3Neighborhood = topologyService.findDevicesInG3Neighborhood(device, future);

        // Asserts
        int expectedNumberOfNeighbors = 2;
        assertThat(devicesInG3Neighborhood).hasSize(expectedNumberOfNeighbors);
        Set<Long> deviceIDs = devicesInG3Neighborhood.stream().map(Device::getId).collect(Collectors.toSet());
        assertThat(deviceIDs).containsOnly(neighbor1.getId(), neighbor2.getId());
        assertThat(neighbors).hasSize(expectedNumberOfNeighbors);
        assertThat(neighbors.get(0).isEffectiveAt(future)).isTrue();
        assertThat(neighbors.get(1).isEffectiveAt(future)).isTrue();
    }

    @Test
    @Transactional
    public void findNeigborsInFuture() {
        Instant now = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);
        Instant future = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(now);
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", now);
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1", now);
        neighbor1.save();
        Device neighbor2 = deviceService.newDevice(deviceConfiguration, "neighbor2", "neighbor2", now);
        neighbor2.save();
        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        neighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.DIFFERENTIAL, Modulation.D8PSK, PhaseInfo.INPHASE);
        neighborhoodBuilder.addNeighbor(neighbor2, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.DEGREE180);
        neighborhoodBuilder.complete();

        // Business method
        List<G3Neighbor> neighbors = topologyService.findG3Neighbors(device, future);

        // Asserts
        int expectedNumberOfNeighbors = 2;
        assertThat(neighbors).hasSize(expectedNumberOfNeighbors);
        Set<Long> deviceIDs = neighbors.stream().map(G3Neighbor::getNeighbor).map(Device::getId).collect(Collectors.toSet());
        assertThat(deviceIDs).containsOnly(neighbor1.getId(), neighbor2.getId());
        assertThat(neighbors).hasSize(expectedNumberOfNeighbors);
        assertThat(neighbors.get(0).isEffectiveAt(future)).isTrue();
        assertThat(neighbors.get(1).isEffectiveAt(future)).isTrue();
    }

    @Test
    @Transactional
    public void findNeigborhoodDevicesInPast() {
        Instant past = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(past);
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", past);
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1", past);
        neighbor1.save();
        Device neighbor2 = deviceService.newDevice(deviceConfiguration, "neighbor2", "neighbor2", past);
        neighbor2.save();
        TopologyService.G3NeighborhoodBuilder initialBuilder = topologyService.buildG3Neighborhood(device);
        initialBuilder.addNeighbor(neighbor1, ModulationScheme.DIFFERENTIAL, Modulation.D8PSK, PhaseInfo.INPHASE);
        initialBuilder.addNeighbor(neighbor2, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.DEGREE180);
        initialBuilder.complete();
        Instant now = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(now);
        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        neighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.DIFFERENTIAL, Modulation.D8PSK, PhaseInfo.INPHASE);
        // Do not add neighbor2, effectively removing it from the neighborhood
        neighborhoodBuilder.complete();

        // Business method
        List<Device> devicesInG3Neighborhood = topologyService.findDevicesInG3Neighborhood(device, past);

        // Asserts
        assertThat(devicesInG3Neighborhood).hasSize(2);
    }

    @Test
    @Transactional
    public void findNeigborsInPast() {
        Instant past = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(past);
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", past);
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1", past);
        neighbor1.save();
        Device neighbor2 = deviceService.newDevice(deviceConfiguration, "neighbor2", "neighbor2", past);
        neighbor2.save();
        TopologyService.G3NeighborhoodBuilder initialBuilder = topologyService.buildG3Neighborhood(device);
        initialBuilder.addNeighbor(neighbor1, ModulationScheme.DIFFERENTIAL, Modulation.D8PSK, PhaseInfo.INPHASE);
        initialBuilder.addNeighbor(neighbor2, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.DEGREE180);
        initialBuilder.complete();
        Instant now = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(now);
        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        neighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.DIFFERENTIAL, Modulation.D8PSK, PhaseInfo.INPHASE);
        // Do not add neighbor2, effectively removing it from the neighborhood
        neighborhoodBuilder.complete();

        // Business method
        List<G3Neighbor> neighbors = topologyService.findG3Neighbors(device, past);

        // Asserts
        assertThat(neighbors).hasSize(2);
    }

    @Test
    @Transactional
    public void switchAllNeighboringDevicesFromDifferentialToCoherent() {
        Instant initialTimestamp = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(initialTimestamp);
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", initialTimestamp);
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1", initialTimestamp);
        neighbor1.save();
        Device neighbor2 = deviceService.newDevice(deviceConfiguration, "neighbor2", "neighbor2", initialTimestamp);
        neighbor2.save();
        TopologyService.G3NeighborhoodBuilder initialNeighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        initialNeighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.INPHASE);
        initialNeighborhoodBuilder.addNeighbor(neighbor2, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.DEGREE180);
        initialNeighborhoodBuilder.complete();
        Instant updateTimestamp = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(updateTimestamp);

        // Business method
        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        neighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.DIFFERENTIAL, Modulation.D8PSK, PhaseInfo.INPHASE);
        neighborhoodBuilder.addNeighbor(neighbor2, ModulationScheme.DIFFERENTIAL, Modulation.D8PSK, PhaseInfo.DEGREE180);
        neighborhoodBuilder.complete();

        // Asserts: assert that the device in the neigherbood are all still there
        List<Device> devicesInG3Neighborhood = topologyService.findDevicesInG3Neighborhood(device);
        assertThat(devicesInG3Neighborhood).hasSize(2);
        Set<Long> deviceIDs = devicesInG3Neighborhood.stream().map(Device::getId).collect(Collectors.toSet());
        assertThat(deviceIDs).containsOnly(neighbor1.getId(), neighbor2.getId());

        // Asserts: assert that the ModulationScheme has switched
        com.elster.jupiter.util.conditions.Condition condition = where("device").isEqualTo(device).and(where("interval").isEffective());
        List<G3Neighbor> updatedNeighbors = topologyService.findG3Neighbors(device);
        assertThat(updatedNeighbors).hasSize(2);
        assertThat(updatedNeighbors.get(0).getModulationScheme()).isEqualTo(ModulationScheme.DIFFERENTIAL);
        assertThat(updatedNeighbors.get(0).isEffectiveAt(initialTimestamp)).isFalse();
        assertThat(updatedNeighbors.get(0).isEffectiveAt(updateTimestamp)).isTrue();
        assertThat(updatedNeighbors.get(1).getModulationScheme()).isEqualTo(ModulationScheme.DIFFERENTIAL);
        assertThat(updatedNeighbors.get(1).isEffectiveAt(initialTimestamp)).isFalse();
        assertThat(updatedNeighbors.get(1).isEffectiveAt(updateTimestamp)).isTrue();
    }

    @Test
    @Transactional
    public void rebuildNeighboorhoodWithAllSamePropertiesDoesNotUpdate() {
        Instant initialTimestamp = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(initialTimestamp);
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", initialTimestamp);
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1", initialTimestamp);
        neighbor1.save();
        Device neighbor2 = deviceService.newDevice(deviceConfiguration, "neighbor2", "neighbor2", initialTimestamp);
        neighbor2.save();
        TopologyService.G3NeighborhoodBuilder initialNeighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        initialNeighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.INPHASE);
        initialNeighborhoodBuilder.addNeighbor(neighbor2, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.DEGREE180);
        initialNeighborhoodBuilder.complete();
        Instant updateTimestamp = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(updateTimestamp);

        // Business method
        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        neighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.INPHASE);
        neighborhoodBuilder.addNeighbor(neighbor2, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.DEGREE180);
        neighborhoodBuilder.complete();

        // Asserts: assert that the device in the neigherbood are all still there
        List<Device> devicesInG3Neighborhood = topologyService.findDevicesInG3Neighborhood(device);
        assertThat(devicesInG3Neighborhood).hasSize(2);
        Set<Long> deviceIDs = devicesInG3Neighborhood.stream().map(Device::getId).collect(Collectors.toSet());
        assertThat(deviceIDs).containsOnly(neighbor1.getId(), neighbor2.getId());

        // Asserts that none of the neighborhood attributes have changed, including the timestamp
        List<G3Neighbor> updatedNeighbors = topologyService.findG3Neighbors(device);
        assertThat(updatedNeighbors).hasSize(2);
        assertThat(updatedNeighbors.get(0).getModulationScheme()).isEqualTo(ModulationScheme.COHERENT);
        assertThat(updatedNeighbors.get(0).getModulation()).isEqualTo(Modulation.CBPSK);
        assertThat(updatedNeighbors.get(0).getPhaseInfo()).isEqualTo(PhaseInfo.INPHASE);
        assertThat(updatedNeighbors.get(0).isEffectiveAt(initialTimestamp)).isTrue();
        assertThat(updatedNeighbors.get(0).isEffectiveAt(updateTimestamp)).isTrue();
        assertThat(updatedNeighbors.get(1).getModulationScheme()).isEqualTo(ModulationScheme.COHERENT);
        assertThat(updatedNeighbors.get(1).getModulation()).isEqualTo(Modulation.CBPSK);
        assertThat(updatedNeighbors.get(1).getPhaseInfo()).isEqualTo(PhaseInfo.DEGREE180);
        assertThat(updatedNeighbors.get(1).isEffectiveAt(initialTimestamp)).isTrue();
        assertThat(updatedNeighbors.get(1).isEffectiveAt(updateTimestamp)).isTrue();
    }

    @Test
    @Transactional
    public void rebuildNeighboorhoodWithAllProperties() {
        Instant initialTimestamp = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(initialTimestamp);
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", initialTimestamp);
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1", initialTimestamp);
        neighbor1.save();
        TopologyService.G3NeighborhoodBuilder initialNeighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        initialNeighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.INPHASE);
        initialNeighborhoodBuilder.complete();
        Instant updateTimestamp = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(updateTimestamp);

        // Business method
        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        int expectedLinkQualityIndicator = 19;
        int expectedTimeToLiveSeconds = 127;
        int expectedToneMap = 31;
        int expectedTxGain = 3;
        int expectedTxResolution = 11;
        int expectedTxCoefficient = 121;
        neighborhoodBuilder
                .addNeighbor(neighbor1, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.INPHASE)
                .linkQualityIndicator(expectedLinkQualityIndicator)
                .timeToLiveSeconds(expectedTimeToLiveSeconds)
                .toneMap(expectedToneMap)
                .toneMapTimeToLiveSeconds(expectedTimeToLiveSeconds)
                .txGain(expectedTxGain)
                .txResolution(expectedTxResolution)
                .txCoefficient(expectedTxCoefficient);
        neighborhoodBuilder.complete();

        // Asserts: assert that the device in the neigherbood are all still there
        List<Device> devicesInG3Neighborhood = topologyService.findDevicesInG3Neighborhood(device);
        assertThat(devicesInG3Neighborhood).hasSize(1);
        Set<Long> deviceIDs = devicesInG3Neighborhood.stream().map(Device::getId).collect(Collectors.toSet());
        assertThat(deviceIDs).containsOnly(neighbor1.getId());

        // Assert the values of the attributes and the effective date of the attributes
        List<G3Neighbor> neighborsEffectiveNow = topologyService.findG3Neighbors(device);
        assertThat(neighborsEffectiveNow).hasSize(1);
        G3Neighbor updatedNeighbor = neighborsEffectiveNow.get(0);
        assertThat(updatedNeighbor.getModulationScheme()).isEqualTo(ModulationScheme.COHERENT);
        assertThat(updatedNeighbor.getModulation()).isEqualTo(Modulation.CBPSK);
        assertThat(updatedNeighbor.getPhaseInfo()).isEqualTo(PhaseInfo.INPHASE);
        assertThat(updatedNeighbor.getLinkQualityIndicator()).isEqualTo(expectedLinkQualityIndicator);
        assertThat(updatedNeighbor.getTimeToLive()).isEqualTo(Duration.ofSeconds(expectedTimeToLiveSeconds));
        assertThat(updatedNeighbor.getToneMap()).isEqualTo(expectedToneMap);
        assertThat(updatedNeighbor.getToneMapTimeToLive()).isEqualTo(Duration.ofSeconds(expectedTimeToLiveSeconds));
        assertThat(updatedNeighbor.getTxGain()).isEqualTo(expectedTxGain);
        assertThat(updatedNeighbor.getTxResolution()).isEqualTo(expectedTxResolution);
        assertThat(updatedNeighbor.getTxCoefficient()).isEqualTo(expectedTxCoefficient);
        assertThat(updatedNeighbor.isEffectiveAt(initialTimestamp)).isFalse();
        assertThat(updatedNeighbor.isEffectiveAt(updateTimestamp)).isTrue();

        // Assert that the old values of the attributes are still there but on older effective entities
        List<G3Neighbor> neighborsEffectiveAtInitialTimestamp = topologyService.findG3Neighbors(device, initialTimestamp);
        G3Neighbor initialNeighbor = neighborsEffectiveAtInitialTimestamp.get(0);
        assertThat(initialNeighbor.getModulationScheme()).isEqualTo(ModulationScheme.COHERENT);
        assertThat(initialNeighbor.getModulation()).isEqualTo(Modulation.CBPSK);
        assertThat(initialNeighbor.getPhaseInfo()).isEqualTo(PhaseInfo.INPHASE);
        assertThat(initialNeighbor.isEffectiveAt(initialTimestamp)).isTrue();
    }

    @Test
    @Transactional
    public void removeNeighbor() {
        Instant initialTimestamp = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(initialTimestamp);
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", initialTimestamp);
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1", initialTimestamp);
        neighbor1.save();
        Device neighbor2 = deviceService.newDevice(deviceConfiguration, "neighbor2", "neighbor2", initialTimestamp);
        neighbor2.save();
        TopologyService.G3NeighborhoodBuilder initialNeighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        initialNeighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.INPHASE);
        initialNeighborhoodBuilder.addNeighbor(neighbor2, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.DEGREE180);
        initialNeighborhoodBuilder.complete();
        Instant updateTimestamp = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(updateTimestamp);

        // Business method
        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        neighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.INPHASE);
        // Not adding neighbor2 should equal to removing it
        List<G3Neighbor> neighbors = neighborhoodBuilder.complete();

        // Asserts
        List<Device> devicesInG3Neighborhood = topologyService.findDevicesInG3Neighborhood(device);
        assertThat(devicesInG3Neighborhood).hasSize(1);
        Set<Long> deviceIDs = devicesInG3Neighborhood.stream().map(Device::getId).collect(Collectors.toSet());
        assertThat(deviceIDs).containsOnly(neighbor1.getId());
        assertThat(neighbors).hasSize(1);
        assertThat(neighbors.get(0).isEffectiveAt(initialTimestamp)).isTrue();
    }

    @Test
    @Transactional
    public void addNeighbor() {
        Instant fromDateForExistingNeighbors = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(fromDateForExistingNeighbors);
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", fromDateForExistingNeighbors);
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1", fromDateForExistingNeighbors);
        neighbor1.save();
        Device neighbor2 = deviceService.newDevice(deviceConfiguration, "neighbor2", "neighbor2", fromDateForExistingNeighbors);
        neighbor2.save();
        TopologyService.G3NeighborhoodBuilder initialNeighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        initialNeighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.INPHASE);
        initialNeighborhoodBuilder.complete();

        Instant fromDateForAddedNeighbor = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(fromDateForAddedNeighbor);

        // Business method
        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        neighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.INPHASE);
        neighborhoodBuilder.addNeighbor(neighbor2, ModulationScheme.DIFFERENTIAL, Modulation.D8PSK, PhaseInfo.INPHASE);
        List<G3Neighbor> neighbors = neighborhoodBuilder.complete();

        // Asserts
        List<Device> devicesInG3Neighborhood = topologyService.findDevicesInG3Neighborhood(device);
        assertThat(devicesInG3Neighborhood).hasSize(2);
        Set<Long> deviceIDs = devicesInG3Neighborhood.stream().map(Device::getId).collect(Collectors.toSet());
        assertThat(deviceIDs).containsOnly(neighbor1.getId(), neighbor2.getId());
        assertThat(neighbors).hasSize(2);
        for (G3Neighbor neighbor : neighbors) {
            if (neighbor.getNeighbor().getId() == neighbor1.getId()) {
                assertThat(neighbor.isEffectiveAt(fromDateForExistingNeighbors)).isTrue();
                assertThat(neighbor.isEffectiveAt(fromDateForAddedNeighbor)).isTrue();
            }
            else {
                assertThat(neighbor.isEffectiveAt(fromDateForExistingNeighbors)).isFalse();
                assertThat(neighbor.isEffectiveAt(fromDateForAddedNeighbor)).isTrue();
            }
        }
    }

    @Test(expected = IllegalStateException.class)
    @Transactional
    public void completeTwice () {
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", Instant.now());
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1", Instant.now());
        neighbor1.save();
        Device neighbor2 = deviceService.newDevice(deviceConfiguration, "neighbor2", "neighbor2", Instant.now());
        neighbor2.save();
        TopologyService.G3NeighborhoodBuilder builder = topologyService.buildG3Neighborhood(device);
        builder.addNeighbor(neighbor1, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.INPHASE);
        builder.complete();

        // Business method
        builder.complete();

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void findG3AddressInformationThatWasNeverCreated() {
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", Instant.now());
        device.save();

        //Business method
        Optional<G3DeviceAddressInformation> addressInformation = topologyService.getG3DeviceAddressInformation(device);

        // Asserts
        assertThat(addressInformation).isEmpty();
    }

    @Test
    @Transactional
    public void createFirstG3AddressInformation() {
        Instant effectiveTimestamp = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(effectiveTimestamp);
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", effectiveTimestamp);
        device.save();

        // Business method
        int expectedIPv6ShortAddress = 0x417A;
        int expectedLogicalDeviceId = 13;
        String expectedIPv6Address = "1080:0:0:0:8:800:200C:417A";
        G3DeviceAddressInformation addressInformation = topologyService.setG3DeviceAddressInformation(device, expectedIPv6Address, expectedIPv6ShortAddress, expectedLogicalDeviceId);

        // Asserts
        assertThat(addressInformation).isNotNull();
        assertThat(addressInformation.getDevice()).isEqualTo(device);
        assertThat(addressInformation.getIPv6Address().getHostName()).isEqualToIgnoringCase(expectedIPv6Address);
        assertThat(addressInformation.getIpv6ShortAddress()).isEqualTo(expectedIPv6ShortAddress);
        assertThat(addressInformation.getLogicalDeviceId()).isEqualTo(expectedLogicalDeviceId);
        assertThat(addressInformation.isEffectiveAt(effectiveTimestamp)).isTrue();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = MessageSeeds.Keys.INVALID_IPV6_ADDRESS)
    public void createG3AddressInformationWithInvalidIPv6Address() {
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", Instant.now());
        device.save();

        // Business method
        topologyService.setG3DeviceAddressInformation(device, "this is not an IPv6Address", 0x417A, 13);

        // Asserts: see expected constraint violation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = MessageSeeds.Keys.INVALID_IPV6_ADDRESS)
    public void createG3AddressInformationWithIPv4Address() {
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", Instant.now());
        device.save();

        // Business method
        topologyService.setG3DeviceAddressInformation(device, "10.0.2.53", 0x417A, 13);

        // Asserts: see expected constraint violation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", strict = false)
    public void createG3AddressInformationWithVeryLongIPv6Address() {
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", Instant.now());
        device.save();

        // Business method
        topologyService.setG3DeviceAddressInformation(device, "createG3AddressInformationWithVeryLongIPv6AddresscreateG3AddressInformationWithVeryLongIPv6AddresscreateG3AddressInformationWithVeryLongIPv6AddresscreateG3AddressInformationWithVeryLongIPv6AddresscreateG3AddressInformationWithVeryLongIPv6AddresscreateG3AddressInformationWithVeryLongIPv6AddresscreateG3AddressInformationWithVeryLongIPv6AddresscreateG3AddressInformationWithVeryLongIPv6Address", 0x417A, 13);

        // Asserts: see expected constraint violation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    public void createG3AddressInformationWithEmptyIPv6Address() {
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", Instant.now());
        device.save();

        // Business method
        topologyService.setG3DeviceAddressInformation(device, "", 0x417A, 13);

        // Asserts: see expected constraint violation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED_KEY + "}")
    public void createG3AddressInformationWithNullIPv6Address() {
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", Instant.now());
        device.save();

        // Business method
        topologyService.setG3DeviceAddressInformation(device, null, 0x417A, 13);

        // Asserts: see expected constraint violation rule
    }

    @Test
    @Transactional
    public void findG3AddressInformation() {
        // Create address information first
        Instant effectiveTimestamp = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(effectiveTimestamp);
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", effectiveTimestamp);
        device.save();
        int expectedIPv6ShortAddress = 0x417A;
        int expectedLogicalDeviceId = 13;
        String expectedIPv6Address = "1080:0:0:0:8:800:200C:417A";
        topologyService.setG3DeviceAddressInformation(device, expectedIPv6Address, expectedIPv6ShortAddress, expectedLogicalDeviceId);

        // Business method
        Optional<G3DeviceAddressInformation> addressInformation = topologyService.getG3DeviceAddressInformation(device);

        // Asserts
        assertThat(addressInformation).isNotNull();
        assertThat(addressInformation.isPresent()).isTrue();
        assertThat(addressInformation.get().getDevice().getId()).isEqualTo(device.getId());
        assertThat(addressInformation.get().getIPv6Address().getHostAddress()).isEqualToIgnoringCase(expectedIPv6Address);
        assertThat(addressInformation.get().getIpv6ShortAddress()).isEqualTo(expectedIPv6ShortAddress);
        assertThat(addressInformation.get().getLogicalDeviceId()).isEqualTo(expectedLogicalDeviceId);
        assertThat(addressInformation.get().isEffectiveAt(effectiveTimestamp)).isTrue();
    }

    @Test
    @Transactional
    public void addSameG3AddressInformationReturnsExisting() {
        // Create address information first
        Instant initialEffectiveTimestamp = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(initialEffectiveTimestamp);
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", initialEffectiveTimestamp);
        device.save();
        int expectedIPv6ShortAddress = 0x417A;
        int expectedLogicalDeviceId = 13;
        String expectedIPv6Address = "1080:0:0:0:8:800:200C:417A";
        G3DeviceAddressInformation initialAddressInformation = topologyService.setG3DeviceAddressInformation(device, expectedIPv6Address, expectedIPv6ShortAddress, expectedLogicalDeviceId);
        Instant updateEffectiveTimestamp = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(updateEffectiveTimestamp);

        // Business method
        G3DeviceAddressInformation updatedAddressInformation = topologyService.setG3DeviceAddressInformation(device, expectedIPv6Address, expectedIPv6ShortAddress, expectedLogicalDeviceId);

        // Asserts
        assertThat(updatedAddressInformation.isEffectiveAt(initialEffectiveTimestamp)).isTrue();
    }

    @Test
    @Transactional
    public void updateG3AddressInformation() {
        // Create address information first
        Instant initialEffectiveTimestamp = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(initialEffectiveTimestamp);
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", initialEffectiveTimestamp);
        device.save();
        int initialIPv6ShortAddress = 0x14A7;
        int initialLogicalDeviceId = 19;
        String expectedIPv6Address = "1080:0:0:0:8:800:200C:417A";
        G3DeviceAddressInformation initialAddressInformation = topologyService.setG3DeviceAddressInformation(device, expectedIPv6Address, initialIPv6ShortAddress, initialLogicalDeviceId);
        Instant updateEffectiveTimestamp = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(updateEffectiveTimestamp);

        int expectedIPv6ShortAddress = 0x417A;
        int expectedLogicalDeviceId = 13;

        // Business method
        G3DeviceAddressInformation updatedAddressInformation = topologyService.setG3DeviceAddressInformation(device, expectedIPv6Address, expectedIPv6ShortAddress, expectedLogicalDeviceId);

        // Asserts
        assertThat(updatedAddressInformation).isNotNull();
        assertThat(updatedAddressInformation.getDevice()).isEqualTo(device);
        assertThat(updatedAddressInformation.getIPv6Address().getHostName()).isEqualToIgnoringCase(expectedIPv6Address);
        assertThat(updatedAddressInformation.getIpv6ShortAddress()).isEqualTo(expectedIPv6ShortAddress);
        assertThat(updatedAddressInformation.getLogicalDeviceId()).isEqualTo(expectedLogicalDeviceId);
        assertThat(updatedAddressInformation.isEffectiveAt(updateEffectiveTimestamp)).isTrue();
    }

    @Test
    @Transactional
    public void findG3AddressInformationInPast() {
        // Create first address information
        Instant initialEffectiveTimestamp = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(initialEffectiveTimestamp);
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE", initialEffectiveTimestamp);
        device.save();
        int initialIPv6ShortAddress = 0x14A7;
        int initialLogicalDeviceId = 19;
        String expectedIPv6Address = "1080:0:0:0:8:800:200C:417A";
        topologyService.setG3DeviceAddressInformation(device, expectedIPv6Address, initialIPv6ShortAddress, initialLogicalDeviceId);

        // Update the address information
        Instant updateEffectiveTimestamp = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(updateEffectiveTimestamp);
        int updatedIPv6ShortAddress = 0x417A;
        int updatedLogicalDeviceId = 13;
        topologyService.setG3DeviceAddressInformation(device, expectedIPv6Address, updatedIPv6ShortAddress, updatedLogicalDeviceId);

        // Business method
        Optional<G3DeviceAddressInformation> addressInformation = topologyService.getG3DeviceAddressInformation(device, initialEffectiveTimestamp);

        // Asserts
        assertThat(addressInformation).isNotNull();
        assertThat(addressInformation.isPresent()).isTrue();
        assertThat(addressInformation.get().getDevice().getId()).isEqualTo(device.getId());
        assertThat(addressInformation.get().getIPv6Address().getHostAddress()).isEqualToIgnoringCase(expectedIPv6Address);
        assertThat(addressInformation.get().getIpv6ShortAddress()).isEqualTo(initialIPv6ShortAddress);
        assertThat(addressInformation.get().getLogicalDeviceId()).isEqualTo(initialLogicalDeviceId);
        assertThat(addressInformation.get().isEffectiveAt(initialEffectiveTimestamp)).isTrue();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(strict=false, messageId = "{" + MessageSeeds.Keys.DEVICE_CANNOT_BE_DATA_LOGGER_FOR_ITSELF + "}")
    public void setDataLoggerGatewaySameAsOriginDeviceTest() {
        Device slave = createSlaveDevice("Data Logger");

        // Business method
        this.getTopologyService().setDataLogger(slave, slave, Instant.now(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(strict=false, messageId = "{" + MessageSeeds.Keys.NOT_A_DATALOGGER_SLAVE_DEVICE + "}")
    public void originNotADataLoggerTest() {
        Device slave = createSimpleDeviceWithName("Not a datalogger slave");
        Device datalogger = createDataLoggerDevice("Data logger enabled");
        // Business method
        this.getTopologyService().setDataLogger(slave, datalogger, Instant.now(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(strict=false, messageId = "{" + MessageSeeds.Keys.GATEWAY_NOT_DATALOGGER_ENABLED + "}")
    public void setNotDataLoggerEnabledGatewayTest() {
        Device slave = createSlaveDevice("Slave");
        Device datalogger = createSimpleDeviceWithName("Data logger");
        // Business method
        this.getTopologyService().setDataLogger(slave, datalogger, Instant.now(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Test
    @Transactional
    public void setDataLoggerTest() {
        Instant now = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(now);

        Device slave = createSlaveDevice("Slave2");
        Device dataLogger = createDataLoggerDevice("Data logger enabled");
        // Business method
        Map<Register, Register> slaveDataLoggerRegisterMap = new HashMap<>();
        slaveDataLoggerRegisterMap.put(slave.getRegisters().get(0), dataLogger.getRegisters().get(0));
        this.getTopologyService().setDataLogger(slave, dataLogger, now, Collections.emptyMap(), slaveDataLoggerRegisterMap);

        List<DataLoggerReferenceImpl> gatewayReferences = ((ServerTopologyService) this.getTopologyService()).dataModel().query(DataLoggerReferenceImpl.class).select(com.elster.jupiter.util.conditions.Condition.TRUE);
        assertThat(gatewayReferences).hasSize(1);
        assertThat(gatewayReferences.get(0)).isInstanceOf(DataLoggerReferenceImpl.class);
        DataLoggerReferenceImpl dataLoggerReference = (DataLoggerReferenceImpl) gatewayReferences.get(0);
        assertThat(dataLoggerReference.getOrigin().getId()).isEqualTo(slave.getId());
        assertThat(dataLoggerReference.getGateway().getId()).isEqualTo(dataLogger.getId());
        assertThat(dataLoggerReference.getRange().lowerEndpoint()).isEqualTo(now);
        assertThat(dataLoggerReference.getDataLoggerChannelUsages()).hasSize(1);
    }

    @Test
    @Transactional
    public void isDataLoggerSlaveCandidateTest() {
        Instant now = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(now);

        Device slave = createSlaveDevice("Slave2");
        assertThat(this.getTopologyService().isDataLoggerSlaveCandidate(slave)).isTrue();

        Device dataLogger = createDataLoggerDevice("Data logger enabled");
        // Business method
        Map<Register, Register> slaveDataLoggerRegisterMap = new HashMap<>();
        slaveDataLoggerRegisterMap.put(slave.getRegisters().get(0), dataLogger.getRegisters().get(0));
        this.getTopologyService().setDataLogger(slave, dataLogger, now, Collections.emptyMap(), slaveDataLoggerRegisterMap);

        assertThat(this.getTopologyService().isDataLoggerSlaveCandidate(slave)).isFalse();
    }

    @Test
    @Transactional
    public void isDataLoggerSlaveCandidateForNonDataLoggerSlaveDeviceTypeTest() {
        Device dataLogger = createDataLoggerDevice("Data logger enabled");
        assertThat(this.getTopologyService().isDataLoggerSlaveCandidate(dataLogger)).isFalse();
    }

    @Test
    @Transactional
    public void findPhysicalConnectedDevicesOnDataLoggerTest() {
        Device dataLogger = createDataLoggerDevice("DataLogger");
        Device slave1 = createSlaveDevice("Slave1");
        Device slave2 = createSlaveDevice("Slave2");

        Instant now = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(now);

        // Business method
        Map<Register, Register> slaveDataLoggerRegisterMap1 = new HashMap<>();
        slaveDataLoggerRegisterMap1.put(slave1.getRegisters().get(0), dataLogger.getRegisters().get(0));
        Map<Register, Register> slaveDataLoggerRegisterMap2 = new HashMap<>();
        slaveDataLoggerRegisterMap2.put(slave2.getRegisters().get(0), dataLogger.getRegisters().get(1));
        this.getTopologyService().setDataLogger(slave1, dataLogger, clock.instant(), Collections.emptyMap(), slaveDataLoggerRegisterMap1);
        this.getTopologyService().setDataLogger(slave2, dataLogger, clock.instant(), Collections.emptyMap(), slaveDataLoggerRegisterMap2);

        // Business method
        List<Device> downstreamDevices = this.getTopologyService().findPhysicalConnectedDevices(dataLogger);

        // Asserts
        assertThat(downstreamDevices).hasSize(0);
    }

    @Test
    @Transactional
    public void findDataLoggerSlavesTest() {
        Device dataLogger = createDataLoggerDevice("DataLogger");
        Device slave1 = createSlaveDevice("Slave1");
        Device slave2 = createSlaveDevice("Slave2");

        Instant now = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(now);

        // Business method
        this.getTopologyService().setDataLogger(slave1, dataLogger, clock.instant(), Collections.emptyMap(), Collections.singletonMap(slave1.getRegisters().get(0), dataLogger.getRegisters().get(0)));
        this.getTopologyService().setDataLogger(slave2, dataLogger, clock.instant(), Collections.emptyMap(), Collections.singletonMap(slave2.getRegisters().get(0), dataLogger.getRegisters().get(1)));

        // Business method
        List<Device> downstreamDevices = this.getTopologyService().findDataLoggerSlaves(dataLogger);

        // Asserts
        assertThat(downstreamDevices).hasSize(2);
        assertThat(downstreamDevices).has(new Condition<List<? extends Device>>() {
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
    public void getDataLoggerRegisterTimeLineWithoutSlavesTest() {
        Device dataLogger = createDataLoggerDevice("DataLogger");
        Instant now = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        Instant lower = LocalDateTime.of(2013, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(now);

        Register<?, ?> dataLoggerRegister = dataLogger.getRegisters().get(0);
        Range<Instant> dataLoggerRange = Range.atLeast(lower);
        List<Pair<Register, Range<Instant>>> dataLoggerRegisterTimeLine = getTopologyService().getDataLoggerRegisterTimeLine(dataLoggerRegister, dataLoggerRange);
        assertThat(dataLoggerRegisterTimeLine).hasSize(1);
        assertThat(dataLoggerRegisterTimeLine.get(0).getFirst()).isEqualTo(dataLoggerRegister);
        assertThat(dataLoggerRegisterTimeLine.get(0).getLast()).isEqualTo(dataLoggerRange);
    }

    @Test
    @Transactional
    public void getDataLoggerRegisterTimeLineWithSingleUnlinkedSlaveTest() {
        Instant now = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        Instant linkingDate = LocalDateTime.of(2014, 1, 1, 0, 0).toInstant(ZoneOffset.UTC);
        Instant unlinkingDate = LocalDateTime.of(2014, 5, 4, 0, 0).toInstant(ZoneOffset.UTC);
        Instant lower = LocalDateTime.of(2013, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(lower);

        Device dataLogger = createDataLoggerDevice("DataLogger");
        Device slave1 = createSlaveDevice("Slave1");
        when(clock.instant()).thenReturn(now);

        Register<?, ?> dataLoggerRegister = dataLogger.getRegisters().get(0);
        // Business method
        this.getTopologyService().setDataLogger(slave1, dataLogger, linkingDate, Collections.emptyMap(), Collections.singletonMap(slave1.getRegisters().get(0), dataLoggerRegister));
        this.getTopologyService().clearDataLogger(slave1, unlinkingDate);

        Range<Instant> dataLoggerRange = Range.atLeast(lower);
        List<Pair<Register, Range<Instant>>> dataLoggerRegisterTimeLine = getTopologyService().getDataLoggerRegisterTimeLine(dataLoggerRegister, dataLoggerRange);
        assertThat(dataLoggerRegisterTimeLine).hasSize(3);
        assertThat(dataLoggerRegisterTimeLine.get(0).getFirst()).isEqualTo(dataLoggerRegister);
        assertThat(dataLoggerRegisterTimeLine.get(0).getLast()).isEqualTo(Range.atLeast(unlinkingDate));
        assertThat(dataLoggerRegisterTimeLine.get(1).getFirst().getDevice().getmRID()).isEqualTo(slave1.getmRID());
        assertThat(dataLoggerRegisterTimeLine.get(1).getFirst().getRegisterSpecId()).isEqualTo(slave1.getRegisters().get(0).getRegisterSpecId());
        assertThat(dataLoggerRegisterTimeLine.get(1).getLast()).isEqualTo(Range.openClosed(linkingDate, unlinkingDate));
        assertThat(dataLoggerRegisterTimeLine.get(2).getFirst()).isEqualTo(dataLoggerRegister);
        assertThat(dataLoggerRegisterTimeLine.get(2).getLast()).isEqualTo(Range.openClosed(lower, linkingDate));
    }

    @Test
    @Transactional
    public void getDataLoggerRegisterTimeLineWithSingleLinkedSlaveTest() {
        Instant now = LocalDateTime.of(2014, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        Instant linkingDate = LocalDateTime.of(2014, 1, 1, 0, 0).toInstant(ZoneOffset.UTC);
        Instant lower = LocalDateTime.of(2013, 12, 15, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(lower);

        Device dataLogger = createDataLoggerDevice("DataLogger");
        Device slave1 = createSlaveDevice("Slave1");
        when(clock.instant()).thenReturn(now);

        Register<?, ?> dataLoggerRegister = dataLogger.getRegisters().get(0);
        // Business method
        this.getTopologyService().setDataLogger(slave1, dataLogger, linkingDate, Collections.emptyMap(), Collections.singletonMap(slave1.getRegisters().get(0), dataLoggerRegister));

        Range<Instant> dataLoggerRange = Range.atLeast(lower);
        List<Pair<Register, Range<Instant>>> dataLoggerRegisterTimeLine = getTopologyService().getDataLoggerRegisterTimeLine(dataLoggerRegister, dataLoggerRange);
        assertThat(dataLoggerRegisterTimeLine).hasSize(2);
        assertThat(dataLoggerRegisterTimeLine.get(0).getFirst().getDevice().getmRID()).isEqualTo(slave1.getmRID());
        assertThat(dataLoggerRegisterTimeLine.get(0).getFirst().getRegisterSpecId()).isEqualTo(slave1.getRegisters().get(0).getRegisterSpecId());
        assertThat(dataLoggerRegisterTimeLine.get(0).getLast()).isEqualTo(Range.atLeast(linkingDate));
        assertThat(dataLoggerRegisterTimeLine.get(1).getFirst()).isEqualTo(dataLoggerRegister);
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
        when(clock.instant()).thenReturn(lower);

        Device dataLogger = createDataLoggerDevice("DataLogger");
        Device slave1 = createSlaveDevice("Slave1");
        Device slave2 = createSlaveDevice("Slave2");
        when(clock.instant()).thenReturn(now);

        Register<?, ?> dataLoggerRegister = dataLogger.getRegisters().get(0);
        // Business method
        this.getTopologyService().setDataLogger(slave1, dataLogger, linkDate1, Collections.emptyMap(), Collections.singletonMap(slave1.getRegisters().get(0), dataLoggerRegister));
        this.getTopologyService().clearDataLogger(slave1, unLinkDate1);
        this.getTopologyService().setDataLogger(slave2, dataLogger, linkDate2, Collections.emptyMap(), Collections.singletonMap(slave2.getRegisters().get(0), dataLoggerRegister));
        this.getTopologyService().clearDataLogger(slave2, unLinkDate2);

        Range<Instant> dataLoggerRange = Range.closedOpen(lower, now);
        List<Pair<Register, Range<Instant>>> dataLoggerRegisterTimeLine = getTopologyService().getDataLoggerRegisterTimeLine(dataLoggerRegister, dataLoggerRange);
        assertThat(dataLoggerRegisterTimeLine).hasSize(5);
        assertThat(dataLoggerRegisterTimeLine.get(0).getFirst()).isEqualTo(dataLoggerRegister);
        assertThat(dataLoggerRegisterTimeLine.get(0).getLast()).isEqualTo(Range.openClosed(unLinkDate2, now));
        assertThat(dataLoggerRegisterTimeLine.get(1).getFirst().getDevice().getmRID()).isEqualTo(slave2.getmRID());
        assertThat(dataLoggerRegisterTimeLine.get(1).getFirst().getRegisterSpecId()).isEqualTo(slave2.getRegisters().get(0).getRegisterSpecId());
        assertThat(dataLoggerRegisterTimeLine.get(1).getLast()).isEqualTo(Range.openClosed(linkDate2, unLinkDate2));
        assertThat(dataLoggerRegisterTimeLine.get(2).getFirst()).isEqualTo(dataLoggerRegister);
        assertThat(dataLoggerRegisterTimeLine.get(2).getLast()).isEqualTo(Range.openClosed(unLinkDate1, linkDate2));
        assertThat(dataLoggerRegisterTimeLine.get(3).getFirst().getDevice().getmRID()).isEqualTo(slave1.getmRID());
        assertThat(dataLoggerRegisterTimeLine.get(3).getFirst().getRegisterSpecId()).isEqualTo(slave1.getRegisters().get(0).getRegisterSpecId());
        assertThat(dataLoggerRegisterTimeLine.get(3).getLast()).isEqualTo(Range.openClosed(linkDate1, unLinkDate1));
        assertThat(dataLoggerRegisterTimeLine.get(4).getFirst()).isEqualTo(dataLoggerRegister);
        assertThat(dataLoggerRegisterTimeLine.get(4).getLast()).isEqualTo(Range.openClosed(lower, linkDate1));
    }

    private ServerDeviceService getDeviceService() {
        return inMemoryPersistence.getDeviceService();
    }

    private TopologyService getTopologyService() {
        return inMemoryPersistence.getTopologyService();
    }

}