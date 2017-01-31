/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.impl.resource.requests;

import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;

public interface AuthorizedActionChangeRequest {
    AuthorizedAction perform();
}
