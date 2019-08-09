/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.impl.resource.requests;

import com.energyict.mdc.common.device.lifecycle.config.AuthorizedAction;

public class AuthorizedActionUnsupportedRequest implements AuthorizedActionChangeRequest{
    @Override
    public AuthorizedAction perform() {
        throw new UnsupportedOperationException("This type of change is not supported yet");
    }
}
