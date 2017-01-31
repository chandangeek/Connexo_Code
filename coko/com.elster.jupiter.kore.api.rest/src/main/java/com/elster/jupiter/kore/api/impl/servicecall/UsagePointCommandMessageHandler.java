/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.json.JsonService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.logging.Logger;

public class UsagePointCommandMessageHandler implements MessageHandler {

    private Logger logger = Logger.getLogger(UsagePointCommandMessageHandler.class.getName());
    private final ServiceCallService serviceCallService;
    private final JsonService jsonService;

    public UsagePointCommandMessageHandler(ServiceCallService serviceCallService, JsonService jsonService) {
        this.serviceCallService = serviceCallService;
        this.jsonService = jsonService;
    }

    @Override
    public void process(Message message) {
        Map<?, ?> map = jsonService.deserialize(message.getPayload(), Map.class);
        try {
            if (map.get("correlationId") != null && map.get("completionMessageStatus") != null) {
                serviceCallService.getServiceCall(Integer.valueOf(map.get("correlationId").toString()))
                        .ifPresent(serviceCall -> process(serviceCall, map.get("completionMessageStatus").toString()));
            }
        } catch (NumberFormatException e) {
            this.logger.fine(() -> "Unable to parse service call id '" + map.get("correlationId"));
        }
    }

    private void process(ServiceCall serviceCall, String completionMessageStatus) {
        UsagePointCommandDomainExtension extension = serviceCall.getExtension(UsagePointCommandDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

        BigDecimal successfulCommands = extension.getActualNumberOfSuccessfulCommands();
        BigDecimal failedCommands = extension.getActualNumberOfFailedCommands();
        BigDecimal expectedCommands = extension.getExpectedNumberOfCommands();

        if (CompletionMessageInfo.CompletionMessageStatus.SUCCESS.name().equals(completionMessageStatus)) {
            successfulCommands = successfulCommands.add(BigDecimal.ONE);
            extension.setActualNumberOfSuccessfulCommands(successfulCommands);
        } else {
            failedCommands = failedCommands.add(BigDecimal.ONE);
            extension.setActualNumberOfFailedCommands(failedCommands);
        }
        serviceCall.update(extension);

        if (extension.getExpectedNumberOfCommands().compareTo(successfulCommands.add(failedCommands)) <= 0) {
            if (successfulCommands.compareTo(expectedCommands) >= 0 && serviceCall.canTransitionTo(DefaultState.SUCCESSFUL)) {
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else if (failedCommands.compareTo(expectedCommands) >= 0 && serviceCall.canTransitionTo(DefaultState.FAILED)) {
                serviceCall.requestTransition(DefaultState.FAILED);
            } else if (serviceCall.canTransitionTo(DefaultState.PARTIAL_SUCCESS)) {
                serviceCall.requestTransition(DefaultState.PARTIAL_SUCCESS);
            }
        }
    }
}
