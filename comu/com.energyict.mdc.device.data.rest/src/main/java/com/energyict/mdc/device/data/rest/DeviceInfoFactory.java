package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.rest.impl.DeviceInfo;
import com.energyict.mdc.device.data.rest.impl.DeviceTopologyInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleStateFactory;
import com.energyict.mdc.device.topology.TopologyService;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeviceInfoFactory {

    private final DeviceLifeCycleStateFactory deviceLifeCycleStateFactory;

    @Inject
    public DeviceInfoFactory(DeviceLifeCycleStateFactory deviceLifeCycleStateFactory) {
        this.deviceLifeCycleStateFactory = deviceLifeCycleStateFactory;
    }

    public List<DeviceInfo> from(List<Device> devices) {
        return devices.stream().map(this::from).collect(Collectors.toList());
    }

    public DeviceInfo from(Device device){
        Objects.requireNonNull(device);
        DeviceInfo deviceInfo = DeviceInfo.from(device);
        return setState(deviceInfo, device);
    }

    public DeviceInfo from(Device device, List<DeviceTopologyInfo> slaveDevices, DeviceImportService deviceImportService, TopologyService topologyService, IssueService issueService, MeteringService meteringService){
        DeviceInfo deviceInfo = DeviceInfo.from(device, slaveDevices, deviceImportService, topologyService, issueService, meteringService);
        return setState(deviceInfo, device);
    }

    private DeviceInfo setState(DeviceInfo deviceInfo, Device device) {
        Objects.requireNonNull(deviceInfo);
        Objects.requireNonNull(device);
        if(device.getCurrentMeterActivation().isPresent()) {
            Optional<Meter> meter = device.getCurrentMeterActivation().get().getMeter();

            if (meter.isPresent() && meter.get().getState().isPresent()) {
                deviceInfo.state = deviceLifeCycleStateFactory.from(meter.get().getState().get());
            }
        }
        return deviceInfo;
    }
}
