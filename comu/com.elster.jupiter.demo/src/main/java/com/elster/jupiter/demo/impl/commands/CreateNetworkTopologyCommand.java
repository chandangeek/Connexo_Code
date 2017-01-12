package com.elster.jupiter.demo.impl.commands;

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

    private final TopologyService topologyService;
    private final DeviceService deviceService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final Clock clock;

    String gatewayMrid;
    int deviceCount;
    int levelCount;

    @Inject
    public  CreateNetworkTopologyCommand(TopologyService topologyService, DeviceService deviceService, DeviceConfigurationService deviceConfigurationService, Clock clock){
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
        System.out.println(String.format("Building topology with %d1 nodes having %d2 levels", this.deviceCount, this.levelCount));
        Optional<Device> gateway = deviceService.findDeviceByName(this.gatewayMrid);
        if (!gateway.isPresent()){
            throw new RuntimeException(String.format("No device with name %s", gatewayMrid));
        }

        new NetworkTopologyBuilder(deviceService, topologyService, deviceConfigurationService, clock).havingNodes(deviceCount).havingLevels(levelCount).buildTopology(gateway.get());
    }
}
