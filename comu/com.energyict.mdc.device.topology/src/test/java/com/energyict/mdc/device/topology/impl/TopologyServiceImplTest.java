package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.G3Neighbor;
import com.energyict.mdc.device.topology.Modulation;
import com.energyict.mdc.device.topology.ModulationScheme;
import com.energyict.mdc.device.topology.PhaseInfo;
import com.energyict.mdc.device.topology.TopologyTimeline;
import com.energyict.mdc.device.topology.TopologyTimeslice;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.topology.G3CommunicationPath;
import com.energyict.mdc.device.topology.G3CommunicationPathSegment;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.device.BaseDevice;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import org.fest.assertions.core.Condition;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.*;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.fest.assertions.api.Assertions.assertThat;

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
        Device device = getDeviceService().newDevice(deviceConfiguration, "Slave", "SlaveMrid");
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

        Device device = getDeviceService().newDevice(deviceConfiguration, "Slave", MRID);
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
        assertThat(reloadedPhysicalGateway.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void getDefaultPhysicalGatewayNullTest() {
        Device simpleDevice = createSimpleDevice();

        // Business method
        Optional<Device> gateway = this.getTopologyService().getPhysicalGateway(simpleDevice);

        // Asserts
        assertThat(gateway.isPresent()).isFalse();
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
        Device device1 = this.getDeviceService().newDevice(deviceConfiguration, "Origin1", MRID);
        device1.save();
        this.getTopologyService().setPhysicalGateway(device1, physicalMaster);
        Device device2 = this.getDeviceService().newDevice(deviceConfiguration, "Origin2", MRID+"2");
        device2.save();
        this.getTopologyService().setPhysicalGateway(device2, physicalMaster);

        // Business method
        List<Device> downstreamDevices = this.getTopologyService().findPhysicalConnectedDevices(physicalMaster);

        // Asserts
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
    public void findDownstreamDevicesAfterRemovalOfOneTest() {
        Device physicalMaster = createSimpleDeviceWithName("PhysicalMaster");
        Device device1 = this.getDeviceService().newDevice(deviceConfiguration, "Origin1", "1");
        device1.save();
        this.getTopologyService().setPhysicalGateway(device1, physicalMaster);
        Device device2 = this.getDeviceService().newDevice(deviceConfiguration, "Origin2", "2");
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
        Device device1 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "Origin1", "1");
        device1.save();
        this.getTopologyService().setPhysicalGateway(device1, physicalMaster);
        Device device2 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "Origin2", "2");
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
        Device device1 = this.getDeviceService().newDevice(deviceConfiguration, "Origin1", "1");
        device1.save();
        this.getTopologyService().setPhysicalGateway(device1, physicalMaster);
        Device device2 = this.getDeviceService().newDevice(deviceConfiguration, "Origin2", "2");
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
        Device gateway = deviceService.newDevice(deviceConfiguration, "gateway", "physGateway");
        gateway.save();

        Device slave1 = deviceService.newDevice(deviceConfiguration, "slave1", "slave1");
        slave1.save();
        topologyService.setPhysicalGateway(slave1, gateway);

        Device slave2 = deviceService.newDevice(deviceConfiguration, "slave2", "slave2");
        slave2.save();
        topologyService.setPhysicalGateway(slave2, gateway);

        Device slave3 = deviceService.newDevice(deviceConfiguration, "slave3", "slave3");
        slave3.save();
        topologyService.setPhysicalGateway(slave3, gateway);

        Device slave4 = deviceService.newDevice(deviceConfiguration, "slave4", "slave4");
        slave4.save();
        topologyService.setPhysicalGateway(slave4, gateway);

        Device slave5 = deviceService.newDevice(deviceConfiguration, "slave5", "slave5");
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
        Device gateway = deviceService.newDevice(deviceConfiguration, "gateway", "physGateway");
        gateway.save();
        Device slave = deviceService.newDevice(deviceConfiguration, "slave", "slave");
        slave.save();
        int expectedCost = 13;
        Duration expectedTimeToLive = Duration.ofMinutes(1);

        // Business method
        G3CommunicationPathSegment segment = topologyService.addCommunicationSegment(slave, gateway, gateway, expectedTimeToLive, expectedCost);

        // Asserts
        assertThat(segment).isNotNull();
        assertThat(segment.getCost()).isEqualTo(expectedCost);
        assertThat(segment.getTimeToLive()).isEqualTo(expectedTimeToLive);
        assertThat(segment.getNextHopDevice()).isNotNull();
        assertThat(segment.getNextHopDevice().isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void addIntermediateCommunicationPathSegment() {
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device gateway = deviceService.newDevice(deviceConfiguration, "gateway", "physGateway");
        gateway.save();
        Device slave1 = deviceService.newDevice(deviceConfiguration, "slave1", "slave1");
        slave1.save();
        topologyService.setPhysicalGateway(slave1, gateway);
        Device slave2 = deviceService.newDevice(deviceConfiguration, "slave2", "slave2");
        slave2.save();
        int expectedCost = 17;
        Duration expectedTimeToLive = Duration.ofMinutes(1);

        // Business method
        G3CommunicationPathSegment segment = topologyService.addCommunicationSegment(slave1, gateway, slave2, expectedTimeToLive, expectedCost);

        // Asserts
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
        Device gateway = deviceService.newDevice(deviceConfiguration, "gateway", "physGateway");
        gateway.save();
        Device slave1 = deviceService.newDevice(deviceConfiguration, "slave1", "slave1");
        slave1.save();
        topologyService.setPhysicalGateway(slave1, gateway);
        Device slave2 = deviceService.newDevice(deviceConfiguration, "slave2", "slave2");
        slave2.save();
        topologyService.setPhysicalGateway(slave2, gateway);
        Device slave3 = deviceService.newDevice(deviceConfiguration, "slave3", "slave3");
        slave3.save();
        topologyService.setPhysicalGateway(slave3, gateway);
        int cost = 17;
        Duration timeToLive = Duration.ofMinutes(1);
        topologyService.addCommunicationSegment(slave1, gateway, slave2, timeToLive, cost);
        topologyService.addCommunicationSegment(slave2, gateway, slave3, timeToLive, cost);
        topologyService.addCommunicationSegment(slave3, gateway, gateway, timeToLive, cost);

        // Business method
        G3CommunicationPath communicationPath = topologyService.getCommunicationPath(slave1, gateway);

        // Asserts
        assertThat(communicationPath.getNumberOfHops()).isEqualTo(2);
    }

    @Test
    @Transactional
    public void buildNeigborhoodFromScratch() {
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE");
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1");
        neighbor1.save();
        Device neighbor2 = deviceService.newDevice(deviceConfiguration, "neighbor2", "neighbor2");
        neighbor2.save();

        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        neighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.DIFFERENTIAL, Modulation.D8PSK, PhaseInfo.INPHASE);
        neighborhoodBuilder.addNeighbor(neighbor2, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.DEGREE180);

        // Business method
        List<G3Neighbor> g3Neighbors = neighborhoodBuilder.complete();

        // Asserts
        assertThat(g3Neighbors).hasSize(2);
        G3Neighbor g3Neighbor1 = g3Neighbors.get(0);
        G3Neighbor g3Neighbor2 = g3Neighbors.get(1);
        assertThat(g3Neighbor1.getModulationScheme()).isEqualTo(ModulationScheme.DIFFERENTIAL);
        assertThat(g3Neighbor1.getModulation()).isEqualTo(Modulation.D8PSK);
        assertThat(g3Neighbor1.getPhaseInfo()).isEqualTo(PhaseInfo.INPHASE);
        assertThat(g3Neighbor2.getModulationScheme()).isEqualTo(ModulationScheme.COHERENT);
        assertThat(g3Neighbor2.getModulation()).isEqualTo(Modulation.CBPSK);
        assertThat(g3Neighbor2.getPhaseInfo()).isEqualTo(PhaseInfo.DEGREE180);
    }

    @Test
    @Transactional
    public void buildNeigborhoodFromScratchWithAllProperties() {
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE");
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1");
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
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE");
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1");
        neighbor1.save();
        Device neighbor2 = deviceService.newDevice(deviceConfiguration, "neighbor2", "neighbor2");
        neighbor2.save();

        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        neighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.DIFFERENTIAL, Modulation.D8PSK, PhaseInfo.INPHASE);
        neighborhoodBuilder.addNeighbor(neighbor2, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.DEGREE180);
        neighborhoodBuilder.complete();

        // Business method
        List<Device> devicesInG3Neighborhood = topologyService.findDevicesInG3Neighborhood(device);

        // Asserts
        assertThat(devicesInG3Neighborhood).hasSize(2);
        Set<Long> deviceIDs = devicesInG3Neighborhood.stream().map(Device::getId).collect(Collectors.toSet());
        assertThat(deviceIDs).containsOnly(neighbor1.getId(), neighbor2.getId());
    }

    @Test
    @Transactional
    public void switchAllNeighboringDevicesFromDifferentialToCoherent() {
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE");
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1");
        neighbor1.save();
        Device neighbor2 = deviceService.newDevice(deviceConfiguration, "neighbor2", "neighbor2");
        neighbor2.save();

        TopologyService.G3NeighborhoodBuilder initialNeighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        initialNeighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.INPHASE);
        initialNeighborhoodBuilder.addNeighbor(neighbor2, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.DEGREE180);
        initialNeighborhoodBuilder.complete();

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
        List<G3NeighborImpl> updatedNeighbors = inMemoryPersistence.getDataModel().mapper(G3NeighborImpl.class).select(condition);
        assertThat(updatedNeighbors).hasSize(2);
        assertThat(updatedNeighbors.get(0).getModulationScheme()).isEqualTo(ModulationScheme.DIFFERENTIAL);
        assertThat(updatedNeighbors.get(1).getModulationScheme()).isEqualTo(ModulationScheme.DIFFERENTIAL);
    }

    @Test
    @Transactional
    public void rebuildNeighboorhoodWithAllSamePropertiesDoesNotUpdate() {
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE");
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1");
        neighbor1.save();
        Device neighbor2 = deviceService.newDevice(deviceConfiguration, "neighbor2", "neighbor2");
        neighbor2.save();

        TopologyService.G3NeighborhoodBuilder initialNeighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        initialNeighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.INPHASE);
        initialNeighborhoodBuilder.addNeighbor(neighbor2, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.DEGREE180);
        initialNeighborhoodBuilder.complete();

        // Business method
        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        initialNeighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.INPHASE);
        initialNeighborhoodBuilder.addNeighbor(neighbor2, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.DEGREE180);
        neighborhoodBuilder.complete();

        // Asserts: assert that the device in the neigherbood are all still there
        List<Device> devicesInG3Neighborhood = topologyService.findDevicesInG3Neighborhood(device);
        assertThat(devicesInG3Neighborhood).hasSize(2);
        Set<Long> deviceIDs = devicesInG3Neighborhood.stream().map(Device::getId).collect(Collectors.toSet());
        assertThat(deviceIDs).containsOnly(neighbor1.getId(), neighbor2.getId());

        // Asserts: assert that none of the neighborhood attributes have changed
        com.elster.jupiter.util.conditions.Condition condition = where("device").isEqualTo(device).and(where("interval").isEffective());
        List<G3NeighborImpl> updatedNeighbors = inMemoryPersistence.getDataModel().mapper(G3NeighborImpl.class).select(condition);
        assertThat(updatedNeighbors).hasSize(2);
        assertThat(updatedNeighbors.get(0).getModulationScheme()).isEqualTo(ModulationScheme.COHERENT);
        assertThat(updatedNeighbors.get(0).getModulation()).isEqualTo(Modulation.CBPSK);
        assertThat(updatedNeighbors.get(0).getPhaseInfo()).isEqualTo(PhaseInfo.INPHASE);
        assertThat(updatedNeighbors.get(1).getModulationScheme()).isEqualTo(ModulationScheme.COHERENT);
        assertThat(updatedNeighbors.get(1).getModulation()).isEqualTo(Modulation.CBPSK);
        assertThat(updatedNeighbors.get(1).getPhaseInfo()).isEqualTo(PhaseInfo.DEGREE180);
    }

    @Test
    @Transactional
    public void rebuildNeighboorhoodWithAllProperties() {
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE");
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1");
        neighbor1.save();

        TopologyService.G3NeighborhoodBuilder initialNeighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        initialNeighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.INPHASE);
        initialNeighborhoodBuilder.complete();

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

        // Asserts: assert that none of the neighborhood attributes have changed
        com.elster.jupiter.util.conditions.Condition condition = where("device").isEqualTo(device).and(where("interval").isEffective());
        List<G3NeighborImpl> updatedNeighbors = inMemoryPersistence.getDataModel().mapper(G3NeighborImpl.class).select(condition);
        assertThat(updatedNeighbors).hasSize(1);
        G3NeighborImpl updatedNeighbor = updatedNeighbors.get(0);
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
    }

    @Test
    @Transactional
    public void removeNeighbor() {
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE");
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1");
        neighbor1.save();
        Device neighbor2 = deviceService.newDevice(deviceConfiguration, "neighbor2", "neighbor2");
        neighbor2.save();

        TopologyService.G3NeighborhoodBuilder initialNeighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        initialNeighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.INPHASE);
        initialNeighborhoodBuilder.addNeighbor(neighbor2, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.DEGREE180);
        initialNeighborhoodBuilder.complete();

        // Business method
        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        neighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.DIFFERENTIAL, Modulation.D8PSK, PhaseInfo.INPHASE);
        // Not adding neighbor2 should equal to removing it
        neighborhoodBuilder.complete();

        // Asserts
        List<Device> devicesInG3Neighborhood = topologyService.findDevicesInG3Neighborhood(device);
        assertThat(devicesInG3Neighborhood).hasSize(1);
        Set<Long> deviceIDs = devicesInG3Neighborhood.stream().map(Device::getId).collect(Collectors.toSet());
        assertThat(deviceIDs).containsOnly(neighbor1.getId());
    }

    @Test
    @Transactional
    public void addNeighbor() {
        ServerDeviceService deviceService = this.getDeviceService();
        TopologyService topologyService = this.getTopologyService();
        Device device = deviceService.newDevice(deviceConfiguration, "device", "DEVICE");
        device.save();
        Device neighbor1 = deviceService.newDevice(deviceConfiguration, "neighbor1", "neighbor1");
        neighbor1.save();
        Device neighbor2 = deviceService.newDevice(deviceConfiguration, "neighbor2", "neighbor2");
        neighbor2.save();

        TopologyService.G3NeighborhoodBuilder initialNeighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        initialNeighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.INPHASE);
        initialNeighborhoodBuilder.complete();

        // Business method
        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(device);
        neighborhoodBuilder.addNeighbor(neighbor1, ModulationScheme.DIFFERENTIAL, Modulation.D8PSK, PhaseInfo.INPHASE);
        neighborhoodBuilder.addNeighbor(neighbor2, ModulationScheme.COHERENT, Modulation.CBPSK, PhaseInfo.DEGREE180);
        neighborhoodBuilder.complete();

        // Asserts
        List<Device> devicesInG3Neighborhood = topologyService.findDevicesInG3Neighborhood(device);
        assertThat(devicesInG3Neighborhood).hasSize(2);
        Set<Long> deviceIDs = devicesInG3Neighborhood.stream().map(Device::getId).collect(Collectors.toSet());
        assertThat(deviceIDs).containsOnly(neighbor1.getId(), neighbor2.getId());
    }

    private ServerDeviceService getDeviceService() {
        return inMemoryPersistence.getDeviceService();
    }

    private TopologyService getTopologyService() {
        return inMemoryPersistence.getTopologyService();
    }

}