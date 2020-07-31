/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.task;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;

import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols.CancellationReason;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols.EndDeviceControlsDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols.MasterEndDeviceControlsDomainExtension;
import com.energyict.mdc.device.data.ami.ICommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.ami.MultiSenseHeadEndInterface;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands.ServiceCallTypes.END_DEVICE_CONTROLS;

public class EndDeviceControlsCancellationHandler implements TaskExecutor {

    private final ServiceCallService serviceCallService;
    private final Clock clock;
    private final MultiSenseHeadEndInterface multiSenseHeadEndInterface;

    public EndDeviceControlsCancellationHandler(ServiceCallService serviceCallService, Clock clock,
                                                MultiSenseHeadEndInterface multiSenseHeadEndInterface) {
        this.serviceCallService = serviceCallService;
        this.clock = clock;
        this.multiSenseHeadEndInterface = multiSenseHeadEndInterface;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        Instant now = clock.instant();
        Finder<ServiceCall> serviceCallFinder = findAvailableServiceCalls(END_DEVICE_CONTROLS.getTypeName());
        serviceCallFinder
                .stream()
                .forEach(serviceCall -> {
                    ServiceCall headEndSC = serviceCall.findChildren().paged(0, 0).find().get(0);
                    ICommandServiceCallDomainExtension headEndExtension = multiSenseHeadEndInterface.getCommandServiceCallDomainExtension(headEndSC)
                            .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

                    long timeout = serviceCall.getParent().get().getParent().get().getExtension(MasterEndDeviceControlsDomainExtension.class)
                            .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for master service call"))
                            .getMaxExecTime();

                    if (Duration.between(headEndExtension.getReleaseDate(), now).get(ChronoUnit.SECONDS) > timeout * 60) {
                        serviceCall = lock(serviceCall);
                        if (serviceCall.getState().equals(DefaultState.WAITING)) {
                            EndDeviceControlsDomainExtension extension = serviceCall.getExtension(EndDeviceControlsDomainExtension.class)
                                    .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
                            extension.setCancellationReason(CancellationReason.TIMEOUT);
                            serviceCall.update(extension);
                            serviceCall.requestTransition(DefaultState.CANCELLED);
                        }
                    }

                });
    }

    private Finder<ServiceCall> findAvailableServiceCalls(String serviceCallTypeName) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.types.add(serviceCallTypeName);
        filter.states = Collections.singletonList(DefaultState.WAITING.name());
        return serviceCallService.getServiceCallFinder(filter);
    }

    private ServiceCall lock(ServiceCall serviceCall) {
        return serviceCallService.lockServiceCall(serviceCall.getId())
                .orElseThrow(() -> new IllegalStateException("Service call " + serviceCall.getNumber() + " disappeared."));
    }
}
