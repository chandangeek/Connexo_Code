package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.configuration.rest.GatewayTypeAdapter;
import com.energyict.mdc.device.data.CIMLifecycleDates;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.imp.Batch;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleStateInfo;
import com.energyict.mdc.device.topology.TopologyService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceInfo {
    public long id;
    public String mRID;
    public String serialNumber;
    public String deviceTypeName;
    public Long deviceTypeId;
    public String deviceConfigurationName;
    public Long deviceConfigurationId;
    public Long deviceProtocolPluggeableClassId;
    public Integer yearOfCertification;
    public String batch;
    public String masterDevicemRID;
    public Long masterDeviceId;
    public List<DeviceTopologyInfo> slaveDevices;
    public long nbrOfDataCollectionIssues;
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
    public List<DeviceDateInfo> dates;
    public long version;

    public DeviceInfo() {
    }

    public static DeviceInfo from(Device device, List<DeviceTopologyInfo> slaveDevices, DeviceImportService deviceImportService, TopologyService topologyService, IssueService issueService, Thesaurus thesaurus) {
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
        State deviceState = device.getState();
        deviceInfo.state = new DeviceLifeCycleStateInfo(thesaurus, deviceState);
        deviceInfo.version = device.getVersion();
        CIMLifecycleDates lifecycleDates = device.getLifecycleDates();
        deviceInfo.dates = Arrays.stream(DeviceDateInfo.DateAttribute.values())
                .map(date -> {
                    DeviceDateInfo dateInfo = new DeviceDateInfo();
                    dateInfo.matchCurrentState = date.isMatchedCurrentState(deviceState);
                    dateInfo.name = date.getDateName();
                    dateInfo.timestamp = date.getValue(lifecycleDates);
                    return dateInfo;
                })
                .collect(Collectors.toList());

        return deviceInfo;
    }

    public static DeviceInfo from(Device device) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.id = device.getId();
        deviceInfo.mRID = device.getmRID();
        deviceInfo.serialNumber = device.getSerialNumber();
        deviceInfo.deviceTypeId = device.getDeviceType().getId();
        deviceInfo.deviceTypeName = device.getDeviceType().getName();
        deviceInfo.deviceConfigurationId = device.getDeviceConfiguration().getId();
        deviceInfo.deviceConfigurationName = device.getDeviceConfiguration().getName();
        return deviceInfo;
    }
}