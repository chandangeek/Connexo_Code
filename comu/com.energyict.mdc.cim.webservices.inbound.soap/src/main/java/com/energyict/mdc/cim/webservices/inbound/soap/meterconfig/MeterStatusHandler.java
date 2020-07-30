/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigServiceCallHandler;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

public class MeterStatusHandler extends EventHandler<LocalEvent> {
    private final static String MANUAL_COMTASKEXECUTION_COMPLETED = "com/energyict/mdc/device/data/manualcomtaskexecution/COMPLETED";
    private final static String MANUAL_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/manualcomtaskexecution/FAILED";
    private final static String SCHEDULED_COMTASKEXECUTION_COMPLETED = "com/energyict/mdc/device/data/scheduledcomtaskexecution/COMPLETED";
    private final static String SCHEDULED_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/scheduledcomtaskexecution/FAILED";

    private final ServiceCallService serviceCallService;
    private final Thesaurus thesaurus;

    @Inject
    public MeterStatusHandler(ServiceCallService serviceCallService, Thesaurus thesaurus) {
        super(LocalEvent.class);
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        switch (event.getType().getTopic()) {
            case MANUAL_COMTASKEXECUTION_COMPLETED:
            case SCHEDULED_COMTASKEXECUTION_COMPLETED:
                processEvent(event, this::onComTaskCompleted);
                break;
            case MANUAL_COMTASKEXECUTION_FAILED:
            case SCHEDULED_COMTASKEXECUTION_FAILED:
                processEvent(event, this::onComTaskFailed);
                break;
            default:
                break;
        }
    }

    private void onComTaskFailed(ComTaskExecution comTaskExecution) {
        Optional<ServiceCall> serviceCall = getServiceCall(comTaskExecution.getComTask(), comTaskExecution.getDevice());
        if (serviceCall.isPresent()) {
            ServiceCall lockedServiceCall = serviceCallService.lockServiceCall(serviceCall.get().getId()).orElseThrow(() -> new IllegalStateException("Unable to lock service call."));;
            if (lockedServiceCall.getState().equals(DefaultState.WAITING)) {
                MeterConfigDomainExtension extension = lockedServiceCall.getExtension(MeterConfigDomainExtension.class)
                        .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call."));
                extension.setErrorMessage(thesaurus.getSimpleFormat(MessageSeeds.COM_TASK_FAILED).format(comTaskExecution.getComTask().getName(), comTaskExecution.getDevice().getName()));
                extension.setErrorCode(MessageSeeds.COM_TASK_FAILED.getErrorCode());
                lockedServiceCall.update(extension);
                lockedServiceCall.requestTransition(DefaultState.ONGOING);
                lockedServiceCall.requestTransition(DefaultState.SUCCESSFUL);
            }
        }

    }

    private void onComTaskCompleted(ComTaskExecution comTaskExecution) {
        Optional<ServiceCall> serviceCall = getServiceCall(comTaskExecution.getComTask(), comTaskExecution.getDevice());
        if (serviceCall.isPresent()) {
            ServiceCall lockedServiceCall = serviceCallService.lockServiceCall(serviceCall.get().getId()).orElseThrow(() -> new IllegalStateException("Unable to lock service call."));
            if (lockedServiceCall.getState().equals(DefaultState.WAITING)) {
                lockedServiceCall.requestTransition(DefaultState.ONGOING);
                lockedServiceCall.requestTransition(DefaultState.SUCCESSFUL);
            }
        }
    }

    private Optional<ServiceCall> getServiceCall(ComTask comTask, Device device) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.types = Collections.singletonList(MeterConfigServiceCallHandler.SERVICE_CALL_HANDLER_NAME);
        filter.targetObjects.add(device);
        filter.states = Collections.singletonList(DefaultState.WAITING.name());
        return serviceCallService.getServiceCallFinder(filter).find().stream()
                .filter(sc -> {
                    Optional<MeterConfigDomainExtension> ext = sc.getExtension(MeterConfigDomainExtension.class);
                    return ext.filter(meterConfigDomainExtension -> comTask.equals(meterConfigDomainExtension.getCommunicationTask().orElse(null))).isPresent();
                })
                .findFirst();
    }

    private void processEvent(LocalEvent event,  Consumer<ComTaskExecution> processor) {
        Object source = event.getSource();
        if (source instanceof ComTaskExecution) {
            ComTaskExecution comTaskExecution = (ComTaskExecution) source;
            processor.accept(comTaskExecution);
        }
    }

}
