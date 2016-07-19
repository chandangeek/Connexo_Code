package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.json.JsonService;

import java.math.BigDecimal;
import java.util.Map;

public class UsagePointCommandMessageHandler implements MessageHandler {

    private final ServiceCallService serviceCallService;
    private final JsonService jsonService;

    public UsagePointCommandMessageHandler(ServiceCallService serviceCallService, JsonService jsonService) {
        this.serviceCallService = serviceCallService;
        this.jsonService = jsonService;
    }

    @Override
    public void process(Message message) {
        Map<?, ?> map = jsonService.deserialize(message.getPayload(), Map.class);
        ServiceCall serviceCall = serviceCallService.getServiceCall((Integer)map.get("id")).get();
        UsagePointCommandDomainExtension extension = serviceCall.getExtension(UsagePointCommandDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

        BigDecimal successfulCommands = extension.getActualNumberOfSuccessfulCommands();
        BigDecimal failedCommands = extension.getActualNumberOfFailedCommands();
        BigDecimal expectedCommands = extension.getExpectedNumberOfCommands();

        if((Boolean) map.get("success")){
            successfulCommands = successfulCommands.add(BigDecimal.ONE);
            extension.setActualNumberOfSuccessfulCommands(successfulCommands);
        } else {
            failedCommands = failedCommands.add(BigDecimal.ONE);
            extension.setActualNumberOfFailedCommands(failedCommands);
        }
        serviceCall.update(extension);

        if (extension.getExpectedNumberOfCommands().compareTo(successfulCommands.add(failedCommands)) <= 0) {
            if (successfulCommands.compareTo(expectedCommands) >= 0) {
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else if (failedCommands.compareTo(expectedCommands) >= 0) {
                serviceCall.requestTransition(DefaultState.FAILED);
            } else {
                serviceCall.requestTransition(DefaultState.PARTIAL_SUCCESS);
            }
        }
    }
}
