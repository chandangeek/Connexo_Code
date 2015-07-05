package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecution;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
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

    private static final Set<String> KNOWN_TOPICS = new HashSet<>(Arrays.asList(
            FIRMWARE_COM_TASK_EXECUTION_STARTED,
            FIRMWARE_COM_TASK_EXECUTION_COMPLETED,
            FIRMWARE_COM_TASK_EXECUTION_FAILED));

    private volatile FirmwareServiceImpl firmwareService;

    public DeviceInFirmwareCampaignHandler() {
        super(LocalEvent.class);
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        if (KNOWN_TOPICS.contains(event.getType().getTopic())) {
            FirmwareComTaskExecution comTaskExecution = (FirmwareComTaskExecution) event.getSource();
            this.firmwareService.getDeviceInFirmwareCampaignsFor(comTaskExecution.getDevice())
                .stream()
                .forEach(deviceInCampaign -> deviceInCampaign.updateStatus(comTaskExecution));
        }
    }

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = (FirmwareServiceImpl) firmwareService;
    }
}
