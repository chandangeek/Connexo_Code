package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandCustomPropertySet;
import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandDomainExtension;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;

import javax.inject.Inject;

public class UsagePointCommandHelper {
    private final CustomPropertySetService customPropertySetService;
    private final ServiceCallService serviceCallService;
    private final MessageService messageService;

    @Inject
    public UsagePointCommandHelper(CustomPropertySetService customPropertySetService, ServiceCallService serviceCallService, MessageService messageService) {
        this.customPropertySetService = customPropertySetService;
        this.serviceCallService = serviceCallService;
        this.messageService = messageService;
    }

    public ServiceCall getServiceCall(UsagePointCommandCallbackInfo callbackInfo){
        RegisteredCustomPropertySet customPropertySet = customPropertySetService.findActiveCustomPropertySets(ServiceCall.class)
                .stream()
                .filter(cps -> cps.getCustomPropertySet()
                        .getId()
                        .equals(UsagePointCommandDomainExtension.class.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find command custom property set"));

        UsagePointCommandDomainExtension usagePointCommandDomainExtension = new UsagePointCommandDomainExtension();
        usagePointCommandDomainExtension.setCallbackHttpMethod(callbackInfo.method);
        usagePointCommandDomainExtension.setCallbackSuccessURL(callbackInfo.successURL);
        usagePointCommandDomainExtension.setCallbackPartialSuccessURL(callbackInfo.partialSuccessURL);
        usagePointCommandDomainExtension.setCallbackFailureURL(callbackInfo.failureURL);

        ServiceCallType serviceCallType = serviceCallService.findServiceCallType("UsagePointCommandHandler", "v1.0")
                .orElseGet(() -> serviceCallService.createServiceCallType("UsagePointCommandHandler", "v1.0")
                        .handler("UsagePointCommandHandler")
                        .customPropertySet(customPropertySet)
                        .create());

        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .origin("PublicAPI")
                .extendedWith(usagePointCommandDomainExtension)
                .create();
        serviceCall.requestTransition(DefaultState.PENDING);
        return serviceCall;
    }

    public DestinationSpec getDestinationSpec(){
        return messageService.getDestinationSpec("CommandCallback").orElseGet(this::createDestinationSpec);
    }

    private DestinationSpec createDestinationSpec() {
        QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec("CommandCallback", 60);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe("CommandCallback");
        return destinationSpec;
    }


}
