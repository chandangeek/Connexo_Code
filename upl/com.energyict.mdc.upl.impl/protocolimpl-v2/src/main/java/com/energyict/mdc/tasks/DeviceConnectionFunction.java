/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks;

import com.energyict.mdc.upl.UPLConnectionFunction;

import com.energyict.protocolimplv2.ConnectionFunctionKeys;

/**
 * Protocolimpl-v2 specific implementation of the {@link UPLConnectionFunction} interface<br/>
 *
 * @author Stijn Vanhoorelbeke
 * @since 19.06.17 - 15:39
 */
public enum DeviceConnectionFunction implements UPLConnectionFunction {

    GATEWAY(1, ConnectionFunctionKeys.GATEWAY),
    MIRROR(2, ConnectionFunctionKeys.MIRROR),
    INBOUND(3, ConnectionFunctionKeys.INBOUND);

    private final long id;
    private final ConnectionFunctionKeys key;

    DeviceConnectionFunction(long id, ConnectionFunctionKeys key) {
        this.id = id;
        this.key = key;
    }

    public String getConnectionFunctionName() {
        return this.key.name();
    }

    @Override
    public long getId() {
        return id;
    }
}