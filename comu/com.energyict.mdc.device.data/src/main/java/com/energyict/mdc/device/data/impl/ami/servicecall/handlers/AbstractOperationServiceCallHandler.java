package com.energyict.mdc.device.data.impl.ami.servicecall.handlers;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsServiceCallDomainExtension;

import org.osgi.service.component.annotations.Activate;

import java.util.Optional;

/**
 * Abstract implementation of {@link ServiceCallHandler} interface which handles the different steps for
 * a device command operation.
 *
 * @author sva
 * @since 06/06/16 - 13:05
 */
public abstract class AbstractOperationServiceCallHandler implements ServiceCallHandler {

    private volatile MessageService messageService;
    private volatile Thesaurus thesaurus;

    public AbstractOperationServiceCallHandler() {
    }

    public AbstractOperationServiceCallHandler(MessageService messageService, Thesaurus thesaurus) {
        this.setMessageService(messageService);
        this.setThesaurus(thesaurus);
    }

    protected MessageService getMessageService() {
        return messageService;
    }

    protected void setMessageService(MessageService messageService) {
        this.messageService = messageService;
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
                    if (domainExtension.getNrOfUnconfirmedDeviceCommands() == 0) {
                        serviceCall.log(LogLevel.INFO, "All device commands have been executed successfully.");
                        serviceCall.requestTransition(DefaultState.SUCCESSFUL);
                    } else {
                        // Still awaiting confirmation on other messages
                        serviceCall.requestTransition(DefaultState.WAITING);
                    }
                }
                // If oldState was Pending, then the device commands were not yet created, thus checking the nr of unconfirmed messages doesn't make sense
                break;
            case SUCCESSFUL:
                sendFinishedMessageToDestinationSpec(serviceCall);
            default:
                // No specific action required for these states
                break;
        }
    }

    protected void sendFinishedMessageToDestinationSpec(ServiceCall serviceCall) {
        Optional<CompletionOptionsServiceCallDomainExtension> extension = serviceCall.getExtensionFor(new CompletionOptionsCustomPropertySet());
        if (extension.isPresent()) {
            CompletionOptionsServiceCallDomainExtension domainExtension = extension.get();
            messageService.getDestinationSpec(domainExtension.getDestinationSpec()).ifPresent(destinationSpec -> destinationSpec.message(domainExtension.getDestinationMessage()));
        }
    }

    @Override
    public void onChildStateChange(ServiceCall parent, ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
    }
}
