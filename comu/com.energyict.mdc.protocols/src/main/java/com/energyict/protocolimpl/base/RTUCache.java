package com.energyict.protocolimpl.base;

import com.energyict.mdc.common.BusinessException;

import com.energyict.protocols.mdc.services.impl.Bus;

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

    public RTUCache(int deviceId) {
        this.deviceId = deviceId;
    }

    public Object getCacheObject() throws IOException {
        return Bus.getOrmClient().getCacheObject(this.deviceId);
    }

    public synchronized void setBlob(final Object cacheObject) throws SQLException, BusinessException {
        Bus.getOrmClient().setCacheObject(this.deviceId, cacheObject);
	}

}