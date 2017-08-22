package com.energyict.mdc.device.topology.rest.demo;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.G3CommunicationPathSegment;
import com.energyict.mdc.device.topology.G3Neighbor;
import com.energyict.mdc.device.topology.Modulation;
import com.energyict.mdc.device.topology.ModulationScheme;
import com.energyict.mdc.device.topology.PhaseInfo;
import com.energyict.mdc.device.topology.TopologyService;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * This factory builds a GraphInfo starting from a given device.
 * The underlaying devices are created
 * Copyrights EnergyICT
 * Date: 10/01/2017
 * Time: 9:34
 */
public class NetworkTopologyBuilder {

    private final ThreadPrincipalService threadPrincipalService;
    private final TransactionService transactionService;
    private final TopologyService topologyService;
    private final DeviceService deviceService;
    private final Clock clock;

    private final List<DeviceConfiguration> availableConfigurations = new ArrayList<>();
    private NodeLevel[] levels;
    private Device gateway;
    private int nodeCount;
    private int levelCount;
    private int nodeNbr;

    public NetworkTopologyBuilder(ThreadPrincipalService threadPrincipalService, TransactionService transactionService, DeviceService deviceService, TopologyService topologyService, DeviceConfigurationService deviceConfigurationService, Clock clock) {
        this.threadPrincipalService = threadPrincipalService;

        this.transactionService = transactionService;
        this.deviceService = deviceService;
        this.topologyService = topologyService;
        this.clock = clock;
        this.initAvailableConfigurations(deviceConfigurationService, availableConfigurations);

        assert availableConfigurations.size() > 0;
    }

    public NetworkTopologyBuilder havingNodes(int nodeCount) {
        assert nodeCount > 0;
        this.nodeCount = nodeCount;
        return this;
    }

    public NetworkTopologyBuilder havingLevels(int levelCount) {
        assert levelCount > 0;
        this.levelCount = levelCount;
        return this;
    }

    public void buildTopology(Device device) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {

            this.levels = new NodeLevel[levelCount];
            this.nodeNbr = 0;
            this.gateway = device;
            int maxNodesPerLevel = nodeCount / levelCount;

            for (int currentLevel = 0; currentLevel < levelCount; currentLevel++) {
                NodeLevel currentNodeLevel = new NodeLevel(currentLevel);
                levels[currentLevel] = currentNodeLevel;

                Random r = new Random();
                int nodesPerLevel = r.nextInt(maxNodesPerLevel) + 1;
                if (currentLevel == levelCount - 1) {
                    nodesPerLevel = nodeCount - nodeNbr;
                }

                for (int nodesOnLevelCount = 0; nodesOnLevelCount < nodesPerLevel; nodesOnLevelCount++) {
                    Device child = createNode();
                    currentNodeLevel.add(child);

                    addComPathSegmentsAndNeighbor(child, currentLevel);
                }
            }
            context.commit();
        }
    }

    private Device createNode() {
        String name = createSerial(++this.nodeNbr);
        Device child = deviceService.findDeviceByName(name).orElseGet(() ->  deviceService.newDevice(randomConfiguration(), name, clock.instant()));
        topologyService.clearPhysicalGateway(child); // reset parent
        topologyService.setPhysicalGateway(child, gateway);
        return child;
    }

    private void initAvailableConfigurations(DeviceConfigurationService deviceConfigurationService, List<DeviceConfiguration> toInitiate) {
        deviceConfigurationService.findAllDeviceTypes().stream().filter(DeviceType::canActAsGateway).flatMap((t) -> t.getConfigurations().stream()).forEach(toInitiate::add);
    }

    private DeviceConfiguration randomConfiguration() {
        return availableConfigurations.get(new Random().nextInt(availableConfigurations.size()));
    }

    private String createSerial(long id) {
        return String.format("Node_%1s_of_%2s", id, gateway.getName());
    }

    private void addComPathSegmentsAndNeighbor(Device device, int levelNbr) {
        List<Device> intermediateHops = new ArrayList<>();
        Optional<Device> deviceOnPreviousLevel = intermediateHop(levelNbr - 1);// random device on previous level
        if (deviceOnPreviousLevel.isPresent()) {
            intermediateHops.addAll(topologyService.getCommunicationPath(gateway, deviceOnPreviousLevel.get()).getIntermediateDevices());
            intermediateHops.add(deviceOnPreviousLevel.get());
        }
        TopologyService.G3CommunicationPathSegmentBuilder builder = topologyService.addCommunicationSegments(gateway);
        for (int i = 0; i < intermediateHops.size(); i++){
            Device hop = intermediateHops.get(i);
            builder.add(device, hop, Duration.ofDays(14), new Random().nextInt(100));
            builder.complete().forEach(this::addNeighbors);
            builder = topologyService.addCommunicationSegments(hop);
        }
        builder.add(device, null, Duration.ofDays(14), new Random().nextInt(100));
        builder.complete();
    }

    private List<G3Neighbor> addNeighbors(G3CommunicationPathSegment segment){
        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(segment.getSource());
        neighborhoodBuilder.addNeighbor(segment.getNextHopDevice().orElse(segment.getTarget()), ModulationScheme.COHERENT, Modulation.fromOrdinal(0), PhaseInfo.NOPHASEINFO)
                           .linkQualityIndicator(segment.getCost()) ;
        return neighborhoodBuilder.complete();
    }

    private Optional<Device> intermediateHop(int level) {
        if (level <= 0){
            return Optional.empty();
        }
        return Optional.of(levels[level].devices.get(new Random().nextInt(levels[level].devices.size())));
    }

    private class NodeLevel {
        int level;
        List<Device> devices = new ArrayList<>();

        NodeLevel(int level) {
            this.level = level;
        }

        boolean add(Device device) {
            return devices.add(device);
        }
    }

}
