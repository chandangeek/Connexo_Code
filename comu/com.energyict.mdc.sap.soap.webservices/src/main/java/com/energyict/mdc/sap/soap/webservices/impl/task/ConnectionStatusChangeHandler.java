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
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallHelper;
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
    private final WebServiceActivator webServiceActivator;

    public ConnectionStatusChangeHandler(JsonService jsonService, SAPCustomPropertySets sapCustomPropertySets,
                                         ServiceCallService serviceCallService, TransactionService transactionService,
                                         Clock clock, WebServiceActivator webServiceActivator) {
        this.jsonService = jsonService;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.serviceCallService = serviceCallService;
        this.transactionService = transactionService;
        this.clock = clock;
        this.webServiceActivator = webServiceActivator;
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

    private void resultTransition(ServiceCall parent) {
        List<ServiceCall> children = ServiceCallHelper.findChildren(parent);
        if (ServiceCallHelper.isLastChild(children)) {
            if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.SUCCESSFUL)) {
                sendResponseMessage(parent, DefaultState.SUCCESSFUL);
            } else if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.CANCELLED)) {
                sendResponseMessage(parent, DefaultState.CANCELLED);
            } else if (ServiceCallHelper.hasAnyChildState(children, DefaultState.SUCCESSFUL)) {
                sendResponseMessage(parent, DefaultState.PARTIAL_SUCCESS);
            } else if (parent.canTransitionTo(DefaultState.FAILED)) {
                sendResponseMessage(parent, DefaultState.FAILED);
            } else if (parent.canTransitionTo(DefaultState.ONGOING)) {
                //  it handles fail case but when parent is in WAITING state for example.
                //  sendResponseMessage transfers parent to ONGOING and after that transfers to FAILED state
                sendResponseMessage(parent, DefaultState.FAILED);
            }
        }
    }

    private void sendResponseMessage(ServiceCall parent, DefaultState finalState) {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCall parentLocked = serviceCallService.lockServiceCall(parent.getId()).orElseThrow(() -> new IllegalStateException("Unable to lock service call"));
            parentLocked.requestTransition(DefaultState.ONGOING);

            ConnectionStatusChangeDomainExtension extension = parentLocked.getExtensionFor(new ConnectionStatusChangeCustomPropertySet()).get();
            parentLocked.log(LogLevel.INFO, "Sending confirmation for disconnection order number: " + extension.getId());

            if (extension.isCancelledBySap() && finalState.equals(DefaultState.CANCELLED)) {
                parentLocked.requestTransition(finalState);
            } else {
                if (extension.isBulk()) {
                    StatusChangeRequestBulkCreateConfirmationMessage responseMessage = StatusChangeRequestBulkCreateConfirmationMessage
                            .builder(sapCustomPropertySets)
                            .from(parentLocked, ServiceCallHelper.findChildren(parentLocked), webServiceActivator.getMeteringSystemId(), clock.instant())
                            .build();

                    WebServiceActivator.STATUS_CHANGE_REQUEST_BULK_CREATE_CONFIRMATIONS.forEach(sender -> {
                                if (sender.call(responseMessage, parentLocked)) {
                                    parentLocked.requestTransition(finalState);
                                } else {
                                    parentLocked.requestTransition(DefaultState.FAILED);
                                }
                            }
                    );
                } else {
                    StatusChangeRequestCreateConfirmationMessage responseMessage = StatusChangeRequestCreateConfirmationMessage
                            .builder(sapCustomPropertySets)
                            .from(parentLocked, ServiceCallHelper.findChildren(parentLocked), webServiceActivator.getMeteringSystemId(), clock.instant())
                            .build();

                    WebServiceActivator.STATUS_CHANGE_REQUEST_CREATE_CONFIRMATIONS.forEach(sender -> {
                                if (sender.call(responseMessage, parentLocked)) {
                                    parentLocked.requestTransition(finalState);
                                } else {
                                    parentLocked.requestTransition(DefaultState.FAILED);
                                }
                            }
                    );
                }
            }
            context.commit();
        }
    }
}