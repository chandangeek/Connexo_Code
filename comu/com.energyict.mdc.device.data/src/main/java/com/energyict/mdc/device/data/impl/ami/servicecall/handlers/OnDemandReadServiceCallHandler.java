package com.energyict.mdc.device.data.impl.ami.servicecall.handlers;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsServiceCallDomainExtension;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;

@Component(name = "com.energyict.servicecall.ami.on.demand.read.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + OnDemandReadServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class OnDemandReadServiceCallHandler implements ServiceCallHandler {

    public static final String VERSION = "v1.0";
    public static final String SERVICE_CALL_HANDLER_NAME = "OnDemandReadServiceCallHandler";

    private volatile MessageService messageService;
    private volatile Thesaurus thesaurus;
    private volatile DeviceService deviceService;

    public OnDemandReadServiceCallHandler() {
        super();
    }

    // Constructor only to be used in JUnit tests
    public OnDemandReadServiceCallHandler(MessageService messageService, DeviceService deviceService, Thesaurus thesaurus) {
        this.messageService = messageService;
        this.deviceService = deviceService;
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case SUCCESSFUL:
                sendFinishedMessageToDestinationSpec(serviceCall);
                break;
            case FAILED:
                sendFinishedMessageToDestinationSpec(serviceCall);
                break;
            case PARTIAL_SUCCESS:
                sendFinishedMessageToDestinationSpec(serviceCall);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    protected void sendFinishedMessageToDestinationSpec(ServiceCall serviceCall) {
        Optional<CompletionOptionsServiceCallDomainExtension> extension = serviceCall.getExtensionFor(new CompletionOptionsCustomPropertySet());
        if (extension.isPresent()) {
            CompletionOptionsServiceCallDomainExtension domainExtension = extension.get();
            messageService.getDestinationSpec(domainExtension.getDestinationSpec()).ifPresent(destinationSpec -> destinationSpec.message(domainExtension.getDestinationMessage()).send());
        }
    }
}