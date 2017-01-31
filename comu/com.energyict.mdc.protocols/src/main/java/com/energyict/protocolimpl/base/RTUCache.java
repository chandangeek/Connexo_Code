/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.base;

import com.energyict.protocols.mdc.services.impl.OrmClient;

import java.io.IOException;
import java.sql.SQLException;

public class RTUCache {

    private int deviceId;
    private final OrmClient ormClient;

    public RTUCache(int deviceId, OrmClient ormClient) {
        this.deviceId = deviceId;
        this.ormClient = ormClient;
    }

    public Object getCacheObject() throws IOException {
        return this.ormClient.getCacheObject(this.deviceId);
    }

    public synchronized void setBlob(final Object cacheObject) throws SQLException {
        this.ormClient.setCacheObject(this.deviceId, cacheObject);
	}

}