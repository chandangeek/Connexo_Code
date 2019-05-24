/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.kpi;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpi;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RegisteredDevicesKpiCalculator implements TaskExecutor {
    private static final Logger LOGGER = Logger.getLogger(RegisteredDevicesKpiCalculator.class.getName());
    private final RegisteredDevicesKpiService registeredDevicesKpiService;
    private final EventService eventService;
    private final TopologyService topologyService;
    private final DeviceService deviceService;

    public RegisteredDevicesKpiCalculator(RegisteredDevicesKpiService registeredDevicesKpiService, EventService eventService, DeviceService deviceService, TopologyService topologyService) {
        this.registeredDevicesKpiService = registeredDevicesKpiService;
        this.eventService = eventService;
        this.deviceService = deviceService;
        this.topologyService = topologyService;
    }

    @Override
    public void execute(TaskOccurrence taskOccurrence) {
        String payload = taskOccurrence.getPayLoad();

        try {
            Long id = Long.valueOf(payload);
            Optional<RegisteredDevicesKpi> registeredDevicesKpi = registeredDevicesKpiService.findRegisteredDevicesKpi(id);
            if(registeredDevicesKpi.isPresent()) {
                RegisteredDevicesKpiImpl kpi = (RegisteredDevicesKpiImpl) registeredDevicesKpi.get();
                Instant now = taskOccurrence.getTriggerTime();
                long maxNumber = kpi.getDeviceGroup().getMemberCount(now);
                List<String> deviceGroupMrids = kpi.getDeviceGroup().getMembers(now).stream().map(IdentifiedObject::getMRID).collect(Collectors.toList());
                long registered = deviceService.deviceQuery().select(Where.where("mRID").in(deviceGroupMrids)).stream()
                        .filter(device -> topologyService.getPhysicalGateway(device, now).isPresent())
                        .count();
                kpi.kpi().getMembers().forEach(member -> {
                    switch (member.getName()) {
                        case RegisteredDevicesKpiImpl.KPI_TOTAL_NAME:
                            member.score(now, BigDecimal.valueOf(maxNumber));
                            break;
                        case RegisteredDevicesKpiImpl.KPI_REGISTERED_NAME:
                            member.score(now, BigDecimal.valueOf(registered));
                            break;
                    }
                });

            } else {
                String errorMsg = "Payload '" + payload + "' does not contain the unique identifier of a " + RegisteredDevicesKpi.class.getSimpleName();
                postFailEvent(eventService, taskOccurrence, errorMsg);
                LOGGER.log(Level.SEVERE, errorMsg);
            }
        } catch (NumberFormatException e) {
            String errorMsg = "The data collection kpi identifier in the payload '" + payload + "' could not be parsed to long: " + e.getLocalizedMessage();
            postFailEvent(eventService, taskOccurrence, errorMsg);
            LOGGER.log(Level.SEVERE,  errorMsg, e);
        }
    }
}
