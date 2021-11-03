/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.meterreadingdocument;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingComTaskExecutionHelper;

import java.util.Map;
import java.util.Optional;

public class SAPMeterReadingDocumentOnDemandHandler implements MessageHandler {
    public final static String MANUAL_COMTASKEXECUTION_COMPLETED = "com/energyict/mdc/device/data/manualcomtaskexecution/COMPLETED";
    public final static String SCHEDULED_COMTASKEXECUTION_COMPLETED = "com/energyict/mdc/device/data/scheduledcomtaskexecution/COMPLETED";
    public final static String MANUAL_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/manualcomtaskexecution/FAILED";
    public final static String SCHEDULED_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/scheduledcomtaskexecution/FAILED";

    private ServiceCallService serviceCallService;
    private SAPMeterReadingComTaskExecutionHelper sapMeterReadingComTaskExecutionHelper;
    private CommunicationTaskService communicationTaskService;
    private JsonService jsonService;


    public SAPMeterReadingDocumentOnDemandHandler(ServiceCallService serviceCallService, SAPMeterReadingComTaskExecutionHelper sapMeterReadingComTaskExecutionHelper, JsonService jsonService, CommunicationTaskService communicationTaskService) {
        super();
        this.serviceCallService = serviceCallService;
        this.sapMeterReadingComTaskExecutionHelper = sapMeterReadingComTaskExecutionHelper;
        this.jsonService = jsonService;
        this.communicationTaskService = communicationTaskService;
    }

    @Override
    public void process(Message message) {
        Map<String, Object> messageProperties = this.jsonService.deserialize(message.getPayload(), Map.class);

        if (messageProperties.get("event.topics")!=null) {

            switch (messageProperties.get("event.topics").toString()) {
                case MANUAL_COMTASKEXECUTION_COMPLETED:
                case SCHEDULED_COMTASKEXECUTION_COMPLETED:
                case MANUAL_COMTASKEXECUTION_FAILED:
                case SCHEDULED_COMTASKEXECUTION_FAILED:
                    processEvent(messageProperties, this::onComTaskExecutionFinished);
                    break;
                default:
                    break;
            }
        }
    }

    private interface EventProcessor {
        void process(ComTaskExecution source);
    }

    private void processEvent(Map<String, Object> messageProperties, EventProcessor processor) {
        if (messageProperties.get("id") != null) {
            Optional<ComTaskExecution> comTaskExecution = communicationTaskService.findComTaskExecution(((Number)(messageProperties.get("id"))).longValue());
            if (comTaskExecution.isPresent()) {
                processor.process(comTaskExecution.get());
            }
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
        filter.targetObjects.add(device);
        filter.types.add(serviceCallTypeName);
        filter.states.add(DefaultState.ONGOING.name());
        return serviceCallService.getServiceCallFinder(filter);
    }
}
