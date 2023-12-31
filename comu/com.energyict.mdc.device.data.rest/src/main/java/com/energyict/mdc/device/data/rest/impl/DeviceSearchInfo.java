/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.DefaultState;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.energyict.mdc.common.device.data.Batch;
import com.energyict.mdc.common.device.data.CIMLifecycleDates;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.Device.CalendarSupport;

import java.time.Instant;
import java.util.Optional;

public class DeviceSearchInfo {
    public long id;
    public String name;
    public String serialNumber;
    public long deviceTypeId;
    public String deviceTypeName;
    public long deviceConfigurationId;
    public String deviceConfigurationName;
    public String state;
    public String batch;
    public Boolean hasOpenDataCollectionIssues;
    public String serviceCategory;
    public String usagePoint;
    public Integer yearOfCertification;
    public String estimationActive;
    public String masterDeviceName;
    public Instant shipmentDate;
    public Instant installationDate;
    public Instant deactivationDate;
    public Instant decommissionDate;
    public String validationActive;
    public Boolean hasOpenDataValidationIssues;
    public String location;
    public String manufacturer;
    public String modelNbr;
    public String modelVersion;
    public String activeCalendar;
    public String passiveCalendar;
    public String plannedPassiveCalendar;
    public Boolean hasServiceKeys;

    public static DeviceSearchInfo from(Device device, GatewayRetriever gatewayRetriever,
                                        IssueRetriever issueService, Thesaurus thesaurus,
                                        MeteringTranslationService meteringTranslationService,
                                        DeviceValidationRetriever deviceValidationRetriever) {
        DeviceSearchInfo searchInfo = new DeviceSearchInfo();
        searchInfo.id = device.getId();
        searchInfo.name = device.getName();
        searchInfo.serialNumber = device.getSerialNumber();
        searchInfo.deviceConfigurationId = device.getDeviceConfiguration().getId();
        searchInfo.deviceConfigurationName = device.getDeviceConfiguration().getName();
        searchInfo.deviceTypeId = device.getDeviceType().getId();
        searchInfo.deviceTypeName = device.getDeviceType().getName();
        searchInfo.state = getStateName(device.getState(), meteringTranslationService);
        searchInfo.batch = device.getBatch().map(Batch::getName).orElse(null);

        searchInfo.hasOpenDataCollectionIssues = issueService.hasOpenDataCollectionIssues(device);
        device.getUsagePoint().ifPresent(usagePoint -> {
            searchInfo.usagePoint = usagePoint.getName();
            searchInfo.serviceCategory = usagePoint.getServiceCategory().getName();
        });
        searchInfo.yearOfCertification = device.getYearOfCertification();
        searchInfo.estimationActive = getStatus(device.forEstimation().isEstimationActive(), thesaurus);
        Optional<Device> physicalGateway = gatewayRetriever.getPhysicalGateway(device);
        if (physicalGateway.isPresent()) {
            searchInfo.masterDeviceName = physicalGateway.get().getName();
        }
        CIMLifecycleDates lifecycleDates = device.getLifecycleDates();
        searchInfo.shipmentDate = lifecycleDates.getReceivedDate().orElse(null);
        searchInfo.installationDate = lifecycleDates.getInstalledDate().orElse(null);
        searchInfo.deactivationDate = lifecycleDates.getRemovedDate().orElse(null);
        searchInfo.decommissionDate = lifecycleDates.getRetiredDate().orElse(null);
        searchInfo.validationActive = getStatus(deviceValidationRetriever.isValidationActive(device), thesaurus);
        searchInfo.hasOpenDataValidationIssues = issueService.hasOpenDataValidationIssues(device);
        searchInfo.location = device.getLocation().map(Location::toString)
                .orElse(device.getSpatialCoordinates().map(SpatialCoordinates::toString).orElse(""));
        searchInfo.manufacturer = device.getManufacturer();
        searchInfo.modelNbr = device.getModelNumber();
        searchInfo.modelVersion = device.getModelVersion();
        getCalendars(searchInfo, device);
        searchInfo.hasServiceKeys = device.getSecurityAccessors().stream()
                .anyMatch(accessor -> accessor.isServiceKey());
        return searchInfo;
    }

    private static void getCalendars(DeviceSearchInfo searchInfo, Device device) {
        CalendarSupport calendars = device.calendars();
        calendars.getActive().ifPresent(cal -> {
            searchInfo.activeCalendar = cal.getAllowedCalendar().getName();
        });
        calendars.getPassive().ifPresent(cal -> {
            searchInfo.passiveCalendar = cal.getAllowedCalendar().getName();
        });
        calendars.getPlannedPassive().ifPresent(cal -> {
            searchInfo.plannedPassiveCalendar = cal.getAllowedCalendar().getName();
        });
    }

    private static String getStateName(State state, MeteringTranslationService meteringTranslationService) {
        return DefaultState
                .from(state)
                .map(meteringTranslationService::getDisplayName)
                .orElseGet(state::getName);
    }

    private static String getStatus(boolean isActive, Thesaurus thesaurus) {
        if (isActive) {
            return thesaurus.getFormat(DeviceSearchModelTranslationKeys.DEVICE_DATA_STATE_ACTIVE).format();
        }
        return thesaurus.getFormat(DeviceSearchModelTranslationKeys.DEVICE_DATA_STATE_INACTIVE).format();
    }
}
