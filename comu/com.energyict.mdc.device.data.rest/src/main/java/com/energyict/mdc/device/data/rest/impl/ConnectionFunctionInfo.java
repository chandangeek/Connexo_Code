/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.protocol.api.ConnectionFunction;

/**
 * @author Stijn Vanhoorelbeke
 * @since 22.06.17 - 16:19
 */
public class ConnectionFunctionInfo {
    public long id;
    public String localizedValue;

    public ConnectionFunctionInfo() {
    }

    public ConnectionFunctionInfo(ConnectionFunction connectionFunction) {
        this.id = connectionFunction.getId();
        this.localizedValue = connectionFunction.getConnectionFunctionDisplayName();
    }
}
