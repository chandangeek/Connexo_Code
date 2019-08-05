/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignService;
import com.energyict.mdc.firmware.FirmwareService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.energyict.mdc.firmware.delete.device", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class DeviceDeletionEventHandler implements TopicHandler {

    private volatile FirmwareService firmwareService;
    private volatile FirmwareCampaignService firmwareCampaignService;

    // For OSGi purpose
    public DeviceDeletionEventHandler() {
        super();
    }

    @Inject
    public DeviceDeletionEventHandler(FirmwareService firmwareService, FirmwareCampaignService firmwareCampaignService) {
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
        return "com/energyict/mdc/device/data/device/BEFORE_DELETE";
    }

    @Override
    public void handle(LocalEvent localEvent) {
        this.deleted((Device) localEvent.getSource());
    }

    private void deleted(Device device) {
        this.deleteFirmwareCampaignItems(device);
    }

    private void deleteFirmwareCampaignItems(Device device) {
        this.firmwareCampaignService.findFirmwareCampaignItems(device).forEach(DeviceInFirmwareCampaign::delete);
    }

}