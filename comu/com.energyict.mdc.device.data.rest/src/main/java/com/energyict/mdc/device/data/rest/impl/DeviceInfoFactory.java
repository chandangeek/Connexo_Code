/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;
import com.elster.jupiter.search.rest.InfoFactoryService;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.multielement.MultiElementDeviceService;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.firmware.FirmwareService;

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
    private volatile MultiElementDeviceService multiElementDeviceService;
    private volatile TopologyService topologyService;
    private volatile IssueService issueService;
    private volatile DeviceService deviceService;
    private volatile Clock clock;
    private volatile DataLoggerSlaveDeviceInfoFactory dataLoggerSlaveDeviceInfoFactory;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile FirmwareService firmwareService;
    private volatile MeteringZoneService meteringZoneService;
    private volatile MeteringService meteringService;
    private volatile EndDeviceZoneInfoFactory endDeviceZoneInfoFactory;
    private volatile MeteringTranslationService meteringTranslationService;

    public DeviceInfoFactory() {
    }

    @Inject
    public DeviceInfoFactory(Thesaurus thesaurus, BatchService batchService, TopologyService topologyService,
                             MeteringZoneService meteringZoneService, MeteringService meteringService, EndDeviceZoneInfoFactory endDeviceZoneInfoFactory,
                             MultiElementDeviceService multiElementDeviceService,
                             IssueService issueService, DataLoggerSlaveDeviceInfoFactory dataLoggerSlaveDeviceInfoFactory, DeviceService deviceService,
                             DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, FirmwareService firmwareService, Clock clock,
                             MeteringTranslationService meteringTranslationService) {
        this();
        this.thesaurus = thesaurus;
        this.setBatchService(batchService);
        this.setTopologyService(topologyService);
        this.setMultiElementDeviceService(multiElementDeviceService);
        this.setIssueService(issueService);
        this.dataLoggerSlaveDeviceInfoFactory = dataLoggerSlaveDeviceInfoFactory;
        this.setDeviceService(deviceService);
        this.setMeteringZoneService(meteringZoneService);
        this.setMeteringService(meteringService);
        this.clock = clock;
        this.endDeviceZoneInfoFactory = endDeviceZoneInfoFactory;
        this.setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        this.setFirmwareService(firmwareService);
        this.setMeteringTranslationService(meteringTranslationService);
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
    public void setMultiElementDeviceService(MultiElementDeviceService multiElementDeviceService) {
        this.multiElementDeviceService = multiElementDeviceService;
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
    public void setMeteringZoneService(MeteringZoneService meteringZoneService) {
        this.meteringZoneService = meteringZoneService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setMeteringTranslationService(MeteringTranslationService meteringTranslationService) {
        this.meteringTranslationService = meteringTranslationService;
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    @Reference
    public void setInfoFactoryService(InfoFactoryService infoFactoryService) {
        // to make sure this factory starts after the whole service
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
                        this.meteringTranslationService,
                        new DeviceValidationRetriever(deviceService));
    }

    @Override
    public List<Object> from(List<Device> domainObjects) {
        GatewayRetriever topologyService = new GatewayRetriever(this.topologyService, domainObjects);
        IssueRetriever issueRetriever = new IssueRetriever(issueService, domainObjects);
        DeviceValidationRetriever validationRetriever = new DeviceValidationRetriever(deviceService, domainObjects);
        return domainObjects.stream()
                .map(device -> DeviceSearchInfo.from(device, topologyService, issueRetriever, thesaurus,meteringTranslationService, validationRetriever))
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
        List<EndDeviceZoneInfo> zones = meteringZoneService.getByEndDevice(
                meteringService.findEndDeviceByMRID(device.getmRID()).get())
                .stream().limit(5).map(deviceZones -> endDeviceZoneInfoFactory.from(deviceZones)).collect(Collectors.toList());
        DeviceInfo deviceInfo = DeviceInfo.from(device, slaveDevices, zones, topologyService, multiElementDeviceService, new IssueRetriever(issueService), deviceLifeCycleConfigurationService,
                dataLoggerSlaveDeviceInfoFactory, formattedLocation, spatialCoordinates.map(SpatialCoordinates::toString).orElse(null), clock, meteringTranslationService);

        Optional<FirmwareManagementOptions> firmwareMgtOptions = firmwareService.findFirmwareManagementOptions(device.getDeviceType());
        deviceInfo.isFirmwareManagementAllowed = !firmwareMgtOptions.map(FirmwareManagementOptions::getOptions).orElse(Collections.emptySet()).isEmpty();

        deviceInfo.protocolNeedsImageIdentifierForFirmwareUpgrade = firmwareService.imageIdentifierExpectedAtFirmwareUpload(device.getDeviceType());
        return deviceInfo;
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
        infos.add(createDescription("activeCalendar", String.class));
        infos.add(createDescription("passiveCalendar", String.class));
        infos.add(createDescription("plannedPassiveCalendar", String.class));
        infos.add(createDescription("hasServiceKeys", Boolean.class));
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
