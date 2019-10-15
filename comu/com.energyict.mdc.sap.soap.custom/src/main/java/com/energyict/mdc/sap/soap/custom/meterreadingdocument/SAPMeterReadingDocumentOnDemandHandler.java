/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.meterreadingdocument;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingComTaskExecutionHelper;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "SAPMeterReadingDocumentOnDemandHandler", service = Subscriber.class, immediate = true)
public class SAPMeterReadingDocumentOnDemandHandler extends EventHandler<LocalEvent> {
    private final static String MANUAL_COMTASKEXECUTION_COMPLETED = "com/energyict/mdc/device/data/manualcomtaskexecution/COMPLETED";
    private final static String SCHEDULED_COMTASKEXECUTION_COMPLETED = "com/energyict/mdc/device/data/scheduledcomtaskexecution/COMPLETED";
    private final static String MANUAL_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/manualcomtaskexecution/FAILED";
    private final static String SCHEDULED_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/scheduledcomtaskexecution/FAILED";

    private volatile ServiceCallService serviceCallService;
    private volatile SAPMeterReadingComTaskExecutionHelper sapMeterReadingComTaskExecutionHelper;

    public SAPMeterReadingDocumentOnDemandHandler() {
        super(LocalEvent.class);
    }

    @Inject
    public SAPMeterReadingDocumentOnDemandHandler(ServiceCallService serviceCallService,
                                                  SAPMeterReadingComTaskExecutionHelper sapMeterReadingComTaskExecutionHelper) {
        this();
        setServiceCallService(serviceCallService);
        setSapMeterReadingComTaskExecutionHelper(sapMeterReadingComTaskExecutionHelper);
    }

    @Reference
    public void setSapMeterReadingComTaskExecutionHelper(SAPMeterReadingComTaskExecutionHelper sapMeterReadingComTaskExecutionHelper) {
        this.sapMeterReadingComTaskExecutionHelper = sapMeterReadingComTaskExecutionHelper;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        switch (event.getType().getTopic()) {
            case MANUAL_COMTASKEXECUTION_COMPLETED:
            case SCHEDULED_COMTASKEXECUTION_COMPLETED:
            case MANUAL_COMTASKEXECUTION_FAILED:
            case SCHEDULED_COMTASKEXECUTION_FAILED:
                processEvent(event, this::onComTaskExecutionFinished);
                break;
            default:
                break;
        }
    }

    private interface EventProcessor {
        void process(ComTaskExecution source);
    }

    private void processEvent(LocalEvent event, EventProcessor processor) {
        Object source = event.getSource();
        if (source instanceof ComTaskExecution) {
            ComTaskExecution comTaskExecution = (ComTaskExecution) source;
            processor.process(comTaskExecution);
        }
    }

    private void onComTaskExecutionFinished(ComTaskExecution comTaskExecution) {
        if (comTaskExecution.getStatus().equals(TaskStatus.Retrying) ||
                comTaskExecution.getStatus().equals(TaskStatus.RetryingWithPriority)) {
            return;
        }

        findAvailableServiceCalls(comTaskExecution.getDevice(), sapMeterReadingComTaskExecutionHelper.getServiceCallTypeName())
                .stream()
                .forEach(serviceCall -> {
                    sapMeterReadingComTaskExecutionHelper.calculateData(serviceCall, comTaskExecution.getId());
                });
    }

    private Finder<ServiceCall> findAvailableServiceCalls(Device device, String serviceCallTypeName) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.targetObject = device;
        filter.types.add(serviceCallTypeName);
        filter.states.add(DefaultState.ONGOING.name());
        return serviceCallService.getServiceCallFinder(filter);
    }
}
