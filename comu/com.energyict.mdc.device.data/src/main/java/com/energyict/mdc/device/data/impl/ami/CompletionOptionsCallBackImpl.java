/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.ami.CompletionOptionsCallBack;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsServiceCallDomainExtension;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.text.MessageFormat;
import java.util.Optional;

/**
 * @author sva
 * @since 15/07/2016 - 9:07
 */
@Component(name = "com.energyict.mdc.device.data.ami.CompletionOptionsCallBack",
        service = {CompletionOptionsCallBack.class},
        property = "name=MultiSenseCompletionOptionsCallBack", immediate = true)
public class CompletionOptionsCallBackImpl implements CompletionOptionsCallBack {

    private JsonService jsonService;
    private MessageService messageService;

    //For OSGI purposes
    public CompletionOptionsCallBackImpl() {
    }

    public CompletionOptionsCallBackImpl(JsonService jsonService, MessageService messageService) {
        this.jsonService = jsonService;
        this.messageService = messageService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    public void sendFinishedMessageToDestinationSpec(ServiceCall serviceCall) {
        sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageInfo.CompletionMessageStatus.SUCCESS, null);
    }

    public void sendFinishedMessageToDestinationSpec(ServiceCall serviceCall, CompletionMessageInfo.CompletionMessageStatus completionMessageStatus) {
        sendFinishedMessageToDestinationSpec(serviceCall, completionMessageStatus, null);
    }

    public void sendFinishedMessageToDestinationSpec(ServiceCall serviceCall, CompletionMessageInfo.CompletionMessageStatus completionMessageStatus, CompletionMessageInfo.FailureReason failureReason) {
        CompletionOptionsServiceCallDomainExtension domainExtension = serviceCall.getExtensionFor(new CompletionOptionsCustomPropertySet()).get();
        CompletionMessageInfo completionMessageInfo = new CompletionMessageInfo(domainExtension.getDestinationIdentification())
                .setCompletionMessageStatus(completionMessageStatus)
                .setFailureReason(failureReason);
        doSendMessageToDestinationSpec(serviceCall, domainExtension.getDestinationSpec(), completionMessageInfo);
    }

    private void doSendMessageToDestinationSpec(ServiceCall serviceCall, String destinationSpecName, CompletionMessageInfo completionMessageInfo) {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(destinationSpecName);
        if (destinationSpec.isPresent()) {
            destinationSpec.get().message(jsonService.serialize(completionMessageInfo)).send();
        } else {
            serviceCall.log(LogLevel.SEVERE, MessageFormat.format("Failed to send message to destination spec: could not find active destination spec with name {0}", destinationSpecName));
        }
    }
}