package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.configuration.rest.GatewayTypeAdapter;
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleStateInfo;
import com.energyict.mdc.device.topology.TopologyService;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceInfo extends DeviceVersionInfo {
    public long id;
    public String serialNumber;
    public String deviceTypeName;
    public long deviceTypeId;
    public String deviceConfigurationName;
    public long deviceConfigurationId;
    public Long deviceProtocolPluggeableClassId;
    public Integer yearOfCertification;
    public String batch;
    public String masterDevicemRID;
    public Long masterDeviceId;
    public List<DeviceTopologyInfo> slaveDevices;
    public int nbrOfDataCollectionIssues;
    public Long openDataValidationIssue;
    @XmlJavaTypeAdapter(GatewayTypeAdapter.class)
    public GatewayType gatewayType;
    public Boolean hasRegisters;
    public Boolean hasLogBooks;
    public Boolean hasLoadProfiles;
    public Boolean isDirectlyAddressed;
    public Boolean isGateway;
    public String serviceCategory;
    public String usagePoint;
    public DeviceEstimationStatusInfo estimationStatus;
    public DeviceLifeCycleStateInfo state;
    public String location;
    public LocationInfo locationInfo;
    public String geoCoordinates;
    public Instant shipmentDate;

    public DeviceInfo() {
    }

    public static DeviceInfo from(Device device, List<DeviceTopologyInfo> slaveDevices, BatchService batchService, TopologyService topologyService,
                                  IssueRetriever issueRetriever, Thesaurus thesaurus, String location, String geoCoordinates) {
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.id = device.getId();
        deviceInfo.mRID = device.getmRID();
        deviceInfo.serialNumber = device.getSerialNumber();
        deviceInfo.deviceTypeId = device.getDeviceType().getId();
        deviceInfo.deviceTypeName = device.getDeviceType().getName();
        deviceInfo.deviceConfigurationId = deviceConfiguration.getId();
        deviceInfo.deviceConfigurationName = deviceConfiguration.getName();
        deviceInfo.deviceProtocolPluggeableClassId = device.getDeviceType().getDeviceProtocolPluggableClass().getId();
        deviceInfo.yearOfCertification = device.getYearOfCertification();
        deviceInfo.batch = batchService.findBatch(device).map(Batch::getName).orElse(null);

        Optional<Device> physicalGateway = topologyService.getPhysicalGateway(device);
        if (physicalGateway.isPresent()) {
            deviceInfo.masterDeviceId = physicalGateway.get().getId();
            deviceInfo.masterDevicemRID = physicalGateway.get().getmRID();
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
        State deviceState = device.getState();
        deviceInfo.state = new DeviceLifeCycleStateInfo(thesaurus, null, deviceState);
        deviceInfo.version = device.getVersion();
        deviceInfo.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());
        if (geoCoordinates != null) {
            deviceInfo.geoCoordinates = geoCoordinates;
        }
        if (location != null) {
            deviceInfo.location = location;
        }
        deviceInfo.shipmentDate = device.getLifecycleDates().getReceivedDate().orElse(null);
        return deviceInfo;
    }

    public static DeviceInfo from(Device device, String location, String geoCoordinates) {
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.id = device.getId();
        deviceInfo.mRID = device.getmRID();
        deviceInfo.serialNumber = device.getSerialNumber();
        deviceInfo.deviceTypeId = device.getDeviceType().getId();
        deviceInfo.deviceTypeName = device.getDeviceType().getName();
        deviceInfo.deviceConfigurationId = deviceConfiguration.getId();
        deviceInfo.deviceConfigurationName = deviceConfiguration.getName();
        deviceInfo.version = device.getVersion();
        deviceInfo.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());
        deviceInfo.location = location;
        deviceInfo.geoCoordinates = geoCoordinates;
        deviceInfo.shipmentDate = device.getLifecycleDates().getReceivedDate().orElse(null);
        return deviceInfo;
    }

    public static DeviceInfo from(Device device) {
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.id = device.getId();
        deviceInfo.mRID = device.getmRID();
        deviceInfo.serialNumber = device.getSerialNumber();
        deviceInfo.deviceTypeId = device.getDeviceType().getId();
        deviceInfo.deviceTypeName = device.getDeviceType().getName();
        deviceInfo.deviceConfigurationId = deviceConfiguration.getId();
        deviceInfo.deviceConfigurationName = deviceConfiguration.getName();
        deviceInfo.version = device.getVersion();
        deviceInfo.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());
        deviceInfo.shipmentDate = device.getLifecycleDates().getReceivedDate().orElse(null);
        return deviceInfo;
    }
}