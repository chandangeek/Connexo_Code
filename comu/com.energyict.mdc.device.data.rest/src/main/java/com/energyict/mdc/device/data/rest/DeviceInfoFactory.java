package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
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

    private final Thesaurus thesaurus;
    private final  DeviceImportService deviceImportService;
    private final TopologyService topologyService;
    private final IssueService issueService;
    private final MeteringService meteringService;

    @Inject
    public DeviceInfoFactory(Thesaurus thesaurus, DeviceImportService deviceImportService, TopologyService topologyService, IssueService issueService, MeteringService meteringService) {
        this.thesaurus = thesaurus;
        this.deviceImportService = deviceImportService;
        this.topologyService = topologyService;
        this.issueService = issueService;
        this.meteringService = meteringService;
    }

    public List<DeviceInfo> from(List<Device> devices) {
        return devices.stream().map(this::from).collect(Collectors.toList());
    }

    public DeviceInfo from(Device device){
        return DeviceInfo.from(device);
    }

    public DeviceInfo from(Device device, List<DeviceTopologyInfo> slaveDevices){
        return DeviceInfo.from(device, slaveDevices, deviceImportService, topologyService, issueService, meteringService, thesaurus);
    }

}
