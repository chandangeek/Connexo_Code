/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.EventType;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignService;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.firmware.FirmwareService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Responds to deletion events of {@link DeviceType}s
 * to make sure that dependent objects that are journalled are deleted
 * instead of being deleted with "cascade delete" option of the foreigh key constraint.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-07-14 (14:44)
 */
@Component(name = "com.energyict.mdc.firmware.delete.devicetype", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class DeviceTypeDeletionEventHandler implements TopicHandler {

    private volatile FirmwareService firmwareService;
    private volatile FirmwareCampaignService firmwareCampaignService;

    // For OSGi purpose
    public DeviceTypeDeletionEventHandler() {
        super();
    }

    @Inject
    public DeviceTypeDeletionEventHandler(FirmwareService firmwareService, FirmwareCampaignService firmwareCampaignService) {
        this();
        this.setFirmwareService(firmwareService);
    }

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
        this.firmwareCampaignService = firmwareService.getFirmwareCampaignService();
    }

    @Override
    public String getTopicMatcher() {
        return EventType.DEVICETYPE_VALIDATE_DELETE.topic();
    }

    @Override
    public void handle(LocalEvent localEvent) {
        this.deleted((DeviceType) localEvent.getSource());
    }

    private void deleted(DeviceType deviceType) {
        this.deleteFirmwareManagementOptions(deviceType);
        this.deleteFirmwareCampaigns(deviceType);
    }

    private void deleteFirmwareManagementOptions(DeviceType deviceType) {
        this.firmwareService.findFirmwareManagementOptions(deviceType).ifPresent(FirmwareManagementOptions::delete);
    }

    private void deleteFirmwareCampaigns(DeviceType deviceType) {
        this.firmwareCampaignService.findFirmwareCampaigns(deviceType).forEach(FirmwareCampaign::delete);
    }

}