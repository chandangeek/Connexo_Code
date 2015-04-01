package com.energyict.mdc.device.lifecycle.config.rest.resource.requests;

import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;

public class AuthorizedActionUnsupportedRequest implements AuthorizedActionChangeRequest{
    @Override
    public AuthorizedAction perform() {
        throw new UnsupportedOperationException("This type of change is not supported yet");
    }
}
