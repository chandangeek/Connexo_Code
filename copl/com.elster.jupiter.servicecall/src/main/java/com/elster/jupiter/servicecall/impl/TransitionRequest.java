package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;

public class TransitionRequest {

    private long serviceCallId;
    private DefaultState requestedState;

    private TransitionRequest() {
    }

    public TransitionRequest(ServiceCall serviceCall, DefaultState requestedState) {
        serviceCallId = serviceCall.getId();
        this.requestedState = requestedState;
    }

    public long getServiceCallId() {
        return serviceCallId;
    }

    public DefaultState getRequestedState() {
        return requestedState;
    }
}
