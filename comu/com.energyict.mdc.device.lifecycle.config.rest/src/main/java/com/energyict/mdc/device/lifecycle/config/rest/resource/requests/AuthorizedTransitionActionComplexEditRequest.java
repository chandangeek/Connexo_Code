package com.energyict.mdc.device.lifecycle.config.rest.resource.requests;

import com.elster.jupiter.fsm.StateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.rest.response.AuthorizedActionInfo;

public class AuthorizedTransitionActionComplexEditRequest implements AuthorizedActionChangeRequest {
    private final AuthorizedTransitionAction action;
    private final DeviceLifeCycle deviceLifeCycle;
    private final AuthorizedActionInfo infoForEdit;

    public AuthorizedTransitionActionComplexEditRequest(DeviceLifeCycle deviceLifeCycle, AuthorizedTransitionAction action, AuthorizedActionInfo infoForEdit) {
        this.action = action;
        this.deviceLifeCycle = deviceLifeCycle;
        this.infoForEdit = infoForEdit;
    }

    @Override
    public AuthorizedAction perform() {
        StateTransitionEventType eventType = action.getStateTransition().getEventType();

        // Remove the old one
        AuthorizedActionChangeRequest deleteRequest = new AuthorizedTransitionActionDeleteRequest(this.deviceLifeCycle, this.action);
        deleteRequest.perform();

        // And create a new one
        AuthorizedActionChangeRequest createRequest = new AuthorizedTransitionActionCreateRequest(this.deviceLifeCycle, eventType, this.infoForEdit);
        return createRequest.perform();
    }
}
