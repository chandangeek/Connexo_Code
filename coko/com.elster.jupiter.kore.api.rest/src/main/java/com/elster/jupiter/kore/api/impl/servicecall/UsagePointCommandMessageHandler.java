package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.json.JsonService;

import java.math.BigDecimal;
import java.util.Arrays;
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
        UsagePointCommandDomainExtension extension = serviceCall.getExtension(UsagePointCommandDomainExtension.class).orElseThrow(IllegalStateException::new);

        long successfulCommands = extension.getActualNumberOfSuccessfulCommands().longValue();
        long failedCommands = extension.getActualNumberOfFailedCommands().longValue();
        long expectedCommands = extension.getExpectedNumberOfCommands().longValue();

        if((Boolean) map.get("success")){
            successfulCommands++;
            extension.setActualNumberOfSuccessfulCommands(new BigDecimal(successfulCommands));
        } else {
            failedCommands++;
            extension.setActualNumberOfFailedCommands(new BigDecimal(failedCommands));
        }

        if (extension.getExpectedNumberOfCommands().longValue()<=(successfulCommands+failedCommands)){
            if(successfulCommands >= expectedCommands){
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else if (failedCommands >= expectedCommands){
                serviceCall.requestTransition(DefaultState.FAILED);
            } else {
                serviceCall.requestTransition(DefaultState.PARTIAL_SUCCESS);
            }
        }
    }
}
