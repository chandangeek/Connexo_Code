/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareManagementDeviceStatus;
import com.energyict.mdc.firmware.FirmwareService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


@Component(name = "com.energyict.mdc.firmware.campaigns.device.handler", service = Subscriber.class, immediate = true)
public class DeviceInFirmwareCampaignHandler extends EventHandler<LocalEvent> {

    private static final String FIRMWARE_COM_TASK_EXECUTION_STARTED = "com/energyict/mdc/device/data/firmwarecomtaskexecution/STARTED";
    private static final String FIRMWARE_COM_TASK_EXECUTION_COMPLETED = "com/energyict/mdc/device/data/firmwarecomtaskexecution/COMPLETED";
    private static final String FIRMWARE_COM_TASK_EXECUTION_FAILED = "com/energyict/mdc/device/data/firmwarecomtaskexecution/FAILED";
    private static final String ACTIVATED_FIRMWARE_VERSION_CREATED = "com/energyict/mdc/firmware/activatedfirmwareversion/CREATED";
    private static final String ACTIVATED_FIRMWARE_VERSION_UPDATED = "com/energyict/mdc/firmware/activatedfirmwareversion/UPDATED";

    private static final Set<String> FIRMWARE_UPLOAD_TOPICS = new HashSet<>(Arrays.asList(
            FIRMWARE_COM_TASK_EXECUTION_STARTED,
            FIRMWARE_COM_TASK_EXECUTION_COMPLETED,
            FIRMWARE_COM_TASK_EXECUTION_FAILED));
    private static final Set<String> FIRMWARE_VALIDATION_TOPICS = new HashSet<>(Arrays.asList(
            ACTIVATED_FIRMWARE_VERSION_CREATED,
            ACTIVATED_FIRMWARE_VERSION_UPDATED));

    private volatile FirmwareServiceImpl firmwareService;

    public DeviceInFirmwareCampaignHandler() {
        super(LocalEvent.class);
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        if (FIRMWARE_UPLOAD_TOPICS.contains(event.getType().getTopic())) {
            ComTaskExecution comTaskExecution = (ComTaskExecution) event.getSource();
            for (DeviceInFirmwareCampaignImpl deviceInCampaign : this.firmwareService.getDeviceInFirmwareCampaignsFor(comTaskExecution.getDevice())) {
                if (FirmwareManagementDeviceStatus.UPLOAD_ONGOING.equals(deviceInCampaign.getStatus())
                        && event.getType().getTopic().equals(FIRMWARE_COM_TASK_EXECUTION_COMPLETED)
                        && deviceInCampaign.getFirmwareCampaign().getValidationTimeout().isPresent()) {
                    deviceInCampaign.startValidation();
                } else {
                    deviceInCampaign.updateStatus(comTaskExecution);
                }
            }
        }
        if (FIRMWARE_VALIDATION_TOPICS.contains(event.getType().getTopic())) {
            ActivatedFirmwareVersion activatedFirmwareVersion = (ActivatedFirmwareVersion) event.getSource();
            for (DeviceInFirmwareCampaignImpl deviceInCampaign : this.firmwareService.getDeviceInFirmwareCampaignsFor(activatedFirmwareVersion.getDevice())) {
                if (FirmwareManagementDeviceStatus.VERIFICATION_ONGOING.equals(deviceInCampaign.getStatus())) {
                    deviceInCampaign.validateFirmwareVersion(activatedFirmwareVersion);
                }
            }
        }
    }

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = (FirmwareServiceImpl) firmwareService;
    }
}
