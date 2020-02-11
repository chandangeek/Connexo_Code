/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.scheduling.ComSchedule;
import com.energyict.mdc.device.data.DeviceService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = DeviceSharedCommunicationScheduleRemover.NAME,
        service = DeviceSharedCommunicationScheduleRemover.class, immediate = true,
        property = "name=" + DeviceSharedCommunicationScheduleRemover.NAME)
public class DeviceSharedCommunicationScheduleRemover {

    public static final String NAME = "DeviceSharedCommunicationScheduleRemover";
    private volatile Thesaurus thesaurus;
    private volatile DeviceService deviceService;

    public void remove(long deviceId) {
        Device device = lockDeviceOrThrowException(deviceId);
        List<ComSchedule> schedules = device.getComTaskExecutions().stream()
                .filter(c -> c.usesSharedSchedule())
                .map(c -> c.getComSchedule())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct()
                .collect(Collectors.toList());
        for (ComSchedule schedule : schedules) {
            device.getComTaskExecutions().stream()
                    .filter(comTaskExecution -> comTaskExecution.usesSharedSchedule())
                    .filter(comTaskExecution -> schedule.getComTasks().contains(comTaskExecution.getComTask()))
                    .forEach(comTaskExecution -> comTaskExecution.schedule(null));
        }
        schedules.stream().forEach(device::removeComSchedule);
    }

    private Device lockDeviceOrThrowException(long deviceId) {
        return deviceService.findAndLockDeviceById(deviceId)
                .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.NO_DEVICE_FOUND, deviceId));
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(WebServiceActivator.COMPONENT_NAME, Layer.SOAP);
    }
}
