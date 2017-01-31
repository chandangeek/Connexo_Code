/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.prepayment.impl.servicecall;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.prepayment.impl.CompletionOptionsMessageHandlerFactory;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.util.json.JsonService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.text.MessageFormat;
import java.util.Optional;

import static com.elster.jupiter.metering.ami.CompletionMessageInfo.CompletionMessageStatus;
import static com.elster.jupiter.metering.ami.CompletionMessageInfo.FailureReason;

/**
 * Implementation of {@link ServiceCallHandler} interface for Redknee prepayment solution
 *
 * @author sva
 * @since 31/03/16 - 13:05
 */
@Component(name = "com.energyict.servicecall.redknee.operation.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + OperationHandler.HANDLER_NAME)
public class OperationHandler implements ServiceCallHandler {

    public static final String HANDLER_NAME = "RedkneeOperationHandler";

    public volatile JsonService jsonService;
    public volatile MessageService messageService;

    public OperationHandler() {
    }

    public OperationHandler(JsonService jsonService, MessageService messageService) {
        setJsonService(jsonService);
        setMessageService(messageService);
    }

    @Activate
    public void activate() {
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public boolean allowStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        if (newState.equals(DefaultState.CANCELLED)) {
            switch (oldState) {
                case WAITING:
                    return true;
                default:
                    return false;
            }
        }
        return true;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case SUCCESSFUL:
                sendMessageToDestinationSpec(serviceCall, CompletionMessageStatus.SUCCESS);
                break;
            case CANCELLED:
                sendMessageToDestinationSpec(serviceCall, CompletionMessageStatus.CANCELLED, FailureReason.SERVICE_CALL_HAS_BEEN_CANCELLED);
                break;
            case FAILED:
            case REJECTED:
                // Fallback path, expecting a message with proper failure reason is already send out the destination spec
                sendMessageToDestinationSpec(serviceCall, CompletionMessageStatus.FAILURE, FailureReason.UNEXPECTED_EXCEPTION);
            default:
                break;
        }
    }

    @Override
    public void onChildStateChange(ServiceCall parent, ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case SUCCESSFUL:
                parent.log(LogLevel.INFO, MessageFormat.format("Service call {0} (type={1}) was successful", serviceCall.getId(), serviceCall.getType().getName()));
                if (parent.findChildren().stream().allMatch(child -> child.getState().equals(DefaultState.SUCCESSFUL))) {
                    parent.log(LogLevel.INFO, "All child service call operations have been executed successfully");
                    requestTransitionTo(parent, DefaultState.SUCCESSFUL);
                }
                break;
            case FAILED:
                parent.log(LogLevel.SEVERE, MessageFormat.format("Child service call {0} (type={1}) failed", serviceCall.getId(), serviceCall.getType().getName()));
                requestTransitionTo(parent, DefaultState.FAILED);
                break;
            case CANCELLED:
                parent.log(LogLevel.SEVERE, MessageFormat.format("Child service call {0} (type={1}) has been cancelled", serviceCall.getId(), serviceCall.getType().getName()));
                if (parent.canTransitionTo(DefaultState.CANCELLED)) {
                    requestTransitionTo(parent, DefaultState.CANCELLED);
                }
                break;
            default:
                break;
        }
    }

    private void requestTransitionTo(ServiceCall serviceCall, DefaultState state) {
        if (serviceCall.getState().equals(DefaultState.WAITING) && !state.equals(DefaultState.CANCELLED)) { // As we can transit directly to CANCELLED state
            serviceCall.requestTransition(DefaultState.ONGOING);
        }
        if (serviceCall.canTransitionTo(state)) {
            serviceCall.requestTransition(state);
        } // Else the serviceCall is probably already in an end state

    }

    private void sendMessageToDestinationSpec(ServiceCall serviceCall, CompletionMessageStatus status) {
        sendMessageToDestinationSpec(serviceCall, status, null);
    }

    private void sendMessageToDestinationSpec(ServiceCall serviceCall, CompletionMessageStatus status, FailureReason reason) {
        CompletionMessageInfo completionMessageInfo = new CompletionMessageInfo(Long.toString(serviceCall.getId()))
                .setCompletionMessageStatus(status)
                .setFailureReason(reason);
        doSendMessageToDestinationSpec(serviceCall, CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_DESTINATION, completionMessageInfo);
    }

    private void doSendMessageToDestinationSpec(ServiceCall serviceCall, String destinationSpecName, CompletionMessageInfo completionMessageInfo) {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(destinationSpecName);
        if (destinationSpec.isPresent()) {
            destinationSpec.get().message(jsonService.serialize(completionMessageInfo)).send();
        } else {
            serviceCall.log(LogLevel.SEVERE, MessageFormat.format("Failed to send message to destination spec: could not find active destination spec with name ", destinationSpecName));
        }
    }
}