package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.InfoFactory;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.rest.impl.DeviceInfo;
import com.energyict.mdc.device.data.rest.impl.DeviceTopologyInfo;
import com.energyict.mdc.device.topology.TopologyService;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.osgi.service.component.annotations.Component;


@Component(name="device.info.factory", service = { InfoFactory.class, Device.class }, immediate = true)
public class DeviceInfoFactory implements InfoFactory<Device> {
    private final Thesaurus thesaurus;
    private final  DeviceImportService deviceImportService;
    private final TopologyService topologyService;
    private final IssueService issueService;

    @Inject
    public DeviceInfoFactory(Thesaurus thesaurus, DeviceImportService deviceImportService, TopologyService topologyService, IssueService issueService) {
        this.thesaurus = thesaurus;
        this.deviceImportService = deviceImportService;
        this.topologyService = topologyService;
        this.issueService = issueService;
    }

    public List<DeviceInfo> from(List<Device> devices) {
        return devices.stream().map(this::from).collect(Collectors.toList());
    }

    @Override
    public DeviceInfo from(Device device){
        return DeviceInfo.from(device);
    }

    public DeviceInfo from(Device device, List<DeviceTopologyInfo> slaveDevices){
        return DeviceInfo.from(device, slaveDevices, deviceImportService, topologyService, issueService, thesaurus);
    }

//    @Override
//    public Object from(Device domainObject) {
//        if (Device.class.isAssignableFrom(domainObject.getClass())) {
//            return this.from((Device)domainObject);
//        } else {
//            throw new IllegalArgumentException("Can only convert Devices");
//        }
//    }

    @Override
    public Class<Device> getDomainClass() {
        return Device.class;
    }
}
