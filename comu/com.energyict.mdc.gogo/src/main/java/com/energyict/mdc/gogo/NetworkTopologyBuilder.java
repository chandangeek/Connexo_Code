/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.gogo;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.Modulation;
import com.energyict.mdc.device.topology.ModulationScheme;
import com.energyict.mdc.device.topology.PhaseInfo;
import com.energyict.mdc.device.topology.TopologyService;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
                    Device child = createSlave();
                    currentNodeLevel.add(child);
                    addComPathSegmentsAndNeighbor(child, currentLevel);
                }
            }
            context.commit();
        }
    }

    private Device createSlave() {
        Device slave = deviceService.newDevice(randomConfiguration(), createSerial(this.nodeNbr++), clock.instant());
        topologyService.setPhysicalGateway(slave, gateway);
        System.out.println(String.format("created slave %s", slave.getName()));
        return slave;
    }

    private void initAvailableConfigurations(DeviceConfigurationService deviceConfigurationService, List<DeviceConfiguration> toInitiate) {
        deviceConfigurationService.findAllDeviceTypes().stream().filter(DeviceType::canActAsGateway).flatMap((t) -> t.getConfigurations().stream()).forEach(toInitiate::add);
    }

    private DeviceConfiguration randomConfiguration() {
        return availableConfigurations.get(new Random().nextInt(availableConfigurations.size()));
    }

    private String createSerial(long id) {
        return String.format("Node_%1s_of_%2s", id, gateway.getId());
    }

    private void addComPathSegmentsAndNeighbor(Device device, int levelNbr) {
        if (levelNbr > 1) {

            int cost = new Random().nextInt(100);
            List<Device> usedHops = new ArrayList<>();
            while (levelNbr > 1) {
                final Device intermediateHop = intermediateHop(levelNbr);
                if (usedHops.stream().anyMatch((d) -> d.getId() == intermediateHop.getId())) {
                    continue;
                }
                usedHops.add(intermediateHop);

                levelNbr--;

                TopologyService.G3CommunicationPathSegmentBuilder builder = topologyService.addCommunicationSegments(device);
                builder.add(intermediateHop, null, Duration.ofDays(14), cost);
                builder.complete();

                TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(intermediateHop);
                neighborhoodBuilder.addNeighbor(device, ModulationScheme.COHERENT, Modulation.fromOrdinal(0), PhaseInfo.NOPHASEINFO);
                neighborhoodBuilder.complete();
            }

        }
    }

    private Device intermediateHop(int level) {
        return levels[level].devices.get(new Random().nextInt(levels[level].devices.size()));
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
