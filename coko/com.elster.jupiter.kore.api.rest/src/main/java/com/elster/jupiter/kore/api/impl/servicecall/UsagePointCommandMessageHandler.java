package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.json.JsonService;

import java.util.Arrays;
import java.util.Map;

public class UsagePointCommandMessageHandler implements MessageHandler {

    private final ServiceCallService serviceCallService;
    private final JsonService jsonService;

    public UsagePointCommandMessageHandler(ServiceCallService serviceCallService, JsonService jsonService) {
        this.serviceCallService = serviceCallService;
        this.jsonService = jsonService;
    }

    @OverrideHEI
    public void process(Message message) {
        Map<?, ?> map = jsonService.deserialize(message.getPayload(), Map.class);
        serviceCallService.getServiceCall((Integer)map.get("id")).ifPresent(serviceCall -> NNnserviceCall.requestTransition(DefaultState.SUCCESSFUL));
    }
}
