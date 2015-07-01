package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.rest.impl.DeviceInfo;
import com.energyict.mdc.device.data.rest.impl.DeviceTopologyInfo;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceInfoFactory {

    private final Thesaurus thesaurus;
    private final DeviceImportService deviceImportService;
    private final TopologyService topologyService;
    private final IssueService issueService;
    private final IssueDataValidationService issueDataValidationService;
    private final MeteringService meteringService;

    @Inject
    public DeviceInfoFactory(Thesaurus thesaurus, DeviceImportService deviceImportService, TopologyService topologyService, IssueService issueService, IssueDataValidationService issueDataValidationService, MeteringService meteringService) {
        this.thesaurus = thesaurus;
        this.deviceImportService = deviceImportService;
        this.topologyService = topologyService;
        this.issueService = issueService;
        this.issueDataValidationService = issueDataValidationService;
        this.meteringService = meteringService;
    }

    public List<DeviceInfo> from(List<Device> devices) {
        return devices.stream().map(this::from).collect(Collectors.toList());
    }

    public DeviceInfo from(Device device){
        return DeviceInfo.from(device);
    }

    public DeviceInfo from(Device device, List<DeviceTopologyInfo> slaveDevices){
        return DeviceInfo.from(device, slaveDevices, deviceImportService, topologyService, issueService, issueDataValidationService, meteringService, thesaurus);
    }

}
