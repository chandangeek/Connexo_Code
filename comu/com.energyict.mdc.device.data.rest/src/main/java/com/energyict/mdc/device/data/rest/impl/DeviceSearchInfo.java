package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.CIMLifecycleDates;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleStateInfo;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;

import java.time.Instant;
import java.util.Optional;

public class DeviceSearchInfo {
    public long id;
    public String mRID;
    public String serialNumber;
    public String deviceTypeName;
    public String deviceConfigurationName;
    public DeviceLifeCycleStateInfo state;
    public String batch;
    public Boolean hasOpenDataCollectionIssues;
    public String serviceCategory;
    public String usagePoint;
    public Integer yearOfCertification;
    public Boolean estimationActive;
    public String masterDevicemRID;
    public Instant shipmentDate;
    public Instant installationDate;
    public Instant deactivationDate;
    public Instant decommissionDate;
    public Boolean validationActive;
    public Boolean hasOpenDataValidationIssues;


    public static DeviceSearchInfo from(Device device, BatchService batchService, TopologyService topologyService, IssueService issueService, IssueDataValidationService issueDataValidationService, MeteringService meteringService, Thesaurus thesaurus) {
        DeviceSearchInfo searchInfo = new DeviceSearchInfo();
        searchInfo.id = device.getId();
        searchInfo.mRID = device.getmRID();
        searchInfo.serialNumber = device.getSerialNumber();
        searchInfo.deviceTypeName = device.getDeviceType().getName();
        searchInfo.deviceConfigurationName = device.getDeviceConfiguration().getName();
        State deviceState = device.getState();
        searchInfo.state = new DeviceLifeCycleStateInfo(thesaurus, null, deviceState);
        searchInfo.batch = batchService.findBatch(device).map(Batch::getName).orElse(null);
        searchInfo.hasOpenDataCollectionIssues = issueService.countOpenDataCollectionIssues(device.getmRID()) != 0;
        device.getUsagePoint().ifPresent(usagePoint -> {
            searchInfo.usagePoint = usagePoint.getMRID();
            searchInfo.serviceCategory = usagePoint.getServiceCategory().getName();
        });
        searchInfo.yearOfCertification = device.getYearOfCertification();
        searchInfo.estimationActive = device.forEstimation().isEstimationActive();
        Optional<Device> physicalGateway = topologyService.getPhysicalGateway(device);
        if (physicalGateway.isPresent()) {
            searchInfo.masterDevicemRID = physicalGateway.get().getmRID();
        }
        CIMLifecycleDates lifecycleDates = device.getLifecycleDates();
        searchInfo.shipmentDate = lifecycleDates.getReceivedDate().orElse(null);
        searchInfo.installationDate = lifecycleDates.getInstalledDate().orElse(null);
        searchInfo.deactivationDate = lifecycleDates.getRemovedDate().orElse(null);
        searchInfo.decommissionDate = lifecycleDates.getRetiredDate().orElse(null);
        searchInfo.validationActive = device.forValidation().isValidationActive();
        searchInfo.hasOpenDataValidationIssues = DeviceInfo.getOpenDataValidationIssue(device, meteringService, issueService, issueDataValidationService).isPresent();
        return searchInfo;
    }
}
