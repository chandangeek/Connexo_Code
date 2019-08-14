/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.common.protocol.ConnectionFunction;

/**
 * @author Stijn Vanhoorelbeke
 * @since 22.06.17 - 16:19
 */
public class ConnectionFunctionInfo {
    public long id;
    public String localizedValue;
    public boolean alreadyUsed;

    public ConnectionFunctionInfo() {
    }

    public ConnectionFunctionInfo(ConnectionFunction connectionFunction) {
        this.id = connectionFunction.getId();
        this.localizedValue = connectionFunction.getConnectionFunctionDisplayName();
        this.alreadyUsed = false;
    }

    public ConnectionFunctionInfo(ConnectionFunction connectionFunction, boolean alreadyUsed) {
        this.id = connectionFunction.getId();
        this.localizedValue = connectionFunction.getConnectionFunctionDisplayName();
        this.alreadyUsed = false;
        this.alreadyUsed = alreadyUsed;
    }
}
