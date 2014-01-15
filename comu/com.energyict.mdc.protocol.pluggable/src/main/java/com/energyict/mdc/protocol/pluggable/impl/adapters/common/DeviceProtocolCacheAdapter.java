package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.protocol.api.DeviceProtocolCache;

/**
 * Adapter object to wrap the cache from a legacy protocol to a new {@link DeviceProtocolCache}
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/08/12
 * Time: 14:35
 */
public class DeviceProtocolCacheAdapter implements DeviceProtocolCache {

    private Object legacyCache;

    public DeviceProtocolCacheAdapter() {
    }

    @Override
    public boolean contentChanged() {
        return true;    //always return true so the update is always performed
    }

    public Object getLegacyCache() {
        return legacyCache;
    }

    public void setLegacyCache(Object legacyCache) {
        this.legacyCache = legacyCache;
    }
}
