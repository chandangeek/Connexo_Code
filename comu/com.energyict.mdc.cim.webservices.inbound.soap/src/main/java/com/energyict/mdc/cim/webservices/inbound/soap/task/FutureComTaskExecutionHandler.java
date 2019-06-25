/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.task;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;

import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ChildGetMeterReadingsDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ComTaskExecutionServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.DeviceMessageServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.SubParentGetMeterReadingsDomainExtension;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

public class FutureComTaskExecutionHandler implements TaskExecutor {

    private final Clock clock;
    private final ServiceCallService serviceCallService;
    private final DeviceService deviceService;

    public FutureComTaskExecutionHandler(Clock clock, ServiceCallService serviceCallService, DeviceService deviceService) {
        this.clock = clock;
        this.serviceCallService = serviceCallService;
        this.deviceService = deviceService;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        findFutureServiceCalls().stream()
            .forEach(serviceCall -> {
                SubParentGetMeterReadingsDomainExtension extension = serviceCall.getParent().get().getExtension(SubParentGetMeterReadingsDomainExtension.class)
                        .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
                ChildGetMeterReadingsDomainExtension childExtension = serviceCall.getExtension(ChildGetMeterReadingsDomainExtension.class)
                        .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
                String deviceMrid = extension.getEndDevice();
                String comTaksName = childExtension.getCommunicationTask();
                Instant triggerDate = childExtension.getTriggerDate();
                if (clock.instant().isAfter(triggerDate)) {
                    Optional<Device> deviceOptional = deviceService.findDeviceByMrid(deviceMrid);
                    Device device;
                    if (deviceOptional.isPresent()) {
                        device = deviceOptional.get();
                    } else {
                        serviceCall.log(LogLevel.SEVERE, "Unable to get device for mrid " + deviceMrid);
                        serviceCall.requestTransition(DefaultState.FAILED);
                        return;
                    }

                    Optional<ComTaskExecution> comTaskExecutionOptional = device.getComTaskExecutions().stream()
                            .filter(cte -> cte.getComTask().getName().equals(comTaksName))
                            .findFirst();
                    if (comTaskExecutionOptional.isPresent()) {
                        serviceCall.requestTransition(DefaultState.PENDING);
                        serviceCall.requestTransition(DefaultState.ONGOING);
                        serviceCall.requestTransition(DefaultState.WAITING);
                        comTaskExecutionOptional.get().runNow();
                    } else {
                        serviceCall.log(LogLevel.SEVERE, "The communication task required for the read-out not found on the device");
                        serviceCall.requestTransition(DefaultState.FAILED);
                    }
                }
            });
    }

    private Finder<ServiceCall> findFutureServiceCalls() {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.types.add(ComTaskExecutionServiceCallHandler.SERVICE_CALL_HANDLER_NAME);
        filter.types.add(DeviceMessageServiceCallHandler.SERVICE_CALL_HANDLER_NAME);
        filter.states.add(DefaultState.SCHEDULED.name());
        return serviceCallService.getServiceCallFinder(filter);
    }
}
