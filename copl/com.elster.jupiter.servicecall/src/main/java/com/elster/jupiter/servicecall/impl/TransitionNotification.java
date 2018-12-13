/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;

public class TransitionNotification {

    private long serviceCallId;
    private DefaultState oldState;
    private DefaultState newState;

    private TransitionNotification() {
    }

    public TransitionNotification(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        this.oldState = oldState;
        serviceCallId = serviceCall.getId();
        this.newState = newState;
    }

    public long getServiceCallId() {
        return serviceCallId;
    }

    public DefaultState getNewState() {
        return newState;
    }

    public DefaultState getOldState() {
        return oldState;
    }
}
