/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.gogo;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.util.Optional;

@Component(name = "com.energyict.mdc.gogo.NetworkTopologyCommands", service = NetworkTopologyCommands.class,
        property = {"osgi.command.scope=mdc.device.topology",
                "osgi.command.function=help",
                "osgi.command.function=topology"},
        immediate = true)
public class NetworkTopologyCommands {

    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile TopologyService topologyService;
    private volatile DeviceService deviceService;
    private volatile Clock clock;

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @SuppressWarnings("unused")
    public void help() {
        System.out.println("topology <existing gateway name> <nbr of slaves> <nbr of levels>");
    }

    public void topology(String gatewayName, int deviceCount, int levelCount){
        System.out.println(String.format("Building topology with %1d nodes having %2d levels", deviceCount, levelCount));
        Optional<Device> gateway = deviceService.findDeviceByName(gatewayName);
        if (!gateway.isPresent()){
            throw new RuntimeException(String.format("No device with name '%s'", gatewayName));
        }

        new NetworkTopologyBuilder(threadPrincipalService, transactionService, deviceService, topologyService, deviceConfigurationService, clock).havingNodes(deviceCount).havingLevels(levelCount).buildTopology(gateway.get());
        System.out.println(String.format("All slaves and topology data for device %s created", gatewayName));
    }
}
