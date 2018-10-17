/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.commands.devices.CreateBeaconDeviceCommand;
import com.elster.jupiter.demo.impl.commands.devices.CreateHANDeviceCommand;
import com.elster.jupiter.demo.impl.templates.ComScheduleTpl;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.topology.*;
import com.energyict.mdc.device.topology.rest.demo.layer.GraphLayerBuilder;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.scheduling.SchedulingService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class CreateNetworkManagementCommand extends CommandWithTransaction {

    private final ThreadPrincipalService threadPrincipalService;
    private final TransactionService transactionService;
    private final MeteringService meteringService;
    private final TopologyService topologyService;
    private final DeviceService deviceService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceLifeCycleService deviceLifeCycleService;
    private final Clock clock;
    private final EngineConfigurationService engineConfigurationService;
    private final SchedulingService schedulingService;
    private final ConnectionTaskService connectionTaskService;
    private final CommunicationTaskService communicationTaskService;
    private final IssueService issueService;
    private final IssueCreationService issueCreationService;
    private final DeviceAlarmService deviceAlarmService;
    private final Provider<CreateBeaconDeviceCommand> createBeaconDeviceCommandProvider;
    private final Provider<CreateHANDeviceCommand> createHANDeviceCommandProvider;
    private final NlsService nlsService;
    private final TimeService timeService;
    private final Injector injector;

    Integer masterDeviceCount;
    Integer slaveDeviceCount;
    Integer levelCount;

    private String host = "158.138.16.171";
    private NodeLevel[] levels;
    private int nodeCount;
    private int nodeNbr;
    List<GraphLayerBuilder> graphLayerBuilders = new ArrayList<>();

    @Inject
    public CreateNetworkManagementCommand(ThreadPrincipalService threadPrincipalService,
                                          TransactionService transactionService,
                                          MeteringService meteringService,
                                          TopologyService topologyService,
                                          DeviceService deviceService,
                                          DeviceConfigurationService deviceConfigurationService,
                                          DeviceLifeCycleService deviceLifeCycleService,
                                          EngineConfigurationService engineConfigurationService,
                                          SchedulingService schedulingService,
                                          ConnectionTaskService connectionTaskService,
                                          CommunicationTaskService communicationTaskService,
                                          IssueService issueService,
                                          IssueCreationService issueCreationService,
                                          DeviceAlarmService deviceAlarmService,
                                          NlsService nlsService,
                                          TimeService timeService,
                                          Clock clock,
                                          Injector injector,
                                          Provider<CreateBeaconDeviceCommand> createBeaconDeviceCommandProvider,
                                          Provider<CreateHANDeviceCommand> createHANDeviceCommandProvider) {
        this.threadPrincipalService = threadPrincipalService;
        this.transactionService = transactionService;
        this.meteringService = meteringService;
        this.topologyService = topologyService;
        this.deviceService = deviceService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.engineConfigurationService = engineConfigurationService;
        this.schedulingService = schedulingService;
        this.connectionTaskService = connectionTaskService;
        this.communicationTaskService = communicationTaskService;
        this.issueService = issueService;
        this.issueCreationService = issueCreationService;
        this.deviceAlarmService = deviceAlarmService;
        this.nlsService = nlsService;
        this.timeService = timeService;
        this.clock = clock;
        this.injector = injector;
        this.createBeaconDeviceCommandProvider = createBeaconDeviceCommandProvider;
        this.createHANDeviceCommandProvider = createHANDeviceCommandProvider;
    }

    public void setMasterDeviceCount(int masterDeviceCount) {
        this.masterDeviceCount = masterDeviceCount;
    }

    public void setSlaveDeviceCount(int slaveDeviceCount) {
        this.slaveDeviceCount = slaveDeviceCount;
    }

    public void setLevelCount(int levelCount) {
        this.levelCount = levelCount;
    }

    @Override
    public void run() {
        if (this.masterDeviceCount == null || this.slaveDeviceCount == null || this.levelCount == null) {
            System.out.println("CreateNetworkManagementCommand <number of nodes> <number of childnodes> <number of levels>");
        } else {
            System.out.println(String.format("Building topology for %1d nodes with %2d childnodes having %3d levels", this.masterDeviceCount, this.slaveDeviceCount, this.levelCount));

            for (int i = 0; i < masterDeviceCount; i++) {

                DeviceTypeTpl deviceTypeTpl = DeviceTypeTpl.BEACON_3100;
                DeviceType deviceType = Builders.from(deviceTypeTpl).get();

                DeviceConfiguration configuration = Builders.from(DeviceConfigurationTpl.DEFAULT_BEACON).withDeviceType(deviceType).get();
                Long masterSerialNumber = 34157300000001L + i;
                String masterDeviceName = Constants.Device.BEACON_PREFIX + String.format("%04d", i);
                createBeaconDevice(configuration, masterDeviceName, masterSerialNumber.toString(), deviceTypeTpl);
                Device masterDevice = deviceService.findDeviceByName(masterDeviceName).get();

                deviceType = Builders.from(DeviceTypeTpl.AM540_DLMS).get();
                levels = new NodeLevel[levelCount];
                nodeNbr = 0;
                nodeCount = slaveDeviceCount;

                for (int currentLevel = 0; currentLevel < levelCount; currentLevel++) {
                    System.out.println(String.format("level %d", currentLevel));
                    NodeLevel currentNodeLevel = new NodeLevel(currentLevel);
                    levels[currentLevel] = currentNodeLevel;

                    Random r = new Random();
                    int nodesPerLevel;
                    if (currentLevel == levelCount - 1) {
                        nodesPerLevel = nodeCount - nodeNbr;
                    } else {
                        nodesPerLevel = ((r.nextInt(2 * (nodeCount - nodeNbr) / (levelCount - currentLevel)) + 1));
                    }

                    for (int nodesOnLevelCount = 0; nodesOnLevelCount < nodesPerLevel; nodesOnLevelCount++) {
                        String slaveDeviceName = createName(++this.nodeNbr, masterDevice);
                        String slaveSerialNumber = createSerial(this.nodeNbr, masterDevice);
                        createBeaconSlaveDevice(Builders.from(DeviceConfigurationTpl.DEFAULT_AM540).withDeviceType(deviceType).get(),
                                slaveDeviceName,
                                slaveSerialNumber,
                                DeviceTypeTpl.AM540_DLMS,
                                masterDeviceName);
                        Device slaveDevice = deviceService.findDeviceByName(slaveDeviceName).get();

                        currentNodeLevel.add(slaveDevice);

                        addComPathSegmentsAndNeighbor(masterDevice, slaveDevice, currentLevel);
                        graphLayerBuilders.stream().forEach((gb) -> gb.buildLayer(slaveDevice));
                    }
                }
            }

        }
    }

    private String createBeaconDevice(DeviceConfiguration configuration, String deviceName, String serialNumber, DeviceTypeTpl deviceTypeTpl) {
        CreateBeaconDeviceCommand createDeviceCommand = this.createBeaconDeviceCommandProvider.get();
        createDeviceCommand.setDeviceTypeTpl(deviceTypeTpl);
        createDeviceCommand.setDeviceConfiguration(configuration);
        createDeviceCommand.setDeviceName(deviceName);
        createDeviceCommand.setSerialNumber(serialNumber);
        createDeviceCommand.setHost(this.host);
        createDeviceCommand.withLocation();
        return createDeviceCommand.run();
    }

    private String createBeaconSlaveDevice(DeviceConfiguration configuration, String slaveDeviceName, String serialNumber, DeviceTypeTpl deviceType, String deviceName) {
        CreateHANDeviceCommand createDeviceCommand = this.createHANDeviceCommandProvider.get();
        createDeviceCommand.setDeviceTypeTpl(deviceType);
        createDeviceCommand.setDeviceConfiguration(configuration);
        createDeviceCommand.setDeviceName(slaveDeviceName);
        createDeviceCommand.setSerialNumber(serialNumber);
        createDeviceCommand.withComSchedule(ComScheduleTpl.DAILY_READ_ALL);
        createDeviceCommand.withSecurityAccesors(ImmutableMap.of("PSK", "730E84DC18DDD0B20DFD6E1E53705D96"));
        createDeviceCommand.linkTo(deviceName);
        return createDeviceCommand.run();
    }

    private String createSerial(long id, Device masterDevice) {
        return String.format("%1s-DC01:DC01:DC01:%2s", masterDevice.getName(), String.format("%04d", id));
    }

    private String createName(long id, Device masterDevice) {
        return String.format("%1s-DC01DC01DC01%2s", masterDevice.getName(), String.format("%04d", id));
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

    private void addComPathSegmentsAndNeighbor(Device masterDevice, Device device, int levelNbr) {
        List<Device> intermediateHops = new ArrayList<>();
        Optional<Device> deviceOnPreviousLevel = intermediateHop(levelNbr - 1);// random device on previous level
        if (deviceOnPreviousLevel.isPresent()) {
            intermediateHops.addAll(topologyService.getCommunicationPath(masterDevice, deviceOnPreviousLevel.get()).getIntermediateDevices());
            intermediateHops.add(deviceOnPreviousLevel.get());
        }
        TopologyService.G3CommunicationPathSegmentBuilder builder = topologyService.addCommunicationSegments();
        if(intermediateHops.size() > 0) {
            for (int i = 0; i < intermediateHops.size(); i++) {
                Device hop = intermediateHops.get(i);
                System.out.println(String.format("hop with id %d", hop.getId()));
                builder.add(masterDevice, device, hop, Duration.ofDays(14), new Random().nextInt(100));
                builder.add(hop, device, null, Duration.ofDays(14), new Random().nextInt(100));
                builder.complete().forEach(this::addNeighbors);
                builder = topologyService.addCommunicationSegments();
            }
        }else {
            builder.add(masterDevice, device, null, Duration.ofDays(14), new Random().nextInt(100));
            builder.complete();
        }
    }

    private List<G3Neighbor> addNeighbors(G3CommunicationPathSegment segment) {
        TopologyService.G3NeighborhoodBuilder neighborhoodBuilder = topologyService.buildG3Neighborhood(segment.getSource());
        neighborhoodBuilder.addNeighbor(segment.getNextHopDevice().orElse(segment.getTarget()), ModulationScheme.COHERENT, Modulation.fromId(99), PhaseInfo.NOPHASEINFO, G3NodeState.UNKNOWN)
                .linkQualityIndicator(segment.getCost());
        return neighborhoodBuilder.complete();
    }

    private Optional<Device> intermediateHop(int level) {
        if (level <= 0) {
            return Optional.empty();
        }
        return Optional.of(levels[level].devices.get(new Random().nextInt(levels[level].devices.size())));
    }
}
