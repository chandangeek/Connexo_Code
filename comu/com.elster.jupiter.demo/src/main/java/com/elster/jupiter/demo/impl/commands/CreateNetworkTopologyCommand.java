package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.rest.demo.NetworkTopologyBuilder;


import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 10/01/2017
 * Time: 13:05
 */
public class CreateNetworkTopologyCommand  extends CommandWithTransaction{

    private final ThreadPrincipalService threadPrincipalService;
    private final TransactionService transactionService;
    private final TopologyService topologyService;
    private final DeviceService deviceService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final Clock clock;

    String gatewayMrid;
    Integer deviceCount;
    Integer levelCount;

    @Inject
    public  CreateNetworkTopologyCommand(ThreadPrincipalService threadPrincipalService, TransactionService transactionService, TopologyService topologyService, DeviceService deviceService, DeviceConfigurationService deviceConfigurationService, Clock clock){
        this.threadPrincipalService = threadPrincipalService;
        this.transactionService = transactionService;
        this.topologyService = topologyService;
        this.deviceService = deviceService;
        this.deviceConfigurationService = deviceConfigurationService;
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
                    .buildTopology(gateway.get());
        }
    }
}
