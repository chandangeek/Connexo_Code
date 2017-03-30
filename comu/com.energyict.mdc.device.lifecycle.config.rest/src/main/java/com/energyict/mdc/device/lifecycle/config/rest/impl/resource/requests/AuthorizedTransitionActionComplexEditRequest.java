/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.impl.resource.requests;

import com.elster.jupiter.fsm.StateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.rest.info.AuthorizedActionInfo;

public class AuthorizedTransitionActionComplexEditRequest implements AuthorizedActionChangeRequest {
    private final AuthorizedTransitionAction action;
    private final DeviceLifeCycle deviceLifeCycle;
    private final AuthorizedActionInfo infoForEdit;
    private final StateTransitionEventType stateTransitionEventType;

    public AuthorizedTransitionActionComplexEditRequest(DeviceLifeCycle deviceLifeCycle, AuthorizedTransitionAction action, StateTransitionEventType eventType, AuthorizedActionInfo infoForEdit) {
        this.action = action;
        this.deviceLifeCycle = deviceLifeCycle;
        this.infoForEdit = infoForEdit;
        this.stateTransitionEventType = eventType;
    }

    @Override
    public AuthorizedAction perform() {
        // Remove the old one
        AuthorizedActionChangeRequest deleteRequest = new AuthorizedTransitionActionDeleteRequest(this.deviceLifeCycle, this.action);
        deleteRequest.perform();

        // And create a new one
        AuthorizedActionChangeRequest createRequest = new AuthorizedTransitionActionCreateRequest(this.deviceLifeCycle, this.stateTransitionEventType, this.infoForEdit);
        return createRequest.perform();
    }
}
