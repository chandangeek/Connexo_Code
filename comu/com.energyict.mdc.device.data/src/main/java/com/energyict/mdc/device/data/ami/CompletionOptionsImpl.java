package com.energyict.mdc.device.data.ami;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.servicecall.ServiceCall;

public class CompletionOptionsImpl implements CompletionOptions {

    private volatile String message;
    private volatile DestinationSpec destination;
    private volatile ServiceCall serviceCall;

    public CompletionOptionsImpl(String message, DestinationSpec destination, ServiceCall serviceCall) {
        this.message = message;
        this.destination = destination;
        this.serviceCall = serviceCall;
    }

    @Override
    public void whenFinishedSend(String message, DestinationSpec destinationSpec) {
        // save the msg and the name of the destination spec to the custom prop set of the child servicecall
        // moved all service call related code to demo bundle under ami_scsexamples
    /*    this.serviceCall.findChildren()
                .stream()
                .filter(child -> child.getExtension(ContactorOperationDomainExtension.class))
                .findFirst()
                .ifPresent(extension -> {
                    extension.setDestinationSpecName(destinationSpec.getName());
                    extension.setCompletionMessage(message);
                    this.serviceCall.update(extension);
                });
    */
        destinationSpec.message(message).send();
    }


    @Override
    public CompletionOptionsImpl setDestination(DestinationSpec destination) {
        this.destination = destination;
        return this;
    }

    @Override
    public CompletionOptionsImpl setMessage(String message) {
        this.message = message;
        return this;
    }
}
