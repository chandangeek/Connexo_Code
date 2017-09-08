package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.rest.demo.NetworkTopologyBuilder;
import com.energyict.mdc.device.topology.rest.demo.layer.CommunicationStatusLayerBuilder;
import com.energyict.mdc.device.topology.rest.demo.layer.DeviceLifeCycleStatusGraphLayerBuilder;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.scheduling.SchedulingService;


import com.google.inject.Injector;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 10/01/2017
 * Time: 13:05
 */
public class CreateNetworkTopologyCommand  extends CommandWithTransaction{

    private final Injector injector;
    private final ThreadPrincipalService threadPrincipalService;
    private final TransactionService transactionService;
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

    String gatewayMrid;
    Integer deviceCount;
    Integer levelCount;

    @Inject
    public  CreateNetworkTopologyCommand(Injector injector, ThreadPrincipalService threadPrincipalService, TransactionService transactionService, TopologyService topologyService, DeviceService deviceService, DeviceConfigurationService deviceConfigurationService, DeviceLifeCycleService deviceLifeCycleService, EngineConfigurationService engineConfigurationService, SchedulingService schedulingService, ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService, IssueService issueService, IssueCreationService issueCreationService, Clock clock){
        this.injector = injector;
        this.threadPrincipalService = threadPrincipalService;
        this.transactionService = transactionService;
        this.topologyService = topologyService;
        this.deviceService = deviceService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.engineConfigurationService = engineConfigurationService;
        this.schedulingService = schedulingService;
        this.connectionTaskService  = connectionTaskService;
        this.communicationTaskService = communicationTaskService;
        this.issueService = issueService;
        this.issueCreationService = issueCreationService;
        this.clock = clock;
    }

    public void setGatewayMrid(String gatewayMrid) {
        this.gatewayMrid = gatewayMrid;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }

    public void setLevelCount(int levelCount) {
        this.levelCount = levelCount;
    }

    @Override
    public void run() {
        if (this.gatewayMrid == null || this.deviceCount == null || this.levelCount == null){
            System.out.println("createNetworkTopology <name of gateway> <number of childnodes> <number of levels>");
        }else {
            System.out.println(String.format("Building topology with %1d nodes having %2d levels", this.deviceCount, this.levelCount));
            Optional<Device> gateway = deviceService.findDeviceByName(this.gatewayMrid);
            if (!gateway.isPresent()) {
                throw new RuntimeException(String.format("No device with name %s", gatewayMrid));
            }
            new NetworkTopologyBuilder(threadPrincipalService, transactionService, deviceService, topologyService, deviceConfigurationService, clock)
                    .havingNodes(deviceCount)
                    .havingLevels(levelCount)
                    .havingGraphLayerBuilder(new DeviceLifeCycleStatusGraphLayerBuilder(deviceLifeCycleService))
                    .havingGraphLayerBuilder(new CommunicationStatusLayerBuilder(engineConfigurationService, schedulingService, connectionTaskService, communicationTaskService, clock))
                    .buildTopology(gateway.get());
        }

    }
}
