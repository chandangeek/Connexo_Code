/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
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
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    public DeviceInfoFactory() {
    }

    @Inject
    public DeviceInfoFactory(Thesaurus thesaurus, BatchService batchService, TopologyService topologyService, IssueService issueService, DataLoggerSlaveDeviceInfoFactory dataLoggerSlaveDeviceInfoFactory, DeviceService deviceService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, Clock clock) {
        this();
        this.thesaurus = thesaurus;
        this.setBatchService(batchService);
        this.setTopologyService(topologyService);
        this.setIssueService(issueService);
        this.dataLoggerSlaveDeviceInfoFactory = dataLoggerSlaveDeviceInfoFactory;
        this.setDeviceService(deviceService);
        this.clock = clock;
        this.setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
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

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    public List<DeviceInfo> fromDevices(List<Device> devices) {
        return devices.stream().map(this::deviceInfo).collect(Collectors.toList());
    }

    public DeviceInfo deviceInfo(Device device) {
        return from(device, Collections.emptyList());
    }

    @Override
    public DeviceSearchInfo from(Device device) {
        return DeviceSearchInfo
                .from(
                    device,
                    new GatewayRetriever(topologyService),
                    new IssueRetriever(issueService),
                    this.thesaurus,
                    this.deviceLifeCycleConfigurationService,
                    new DeviceValidationRetriever(deviceService));
    }

    @Override
    public List<Object> from(List<Device> domainObjects) {
        GatewayRetriever topologyService = new GatewayRetriever(this.topologyService, domainObjects);
        IssueRetriever issueRetriever = new IssueRetriever(issueService, domainObjects);
        DeviceValidationRetriever validationRetriever = new DeviceValidationRetriever(deviceService, domainObjects);
        return domainObjects.stream()
                .map(device -> DeviceSearchInfo.from(device, topologyService, issueRetriever, thesaurus, deviceLifeCycleConfigurationService, validationRetriever))
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
        return DeviceInfo.from(device, slaveDevices, topologyService, new IssueRetriever(issueService), deviceLifeCycleConfigurationService,
                dataLoggerSlaveDeviceInfoFactory, formattedLocation, spatialCoordinates.map(SpatialCoordinates::toString).orElse(null), clock);
    }

    @Override
    public Class<Device> getDomainClass() {
        return Device.class;
    }

    @Override
    public List<PropertyDescriptionInfo> modelStructure() {
        List<PropertyDescriptionInfo> infos = new ArrayList<>(21);
        infos.add(createDescription("batch", String.class));
        infos.add(createDescription("hasOpenDataCollectionIssues", Boolean.class));
        infos.add(createDescription("usagePoint", String.class));
        infos.add(createDescription("yearOfCertification", Integer.class));
        infos.add(createDescription("estimationActive", String.class));
        infos.add(createDescription("masterDeviceName", String.class));
        infos.add(createDescription("shipmentDate", Instant.class));
        infos.add(createDescription("installationDate", Instant.class));
        infos.add(createDescription("deactivationDate", Instant.class));
        infos.add(createDescription("decommissionDate", Instant.class));
        infos.add(createDescription("validationActive", String.class));
        infos.add(createDescription("hasOpenDataValidationIssues", Boolean.class));
        infos.add(createDescription("manufacturer", String.class));
        infos.add(createDescription("modelNbr", String.class));
        infos.add(createDescription("modelVersion", String.class));
        Collections.sort(infos, Comparator.comparing(pdi -> pdi.propertyName));

        // Default columns in proper order
        infos.add(0, createDescription("location", String.class));
        infos.add(0, new PropertyDescriptionInfo("state", String.class, thesaurus.getFormat(DeviceSearchModelTranslationKeys.STATE).format()));
        infos.add(0, createDescription("deviceConfigurationName", DeviceConfiguration.class));
        infos.add(0, createDescription("deviceTypeName", DeviceType.class));
        infos.add(0, createDescription("serialNumber", String.class));
        infos.add(0, createDescription("name", String.class));
        return infos;
    }

    private PropertyDescriptionInfo createDescription(String propertyName, Class<?> aClass) {
        return new PropertyDescriptionInfo(
                propertyName,
                aClass,
                thesaurus.getFormat(new SimpleTranslationKey(DeviceSearchModelTranslationKeys.Keys.PREFIX + propertyName, propertyName)).format());
    }

}
