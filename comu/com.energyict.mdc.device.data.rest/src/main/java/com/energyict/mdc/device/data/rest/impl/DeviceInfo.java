package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
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
import com.energyict.mdc.issue.datavalidation.DataValidationIssueFilter;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
    public Boolean isDataLogger;
    public Boolean isDataLoggerSlave;
    public String serviceCategory;
    public String usagePoint;
    public DeviceEstimationStatusInfo estimationStatus;
    public DeviceLifeCycleStateInfo state;
    public List<DataLoggerSlaveDeviceInfo> dataLoggerSlaveDevices;

    public DeviceInfo() {
    }

    public static DeviceInfo from(Device device, List<DeviceTopologyInfo> slaveDevices, BatchService batchService, TopologyService topologyService, IssueService issueService, IssueDataValidationService issueDataValidationService, MeteringService meteringService, Thesaurus thesaurus, DataLoggerSlaveDeviceInfoFactory dataLoggerSlaveDeviceInfoFactory) {
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.id = device.getId();
        deviceInfo.mRID = device.getmRID();
        deviceInfo.serialNumber = device.getSerialNumber();
        deviceInfo.deviceTypeId = device.getDeviceType().getId();
        deviceInfo.deviceTypeName = device.getDeviceType().getName();
        deviceInfo.deviceConfigurationId = deviceConfiguration.getId();
        deviceInfo.deviceConfigurationName = deviceConfiguration.getName();
        deviceInfo.deviceProtocolPluggeableClassId = device.getDeviceType().getDeviceProtocolPluggableClass()!=null
            ? device.getDeviceType().getDeviceProtocolPluggableClass().getId() : 0;
        deviceInfo.yearOfCertification = device.getYearOfCertification();
        deviceInfo.batch = batchService.findBatch(device).map(Batch::getName).orElse(null);

        Optional<Device> physicalGateway = topologyService.getPhysicalGateway(device);
        if (physicalGateway.isPresent()) {
            deviceInfo.masterDeviceId = physicalGateway.get().getId();
            deviceInfo.masterDevicemRID = physicalGateway.get().getmRID();
        }

        deviceInfo.gatewayType = device.getConfigurationGatewayType();
        deviceInfo.slaveDevices = slaveDevices;
        deviceInfo.nbrOfDataCollectionIssues = issueService.countOpenDataCollectionIssues(device.getmRID());
        deviceInfo.openDataValidationIssue = getOpenDataValidationIssue(device, meteringService, issueService, issueDataValidationService).map(Issue::getId).orElse(null);
        deviceInfo.hasLoadProfiles = !device.getLoadProfiles().isEmpty();
        deviceInfo.hasLogBooks = !device.getLogBooks().isEmpty();
        deviceInfo.hasRegisters = !device.getRegisters().isEmpty();
        deviceInfo.isDirectlyAddressed = deviceConfiguration.isDirectlyAddressable();
        deviceInfo.isGateway = deviceConfiguration.canActAsGateway();
        deviceInfo.isDataLogger = deviceConfiguration.isDataloggerEnabled();
        deviceInfo.isDataLoggerSlave = device.getDeviceType().isDataloggerSlave();
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
        deviceInfo.dataLoggerSlaveDevices = dataLoggerSlaveDeviceInfoFactory.from(device);
        return deviceInfo;
    }

    static Optional<? extends IssueDataValidation> getOpenDataValidationIssue(Device device, MeteringService meteringService, IssueService issueService, IssueDataValidationService issueDataValidationService) {
        Optional<AmrSystem> amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId());
        Optional<Meter> meter = amrSystem.get().findMeter(String.valueOf(device.getId()));
        DataValidationIssueFilter filter = new DataValidationIssueFilter();
        filter.setDevice(meter.get());
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        return issueDataValidationService.findAllDataValidationIssues(filter).stream().findFirst();
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
        return deviceInfo;
    }
}