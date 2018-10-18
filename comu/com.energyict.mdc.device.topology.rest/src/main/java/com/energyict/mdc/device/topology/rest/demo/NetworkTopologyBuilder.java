package com.energyict.mdc.device.topology.rest.demo;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.G3CommunicationPathSegment;
import com.energyict.mdc.device.topology.G3Neighbor;
import com.energyict.mdc.device.topology.G3NodeState;
import com.energyict.mdc.device.topology.Modulation;
import com.energyict.mdc.device.topology.ModulationScheme;
import com.energyict.mdc.device.topology.PhaseInfo;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.rest.demo.layer.GraphLayerBuilder;

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

    List<GraphLayerBuilder> graphLayerBuilders = new ArrayList<>();

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

    public NetworkTopologyBuilder havingGraphLayerBuilder(GraphLayerBuilder graphLayerBuilder){
        graphLayerBuilders.add(graphLayerBuilder);
        return this;
    }

    public void buildTopology(Device device) {
        this.levels = new NodeLevel[levelCount];
        this.nodeNbr = 0;
        this.gateway = device;

        for (int currentLevel = 0; currentLevel < levelCount; currentLevel++) {
            System.out.println(String.format("level %d", currentLevel));
            NodeLevel currentNodeLevel = new NodeLevel(currentLevel);
            levels[currentLevel] = currentNodeLevel;

            Random r = new Random();
            int nodesPerLevel;
            if (currentLevel == levelCount - 1) {
                nodesPerLevel = nodeCount - nodeNbr;
            }else{
                nodesPerLevel = ((r.nextInt(2* (nodeCount - nodeNbr) / (levelCount - currentLevel)) + 1) );
            }

            for (int nodesOnLevelCount = 0; nodesOnLevelCount < nodesPerLevel; nodesOnLevelCount++) {
                Device child = createNode();
                currentNodeLevel.add(child);

                addComPathSegmentsAndNeighbor(child, currentLevel);
                graphLayerBuilders.stream().forEach((gb) -> gb.buildLayer(child));
            }
        }
    }

    private Device createNode() {
        String name = createSerial(++this.nodeNbr);
        Device child = deviceService.findDeviceByName(name).orElseGet(() ->  deviceService.newDevice(randomConfiguration(), name, clock.instant()));
        topologyService.clearPhysicalGateway(child); // reset parent
        topologyService.setPhysicalGateway(child, gateway);
        System.out.println(String.format("created slave %s (%d)", child.getName(), child.getId()));
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

        TopologyService.G3CommunicationPathSegmentBuilder builder = topologyService.addCommunicationSegments();

        if( intermediateHops.size() > 0) {
            for (int i = 0; i < intermediateHops.size(); i++) {
                Device hop = intermediateHops.get(i);
                System.out.println(String.format("hop with id %d", hop.getId()));
                builder.add(gateway, device, hop, Duration.ofDays(14), new Random().nextInt(100));
                builder.add(hop, device, null, Duration.ofDays(14), new Random().nextInt(100));
                builder.complete().forEach(this::addNeighbors);
                builder = topologyService.addCommunicationSegments();
            }
        }
        else
        {
            builder.add(gateway, device, null, Duration.ofDays(14), new Random().nextInt(100));
            builder.complete();
        }
    }

    private List<G3Neighbor> addNeighbors(G3CommunicationPathSegment segment){
        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(segment.getSource());
        neighborhoodBuilder.addNeighbor(segment.getNextHopDevice().orElse(segment.getTarget()), ModulationScheme.COHERENT, Modulation.fromId(99), PhaseInfo.NOPHASEINFO, G3NodeState.UNKNOWN)
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
