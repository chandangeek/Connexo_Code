/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.task;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestBulkCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeDomainExtension;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConnectionStatusChangeHandler implements MessageHandler {

    private final JsonService jsonService;
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final ServiceCallService serviceCallService;
    private final TransactionService transactionService;
    private final Clock clock;

    public ConnectionStatusChangeHandler(JsonService jsonService, SAPCustomPropertySets sapCustomPropertySets,
                                         ServiceCallService serviceCallService, TransactionService transactionService, Clock clock) {
        this.jsonService = jsonService;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.serviceCallService = serviceCallService;
        this.transactionService = transactionService;
        this.clock = clock;
    }

    @Override
    public void process(Message message) {
        // No implementation needed
    }

    @Override
    public void onMessageDelete(Message message) {
        Optional.ofNullable(jsonService.deserialize(message.getPayload(), CompletionMessageInfo.class))
                .ifPresent(completionMessageInfo -> serviceCallService
                        .getServiceCall(Long.parseLong(completionMessageInfo.getCorrelationId()))
                        .ifPresent(this::resultTransition));
    }

    private List<ServiceCall> findAllChilds(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().collect(Collectors.toList());
    }

    private boolean hasAllChildrenInState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().allMatch(sc -> sc.getState().equals(defaultState));
    }

    private boolean hasAnyChildState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().anyMatch(sc -> sc.getState().equals(defaultState));
    }

    private boolean isLastChild(List<ServiceCall> serviceCalls) {
        return serviceCalls.stream()
                .allMatch(sc -> sc.getState().equals(DefaultState.CANCELLED) ||
                        sc.getState().equals(DefaultState.FAILED) ||
                        sc.getState().equals(DefaultState.SUCCESSFUL));
    }

    private void resultTransition(ServiceCall parent) {
        List<ServiceCall> childs = findAllChilds(parent);
        if (isLastChild(childs)) {
            if (hasAllChildrenInState(childs, DefaultState.SUCCESSFUL)) {
                sendResponseMessage(parent, DefaultState.SUCCESSFUL);
            } else if (hasAllChildrenInState(childs, DefaultState.CANCELLED)) {
                sendResponseMessage(parent, DefaultState.CANCELLED);
            } else if (hasAnyChildState(childs, DefaultState.SUCCESSFUL)) {
                sendResponseMessage(parent, DefaultState.PARTIAL_SUCCESS);
            } else if (parent.canTransitionTo(DefaultState.FAILED)) {
                sendResponseMessage(parent, DefaultState.FAILED);
            } else if (parent.canTransitionTo(DefaultState.ONGOING)) {
                sendResponseMessage(parent, DefaultState.FAILED);
            }
        }
    }

    private void sendResponseMessage(ServiceCall parent, DefaultState finalState) {
        try (TransactionContext context = transactionService.getContext()) {
            parent.transitionWithLockIfPossible(DefaultState.ONGOING);

            ConnectionStatusChangeDomainExtension extension = parent.getExtensionFor(new ConnectionStatusChangeCustomPropertySet()).get();
            parent.log(LogLevel.INFO, "Sending confirmation for disconnection order number: " + extension.getId());

            if (extension.isCancelledBySap() && finalState.equals(DefaultState.CANCELLED)) {
                parent.transitionWithLockIfPossible(finalState);
            } else {
                if (extension.isBulk()) {
                    StatusChangeRequestBulkCreateConfirmationMessage responseMessage = StatusChangeRequestBulkCreateConfirmationMessage
                            .builder(sapCustomPropertySets)
                            .from(parent, findAllChilds(parent), clock.instant())
                            .build();

                    WebServiceActivator.STATUS_CHANGE_REQUEST_BULK_CREATE_CONFIRMATIONS.forEach(sender -> {
                                if (sender.call(responseMessage, parent)) {
                                    parent.requestTransition(finalState);
                                } else {
                                    parent.requestTransition(DefaultState.FAILED);
                                }
                            }
                    );
                } else {
                    StatusChangeRequestCreateConfirmationMessage responseMessage = StatusChangeRequestCreateConfirmationMessage
                            .builder(sapCustomPropertySets)
                            .from(parent, findAllChilds(parent), clock.instant())
                            .build();

                    WebServiceActivator.STATUS_CHANGE_REQUEST_CREATE_CONFIRMATIONS.forEach(sender -> {
                                if (sender.call(responseMessage, parent)) {
                                    parent.requestTransition(finalState);
                                } else {
                                    parent.requestTransition(DefaultState.FAILED);
                                }
                            }
                    );
                }
            }
            context.commit();
        }
    }
}