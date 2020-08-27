/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.estimation.rest.EstimationStatusInfo;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.GatewayType;
import com.energyict.mdc.common.device.data.Batch;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.configuration.rest.GatewayTypeAdapter;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleStateInfo;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.multielement.MultiElementDeviceService;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceInfo extends DeviceVersionInfo {
    public String serialNumber;
    public String manufacturer;
    public String modelNbr;
    public String modelVersion;
    public String deviceTypeName;
    public long deviceTypeId;
    public String deviceConfigurationName;
    public long deviceConfigurationId;
    public Long deviceProtocolPluggeableClassId;
    public Integer yearOfCertification;
    public String batch;
    public String masterDeviceName;
    public Long masterDeviceId;
    public List<DeviceTopologyInfo> slaveDevices;
    public List<EndDeviceZoneInfo> zones;
    public int nbrOfDataCollectionIssues;
    public Long openDataValidationIssue;
    @XmlJavaTypeAdapter(GatewayTypeAdapter.class)
    public GatewayType gatewayType;
    public Boolean hasRegisters;
    public Boolean hasLogBooks;
    public Boolean hasLoadProfiles;
    public Boolean isDirectlyAddressed;
    public Boolean isGateway;
    public Boolean isDataLogger;
    public String dataloggerName; // only available when we are a dataloggerslave
    public Boolean isDataLoggerSlave;
    public Boolean isMultiElementDevice;
    public String multiElementDeviceName; // only available when we are a multi-element slave
    public Boolean isMultiElementSlave;
    public Boolean isFirmwareManagementAllowed;
    public String serviceCategory;
    public String usagePoint;
    public EstimationStatusInfo estimationStatus;
    public DeviceLifeCycleStateInfo state;
    public String location;
    public LocationInfo locationInfo;
    public String geoCoordinates;
    public Instant shipmentDate;
    public List<DataLoggerSlaveDeviceInfo> dataLoggerSlaveDevices;
    public Boolean hasValidationRules;
    public Boolean hasEstimationRules;
    public boolean protocolNeedsImageIdentifierForFirmwareUpgrade;

    public DeviceInfo() {
    }

    public static String getDeviceName(Device device) {
        return device.getName();
    }

    public static DeviceInfo from(Device device) {
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.id = device.getId();
        deviceInfo.version = device.getVersion();
        deviceInfo.name = device.getName();
        deviceInfo.mRID = device.getmRID();
        deviceInfo.serialNumber = device.getSerialNumber();
        deviceInfo.manufacturer = device.getManufacturer();
        deviceInfo.modelNbr = device.getModelNumber();
        deviceInfo.modelVersion = device.getModelVersion();
        deviceInfo.deviceTypeId = device.getDeviceType().getId();
        deviceInfo.deviceTypeName = device.getDeviceType().getName();
        deviceInfo.deviceConfigurationId = deviceConfiguration.getId();
        deviceInfo.deviceConfigurationName = deviceConfiguration.getName();
        deviceInfo.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());
        deviceInfo.shipmentDate = device.getLifecycleDates().getReceivedDate().orElse(null);
        deviceInfo.hasEstimationRules = !deviceConfiguration.getEstimationRuleSets().isEmpty();
        deviceInfo.hasValidationRules = !deviceConfiguration.getValidationRuleSets().isEmpty();
        return deviceInfo;
    }

    public static DeviceInfo from(Device device, String location, String geoCoordinates) {
        DeviceInfo deviceInfo = from(device);
        deviceInfo.location = location;
        deviceInfo.geoCoordinates = geoCoordinates;
        return deviceInfo;
    }

    public static DeviceInfo from(Device device, List<DeviceTopologyInfo> slaveDevices, List<EndDeviceZoneInfo> zones, TopologyService topologyService, MultiElementDeviceService multiElementDeviceService, IssueRetriever issueRetriever, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, DataLoggerSlaveDeviceInfoFactory dataLoggerSlaveDeviceInfoFactory, String location, String geoCoordinates, Clock clock, MeteringTranslationService meteringTranslationService) {
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        DeviceInfo deviceInfo = from(device, location, geoCoordinates);
        deviceInfo.deviceProtocolPluggeableClassId = device.getDeviceType().getDeviceProtocolPluggableClass().map(HasId::getId).orElse(0L);
        deviceInfo.yearOfCertification = device.getYearOfCertification();
        deviceInfo.batch = device.getBatch().map(Batch::getName).orElse(null);
        Optional<Device> physicalGateway = topologyService.getPhysicalGateway(device);
        if (physicalGateway.isPresent()) {
            deviceInfo.masterDeviceId = physicalGateway.get().getId();
            deviceInfo.masterDeviceName = physicalGateway.get().getName();
        }
        deviceInfo.gatewayType = device.getConfigurationGatewayType();
        deviceInfo.slaveDevices = slaveDevices;
        deviceInfo.nbrOfDataCollectionIssues = issueRetriever.numberOfDataCollectionIssues(device);
        deviceInfo.openDataValidationIssue = issueRetriever.getOpenDataValidationIssue(device).map(Entity::getId).orElse(null);
        deviceInfo.hasLoadProfiles = !device.getLoadProfiles().isEmpty();
        deviceInfo.hasLogBooks = !device.getLogBooks().isEmpty();
        deviceInfo.hasRegisters = !device.getRegisters().isEmpty();
        deviceInfo.isDirectlyAddressed = deviceConfiguration.isDirectlyAddressable();
        deviceInfo.isGateway = deviceConfiguration.canActAsGateway();
        deviceInfo.isDataLogger = deviceConfiguration.isDataloggerEnabled();
        deviceInfo.isDataLoggerSlave = device.getDeviceType().isDataloggerSlave();
        if (device.getDeviceType().isDataloggerSlave()) {
            topologyService.getDataLogger(device, clock.instant()).ifPresent(datalogger -> deviceInfo.dataloggerName = device.getName());
        }
        deviceInfo.zones = zones;
        deviceInfo.isMultiElementDevice = deviceConfiguration.isMultiElementEnabled();
        deviceInfo.isMultiElementSlave = device.getDeviceType().isMultiElementSlave();
        if (device.getDeviceType().isMultiElementSlave()) {
            multiElementDeviceService.getMultiElementDevice(device, clock.instant()).ifPresent(multiElementDevice -> deviceInfo.multiElementDeviceName = multiElementDevice.getName());
        }
        device.getUsagePoint().ifPresent(usagePoint -> {
            deviceInfo.usagePoint = usagePoint.getName();
            deviceInfo.serviceCategory = usagePoint.getServiceCategory().getName();
        });
        deviceInfo.estimationStatus = new EstimationStatusInfo(device.forEstimation().isEstimationActive());
        State deviceState = device.getState();
        deviceInfo.state = new DeviceLifeCycleStateInfo(deviceLifeCycleConfigurationService, null, deviceState, meteringTranslationService);
        deviceInfo.dataLoggerSlaveDevices = dataLoggerSlaveDeviceInfoFactory.from(device);

        return deviceInfo;
    }
}
