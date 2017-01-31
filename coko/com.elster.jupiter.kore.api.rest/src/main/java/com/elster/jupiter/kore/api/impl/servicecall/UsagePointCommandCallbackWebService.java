/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.impl.servicecall;


import javax.ws.rs.client.WebTarget;

public class UsagePointCommandCallbackWebService {

    private final WebTarget target;

    public UsagePointCommandCallbackWebService(WebTarget target) {
        this.target = target;
    }
}
