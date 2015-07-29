package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.tasks.TaskService;

import javax.inject.Inject;
import java.time.Clock;

public class FirmwareCampaignHandlerContext {
    private final FirmwareServiceImpl firmwareService;
    private final MeteringGroupsService meteringGroupsService;
    private final DeviceService deviceService;
    private final Clock clock;
    private final EventService eventService;
    private final TaskService taskService;

    @Inject
    public FirmwareCampaignHandlerContext(FirmwareServiceImpl firmwareService, MeteringGroupsService meteringGroupsService, DeviceService deviceService, Clock clock, EventService eventService, TaskService taskService) {
        this.firmwareService = firmwareService;
        this.meteringGroupsService = meteringGroupsService;
        this.deviceService = deviceService;
        this.clock = clock;
        this.eventService = eventService;
        this.taskService = taskService;
    }

    public FirmwareServiceImpl getFirmwareService() {
        return firmwareService;
    }

    public MeteringGroupsService getMeteringGroupsService() {
        return meteringGroupsService;
    }

    public DeviceService getDeviceService() {
        return deviceService;
    }

    public Clock getClock() {
        return clock;
    }

    public EventService getEventService() {
        return eventService;
    }

    public TaskService getTaskService() {
        return taskService;
    }
}
