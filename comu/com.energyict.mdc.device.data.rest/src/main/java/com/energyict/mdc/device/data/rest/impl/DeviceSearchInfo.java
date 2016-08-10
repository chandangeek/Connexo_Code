package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.CIMLifecycleDates;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import java.time.Instant;
import java.util.Optional;

public class DeviceSearchInfo {
    public long id;
    public String mRID;
    public String serialNumber;
    public String deviceTypeName;
    public long deviceTypeId;
    public String deviceConfigurationName;
    public long deviceConfigurationId;
    public String state;
    public String batch;
    public Boolean hasOpenDataCollectionIssues;
    public String serviceCategory;
    public String usagePoint;
    public Integer yearOfCertification;
    public String estimationActive;
    public String masterDevicemRID;
    public Instant shipmentDate;
    public Instant installationDate;
    public Instant deactivationDate;
    public Instant decommissionDate;
    public String validationActive;
    public Boolean hasOpenDataValidationIssues;
    public String location;


    public static DeviceSearchInfo from(Device device, BatchRetriever batchService, GatewayRetriever gatewayRetriever,
                                        IssueRetriever issueService, Thesaurus thesaurus, DeviceEstimationRetriever deviceEstimationRetriever, DeviceValidationRetriever deviceValidationRetriever) {
        DeviceSearchInfo searchInfo = new DeviceSearchInfo();
        searchInfo.id = device.getId();
        searchInfo.mRID = device.getmRID();
        searchInfo.serialNumber = device.getSerialNumber();
        searchInfo.deviceConfigurationName = device.getDeviceConfiguration().getName();
        searchInfo.deviceConfigurationId = device.getDeviceConfiguration().getId();
        searchInfo.deviceTypeName = device.getDeviceType().getName();
        searchInfo.deviceTypeId = device.getDeviceType().getId();
        searchInfo.state = getStateName(device.getState(), thesaurus);
        searchInfo.batch = batchService.findBatch(device).map(Batch::getName).orElse(null);

        searchInfo.hasOpenDataCollectionIssues = issueService.hasOpenDataCollectionIssues(device);
        device.getUsagePoint().ifPresent(usagePoint -> {
            searchInfo.usagePoint = usagePoint.getMRID();
            searchInfo.serviceCategory = usagePoint.getServiceCategory().getName();
        });
        searchInfo.yearOfCertification = device.getYearOfCertification();
        searchInfo.estimationActive = getStatus(deviceEstimationRetriever.isEstimationActive(device), thesaurus);
        Optional<Device> physicalGateway = gatewayRetriever.getPhysicalGateway(device);
        if (physicalGateway.isPresent()) {
            searchInfo.masterDevicemRID = physicalGateway.get().getmRID();
        }
        CIMLifecycleDates lifecycleDates = device.getLifecycleDates();
        searchInfo.shipmentDate = lifecycleDates.getReceivedDate().orElse(null);
        searchInfo.installationDate = lifecycleDates.getInstalledDate().orElse(null);
        searchInfo.deactivationDate = lifecycleDates.getRemovedDate().orElse(null);
        searchInfo.decommissionDate = lifecycleDates.getRetiredDate().orElse(null);
        searchInfo.validationActive = getStatus(deviceValidationRetriever.isValidationActive(device), thesaurus);
        searchInfo.hasOpenDataValidationIssues = issueService.hasOpenDataValidationIssues(device);
        searchInfo.location = device.getLocation().map(Location::toString).
                orElse(device.getSpatialCoordinates()
                        .map(coordinates -> coordinates.toString()).orElse(""));
        return searchInfo;
    }

    private static String getStateName(State state, Thesaurus thesaurus) {
        Optional<DefaultState> defaultState = DefaultState.from(state);
        String name;
        if (defaultState.isPresent()) {
            name = thesaurus.getString(defaultState.get().getKey(), defaultState.get().getKey());
        } else {
            name = state.getName();
        }
        return name;
    }

    private static String getStatus(boolean isActive, Thesaurus thesaurus) {
        if (isActive) {
            return thesaurus.getFormat(DeviceSearchModelTranslationKeys.DEVICE_DATA_STATE_ACTIVE).format();
        }
        return thesaurus.getFormat(DeviceSearchModelTranslationKeys.DEVICE_DATA_STATE_INACTIVE).format();
    }
}
