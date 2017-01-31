/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.ace4000.requests.tracking;

public enum RequestState {

    Sent(null),
    Success(true),
    Fail(false);

    private Boolean success;

    RequestState(Boolean success) {
        this.success = success;
    }

    public Boolean getSuccess() {
        return success;
    }
}