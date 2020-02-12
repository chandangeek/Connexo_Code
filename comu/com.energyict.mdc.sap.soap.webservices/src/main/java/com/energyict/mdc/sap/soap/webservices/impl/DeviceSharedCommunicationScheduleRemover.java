/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.common.scheduling.ComSchedule;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.DeviceService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component(name = DeviceSharedCommunicationScheduleRemover.NAME,
        service = DeviceSharedCommunicationScheduleRemover.class, immediate = true,
        property = "name=" + DeviceSharedCommunicationScheduleRemover.NAME)
public class DeviceSharedCommunicationScheduleRemover {

    private static final Logger LOGGER = Logger.getLogger(DeviceSharedCommunicationScheduleRemover.class.getName());
    public static final String NAME = "DeviceSharedCommunicationScheduleRemover";
    private volatile DeviceService deviceService;

    public void removeComSchedules(long deviceId) {
        deviceService.findAndLockDeviceById(deviceId).ifPresent(device -> {
            LOGGER.log(Level.INFO, "All profiles are closed, removing shared com schedules from device " + device.getName());
            List<ComSchedule> schedules = device.getComTaskExecutions().stream()
                    .filter(ComTaskExecution::usesSharedSchedule)
                    .peek(comTaskExecution -> comTaskExecution.schedule(null))
                    .map(ComTaskExecution::getComSchedule)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .distinct()
                    .collect(Collectors.toList());
            schedules.stream().forEach(device::removeComSchedule);
        });
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }
}
