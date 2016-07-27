package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = "device.info.factory", service = {InfoFactory.class}, immediate = true)
public class DeviceInfoFactory implements InfoFactory<Device> {

    private volatile Thesaurus thesaurus;
    private volatile BatchService batchService;
    private volatile TopologyService topologyService;
    private volatile IssueService issueService;
    private volatile DeviceService deviceService;
    private volatile Clock clock;
    private volatile DataLoggerSlaveDeviceInfoFactory dataLoggerSlaveDeviceInfoFactory;


    public DeviceInfoFactory() {
    }

    @Inject
    public DeviceInfoFactory(Thesaurus thesaurus, BatchService batchService, TopologyService topologyService, IssueService issueService, DataLoggerSlaveDeviceInfoFactory dataLoggerSlaveDeviceInfoFactory, DeviceService deviceService, Clock clock) {
        this.thesaurus = thesaurus;
        this.batchService = batchService;
        this.topologyService = topologyService;
        this.issueService = issueService;
        this.dataLoggerSlaveDeviceInfoFactory = dataLoggerSlaveDeviceInfoFactory;
        this.deviceService = deviceService;
        this.clock = clock;
    }

    @Reference
    public void setBatchService(BatchService batchService) {
        this.batchService = batchService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceApplication.COMPONENT_NAME, Layer.REST)
                .join(nlsService.getThesaurus("DLR", Layer.REST));
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    public List<DeviceInfo> fromDevices(List<Device> devices) {
        return devices.stream().map(this::deviceInfo).collect(Collectors.toList());
    }

    public DeviceInfo deviceInfo(Device device) {
        return from(device, Collections.emptyList());
    }

    @Override
    public DeviceSearchInfo from(Device device) {
        return DeviceSearchInfo.from(device, new BatchRetriever(batchService), new GatewayRetriever(topologyService), new IssueRetriever(issueService),
                thesaurus, new DeviceEstimationRetriever(deviceService), new DeviceValidationRetriever(deviceService));
    }

    @Override
    public List<Object> from(List<Device> domainObjects) {
        BatchRetriever batchService = new BatchRetriever(this.batchService, domainObjects);
        GatewayRetriever topologyService = new GatewayRetriever(this.topologyService, domainObjects);
        IssueRetriever issueRetriever = new IssueRetriever(issueService, domainObjects);
        DeviceEstimationRetriever estimationRetrievers = new DeviceEstimationRetriever(deviceService, domainObjects);
        DeviceValidationRetriever validationRetriever = new DeviceValidationRetriever(deviceService, domainObjects);
        return domainObjects.stream()
                .map(device -> DeviceSearchInfo.from(device, batchService, topologyService, issueRetriever, thesaurus, estimationRetrievers, validationRetriever))
                .collect(Collectors.toList());
    }

    public DeviceInfo from(Device device, List<DeviceTopologyInfo> slaveDevices) {
        Optional<Location> location = device.getLocation();
        Optional<SpatialCoordinates> spatialCoordinates = device.getSpatialCoordinates();
        String formattedLocation = "";
        if (location.isPresent()) {
            List<List<String>> formattedLocationMembers = location.get().format();
            formattedLocationMembers.stream().skip(1).forEach(list ->
                    list.stream().filter(Objects::nonNull).findFirst().ifPresent(member -> list.set(list.indexOf(member), "\\r\\n" + member)));
            formattedLocation = formattedLocationMembers.stream()
                    .flatMap(List::stream).filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
        }
        return DeviceInfo.from(device, slaveDevices, batchService, topologyService, new IssueRetriever(issueService), thesaurus,
                dataLoggerSlaveDeviceInfoFactory, formattedLocation, spatialCoordinates.map(coord -> coord.toString()).orElse(null), clock);
    }

    @Override
    public Class<Device> getDomainClass() {
        return Device.class;
    }

    @Override
    public List<PropertyDescriptionInfo> modelStructure() {
        List<PropertyDescriptionInfo> infos = new ArrayList<>(23);
        infos.add(createDescription("batch", String.class));
        infos.add(createDescription("deviceTypeId", Long.class));
        infos.add(createDescription("deviceConfigurationId", Long.class));
        infos.add(createDescription("hasOpenDataCollectionIssues", Boolean.class));
        infos.add(createDescription("serviceCategory", String.class));
        infos.add(createDescription("usagePoint", String.class));
        infos.add(createDescription("yearOfCertification", Integer.class));
        infos.add(createDescription("estimationActive", Boolean.class));
        infos.add(createDescription("masterDevicemRID", String.class));
        infos.add(createDescription("shipmentDate", Instant.class));
        infos.add(createDescription("installationDate", Instant.class));
        infos.add(createDescription("deactivationDate", Instant.class));
        infos.add(createDescription("decommissionDate", Instant.class));
        infos.add(createDescription("validationActive", Boolean.class));
        infos.add(createDescription("hasOpenDataValidationIssues", Boolean.class));
        Collections.sort(infos, Comparator.comparing(pdi -> pdi.propertyName));

        // Default columns in proper order
        infos.add(0, createDescription("location", String.class));
        infos.add(0, new PropertyDescriptionInfo("state", String.class, thesaurus.getFormat(DeviceSearchModelTranslationKeys.STATE).format()));
        infos.add(0, createDescription("deviceConfigurationName", String.class));
        infos.add(0, createDescription("deviceTypeName", String.class));
        infos.add(0, createDescription("serialNumber", String.class));
        infos.add(0, createDescription("mRID", String.class));
        infos.add(0, createDescription("location", String.class));
        return infos;
    }

    private PropertyDescriptionInfo createDescription(String propertyName, Class<?> aClass) {
        return new PropertyDescriptionInfo(propertyName, aClass, thesaurus.getString(DeviceSearchModelTranslationKeys.Keys.PREFIX + propertyName, propertyName));
    }

}
