/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.servicecall.handlers;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ami.CompletionOptionsCallBack;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandOperationStatus;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import org.osgi.service.component.annotations.Activate;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import static com.elster.jupiter.metering.ami.CompletionMessageInfo.CompletionMessageStatus;
import static com.elster.jupiter.metering.ami.CompletionMessageInfo.FailureReason;

/**
 * Abstract implementation of {@link ServiceCallHandler} interface which handles the different steps for
 * a device command operation.
 *
 * @author sva
 * @since 06/06/16 - 13:05
 */
public abstract class AbstractOperationServiceCallHandler implements ServiceCallHandler {

    private static final String DEVICE_MSG_DELIMITER = ", ";

    private volatile MessageService messageService;
    private volatile Thesaurus thesaurus;
    private volatile CompletionOptionsCallBack completionOptionsCallBack;

    public AbstractOperationServiceCallHandler() {
    }

    public AbstractOperationServiceCallHandler(MessageService messageService, Thesaurus thesaurus, CompletionOptionsCallBack completionOptionsCallBack) {
        this.setMessageService(messageService);
        this.setThesaurus(thesaurus);
        this.setCompletionOptionsCallBack(completionOptionsCallBack);
    }

    protected MessageService getMessageService() {
        return messageService;
    }

    protected void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    protected CompletionOptionsCallBack getCompletionOptionsCallBack() {
        return completionOptionsCallBack;
    }

    protected void setCompletionOptionsCallBack(CompletionOptionsCallBack completionOptionsCallBack) {
        this.completionOptionsCallBack = completionOptionsCallBack;
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Activate
    public void activate() {
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case ONGOING:
                if (oldState != DefaultState.PENDING) {
                    CommandServiceCallDomainExtension domainExtension = serviceCall.getExtensionFor(new CommandCustomPropertySet()).get();
                    if (CommandOperationStatus.SEND_OUT_DEVICE_MESSAGES.equals(domainExtension.getCommandOperationStatus())) {
                        if (domainExtension.getNrOfUnconfirmedDeviceCommands() == 0) {
                            serviceCall.log(LogLevel.INFO, "All device commands have been executed successfully.");
                            handleAllDeviceCommandsExecutedSuccessfully(serviceCall, domainExtension);
                        } else {
                            // Still awaiting confirmation on other messages
                            if (serviceCall.canTransitionTo(DefaultState.WAITING)) {
                                serviceCall.requestTransition(DefaultState.WAITING);
                            }
                        }
                    } else if (domainExtension.getCommandOperationStatus().equals(CommandOperationStatus.READ_STATUS_INFORMATION)) {
                        verifyDeviceStatus(serviceCall);
                    }
                }
                // If oldState was Pending, then the device commands were not yet created, thus checking the nr of unconfirmed messages doesn't make sense
                break;
            case SUCCESSFUL:
                completionOptionsCallBack.sendFinishedMessageToDestinationSpec(serviceCall);
                break;
            case FAILED:
                completionOptionsCallBack.sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageStatus.FAILURE, FailureReason.ONE_OR_MORE_DEVICE_COMMANDS_FAILED);
                break;
            case CANCELLED:
                cancelServiceCall(serviceCall);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    protected void verifyDeviceStatus(ServiceCall serviceCall) {
        // No implementation needed here, subclasses can override this
    }

    protected void handleAllDeviceCommandsExecutedSuccessfully(ServiceCall serviceCall, CommandServiceCallDomainExtension domainExtension) {
        serviceCall.requestTransition(DefaultState.SUCCESSFUL);
    }

    protected void cancelServiceCall(ServiceCall serviceCall) {
        if (!serviceCall.getParent().isPresent() || !serviceCall.getParent().get().getState().equals(DefaultState.CANCELLED)) {
            // If the parent service call is not cancelled, then send out a finished message
            // Else, the finished message should be already send out by the parent & thus there is no need to send one out here
            completionOptionsCallBack.sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageStatus.CANCELLED, FailureReason.SERVICE_CALL_HAS_BEEN_CANCELLED);
        }

        CommandServiceCallDomainExtension domainExtension = serviceCall.getExtensionFor(new CommandCustomPropertySet()).get();
        if (domainExtension.getNrOfUnconfirmedDeviceCommands() != 0) {
            cancelNotYetProcessedDeviceMessages(serviceCall, domainExtension);
        }
    }

    private void cancelNotYetProcessedDeviceMessages(ServiceCall serviceCall, CommandServiceCallDomainExtension domainExtension) {
        // Try to cancel all not yet processed device messages
        Device device = (Device) serviceCall.getTargetObject().get();
        List<DeviceMessage<Device>> interruptCandidates = device.getMessagesByState(DeviceMessageStatus.PENDING);
        interruptCandidates.addAll(device.getMessagesByState(DeviceMessageStatus.WAITING));
        List<String> deviceMsgIds = Arrays.asList(domainExtension.getDeviceMessages().substring(1, domainExtension.getDeviceMessages().length() - 1).split(DEVICE_MSG_DELIMITER));
        serviceCall.log(LogLevel.WARNING, MessageFormat.format("Revoking device messages with ids {0}", Arrays.toString(deviceMsgIds.toArray())));
        interruptCandidates.stream()
                .filter(msg -> deviceMsgIds.contains(Long.toString(msg.getId())))
                .filter(msg -> msg.getStatus().isPredecessorOf(DeviceMessageStatus.REVOKED))
                .forEach(msg -> {
                    tryToRevokeDeviceMessage(msg, serviceCall);
                });
    }

    /**
     * Try to revoke the DeviceMessage<br/>
     * Remark: all ConstraintViolationExceptions are caught and logged on the ServiceCall, these should not be thrown to outside this method
     * ( as we should be able to continue the flow)
     */
    private void tryToRevokeDeviceMessage(DeviceMessage<Device> msg, ServiceCall serviceCall) {
        try {
            msg.revoke();
        } catch (ConstraintViolationException e) {
            serviceCall.log(
                    LogLevel.SEVERE,
                    MessageFormat.format(
                            "Could not revoke device message with id {0}: {1}",
                            msg.getId(),
                            ((ConstraintViolation) e.getConstraintViolations().toArray()[0]).getMessage()
                    )
            );
        }
    }
}
