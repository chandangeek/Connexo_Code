package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.imp.Batch;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.rest.impl.DeviceApplication;
import com.energyict.mdc.device.data.rest.impl.DeviceEstimationStatusInfo;
import com.energyict.mdc.device.data.rest.impl.DeviceInfo;
import com.energyict.mdc.device.data.rest.impl.DeviceTopologyInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleStateInfo;
import com.energyict.mdc.device.topology.TopologyService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


@Component(name="device.info.factory", service = { InfoFactory.class }, immediate = true)
public class DeviceInfoFactory implements InfoFactory<Device> {
    private volatile Thesaurus thesaurus;
    private volatile DeviceImportService deviceImportService;
    private volatile TopologyService topologyService;
    private volatile IssueService issueService;

    public DeviceInfoFactory() {
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceApplication.COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setDeviceImportService(DeviceImportService deviceImportService) {
        this.deviceImportService = deviceImportService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Inject
    public DeviceInfoFactory(Thesaurus thesaurus, DeviceImportService deviceImportService, TopologyService topologyService, IssueService issueService) {
        this.thesaurus = thesaurus;
        setDeviceImportService(deviceImportService);
        setTopologyService(topologyService);
        setIssueService(issueService);
    }

    public List<DeviceInfo> from(List<Device> devices) {
        return devices.stream().map(this::from).collect(Collectors.toList());
    }

    @Override
    public DeviceInfo from(Device device){
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.id = device.getId();
        deviceInfo.mRID = device.getmRID();
        deviceInfo.serialNumber = device.getSerialNumber();
        deviceInfo.deviceTypeId = device.getDeviceType().getId();
        deviceInfo.deviceTypeName = device.getDeviceType().getName();
        deviceInfo.deviceConfigurationId = device.getDeviceConfiguration().getId();
        deviceInfo.deviceConfigurationName = device.getDeviceConfiguration().getName();
        deviceInfo.deviceProtocolPluggeableClassId = device.getDeviceType().getDeviceProtocolPluggableClass().getId();
        deviceInfo.yearOfCertification = device.getYearOfCertification();
        return deviceInfo;
    }

    @Override
    public List<PropertyDescriptionInfo> infoStructure() {
        List<PropertyDescriptionInfo> infos = new ArrayList<>();
        infos.add(createDescription("id", Long.class));
        infos.add(createDescription("mRID", String.class));
        infos.add(createDescription("serialNumber", String.class));
        infos.add(new DeviceTypeProperty());
        infos.add(new DeviceConfigProperty());
        infos.add(createDescription("deviceProtocolPluggeableClassId", Long.class));
        infos.add(createDescription("yearOfCertification", Long.class));
        return infos;
    }

    class DeviceTypeProperty extends PropertyDescriptionInfo {
        public Object id;

        public DeviceTypeProperty() {
            super("deviceTypeName", DeviceType.class, thesaurus.getString("deviceTypeName", "Device type"));
            this.id = new SubId();
            ((SubId)this.id).deviceType = "deviceTypeId";
        }

        class SubId {
            public String deviceType;
        }
    }

    class DeviceConfigProperty extends PropertyDescriptionInfo {
        public Object id;

        public DeviceConfigProperty() {
            super("deviceConfigurationName", DeviceConfiguration.class, thesaurus.getString("deviceConfigurationName", "Device configuration"));
            this.id = new SubId();
            ((SubId)this.id).deviceType = "deviceTypeId";
            ((SubId)this.id).deviceConfig = "deviceConfigurationId";
        }

        class SubId {
            public String deviceType;
            public String deviceConfig;
        }
    }


    private PropertyDescriptionInfo createDescription(String propertyName, Class<?> aClass) {
        return new PropertyDescriptionInfo(propertyName, aClass, thesaurus.getString(propertyName, propertyName));
    }

    public DeviceInfo from(Device device, List<DeviceTopologyInfo> slaveDevices){
        DeviceInfo deviceInfo = from(device);

        Optional<Batch> optionalBatch = deviceImportService.findBatch(device.getId());
        if (optionalBatch.isPresent()) {
            deviceInfo.batch = optionalBatch.get().getName();
        }
        Optional<Device> physicalGateway = topologyService.getPhysicalGateway(device);
        if (physicalGateway.isPresent()) {
            deviceInfo.masterDeviceId = physicalGateway.get().getId();
            deviceInfo.masterDevicemRID = physicalGateway.get().getmRID();
        }

        deviceInfo.gatewayType = device.getConfigurationGatewayType();
        deviceInfo.slaveDevices = slaveDevices;
        deviceInfo.nbrOfDataCollectionIssues = issueService.countOpenDataCollectionIssues(device.getmRID());
        deviceInfo.hasLoadProfiles = !device.getLoadProfiles().isEmpty();
        deviceInfo.hasLogBooks = !device.getLogBooks().isEmpty();
        deviceInfo.hasRegisters = !device.getRegisters().isEmpty();
        deviceInfo.isDirectlyAddressed = device.getDeviceConfiguration().isDirectlyAddressable();
        deviceInfo.isGateway = device.getDeviceConfiguration().canActAsGateway();
        Optional<? extends MeterActivation> meterActivation = device.getCurrentMeterActivation();
        if (meterActivation.isPresent()) {
            meterActivation.map(MeterActivation::getUsagePoint)
                    .ifPresent(up ->
                            up.ifPresent(usagePoint -> {
                                deviceInfo.usagePoint = usagePoint.getMRID();
                                deviceInfo.serviceCategory = usagePoint.getServiceCategory().getName();
                            }));
        }
        deviceInfo.estimationStatus = new DeviceEstimationStatusInfo(device);
        deviceInfo.state = new DeviceLifeCycleStateInfo(thesaurus, device.getState());
        deviceInfo.version = device.getVersion();
        return deviceInfo;
    }

    @Override
    public Class<Device> getDomainClass() {
        return Device.class;
    }

}
