/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap.task;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReadMeterChangeHandler implements MessageHandler {

    private final JsonService jsonService;
    private final ServiceCallService serviceCallService;
    private final TransactionService transactionService;

    public ReadMeterChangeHandler(JsonService jsonService, ServiceCallService serviceCallService,
                                  TransactionService transactionService) {
        this.jsonService = jsonService;
        this.serviceCallService = serviceCallService;
        this.transactionService = transactionService;
    }

    @Override
    public void process(Message message) {
        // No implementation needed
    }

    @Override
    public void onMessageDelete(Message message) {
        try (TransactionContext context = transactionService.getContext()) {
            Optional.ofNullable(jsonService.deserialize(message.getPayload(), CompletionMessageInfo.class))
                    .ifPresent(completionMessageInfo -> serviceCallService
                            .getServiceCall(Long.parseLong(completionMessageInfo.getCorrelationId()))
                            .ifPresent(this::resultTransition));
            context.commit();
        }
    }

    private List<ServiceCall> findAllChildren(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().collect(Collectors.toList());
    }

    private boolean hasAllChildrenStates(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().allMatch(sc -> sc.getState().equals(defaultState));
    }

    private boolean hasAnyChildState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().anyMatch(sc -> sc.getState().equals(defaultState));
    }

    private boolean isLastChild(List<ServiceCall> serviceCalls) {
        return serviceCalls.stream()
                .allMatch(sc -> sc.getState().equals(DefaultState.CANCELLED)
                        || sc.getState().equals(DefaultState.FAILED)
                        || sc.getState().equals(DefaultState.REJECTED)
                        || sc.getState().equals(DefaultState.SUCCESSFUL));
    }

    private void resultTransition(ServiceCall parent) {
        List<ServiceCall> childs = findAllChildren(parent);
        if (isLastChild(childs)) {
            if (hasAllChildrenStates(childs, DefaultState.SUCCESSFUL)) {
                transitParentToResultState(parent, DefaultState.PAUSED);
                transitParentToResultState(parent, DefaultState.ONGOING);
            } else if (hasAllChildrenStates(childs, DefaultState.CANCELLED)) {
                transitParentToResultState(parent, DefaultState.CANCELLED);
            } else if (hasAnyChildState(childs, DefaultState.SUCCESSFUL)) {
                transitParentToResultState(parent, DefaultState.PARTIAL_SUCCESS);
            } else {
                transitParentToResultState(parent, DefaultState.FAILED);
            }
        }
    }

    private void transitParentToResultState(ServiceCall parent, DefaultState finalState) {
        parent.requestTransition(DefaultState.ONGOING);
        if (finalState != DefaultState.ONGOING) {
            parent.requestTransition(finalState);
        }
    }
}