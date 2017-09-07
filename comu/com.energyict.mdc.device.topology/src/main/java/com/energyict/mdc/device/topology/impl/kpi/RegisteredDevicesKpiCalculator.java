/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.kpi;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpi;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RegisteredDevicesKpiCalculator implements TaskExecutor {
    private static final Logger LOGGER = Logger.getLogger(RegisteredDevicesKpiCalculator.class.getName());
    private final RegisteredDevicesKpiService registeredDevicesKpiService;
    private final TopologyService topologyService;
    private final DeviceService deviceService;

    public RegisteredDevicesKpiCalculator(RegisteredDevicesKpiService registeredDevicesKpiService, DeviceService deviceService, TopologyService topologyService) {
        this.registeredDevicesKpiService = registeredDevicesKpiService;
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
                LOGGER.log(Level.SEVERE, "Payload '" + payload + "' does not contain the unique identifier of a " + RegisteredDevicesKpi.class.getSimpleName());
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "The data collection kpi identifier in the payload '" + payload + "' could not be parsed to long", e);
        }
    }
}
