package com.energyict.protocolimpl.base;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Provides functionality to have access to the CES_DEVICECACHE table.
 *
 * Copyrights EnergyICT
 * Date: 20-mei-2010
 * Time: 11:29:25
 */
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