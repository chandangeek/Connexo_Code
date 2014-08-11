package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.protocol.api.DeviceProtocolCache;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Adapter object to wrap the cache from a legacy protocol to a new {@link DeviceProtocolCache}
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/08/12
 * Time: 14:35
 */
@XmlRootElement(name = "DeviceProtocolCacheAdapter")
@XmlAccessorType(XmlAccessType.FIELD)
public class DeviceProtocolCacheAdapter implements DeviceProtocolCache {

    private static final String legacyDlmsCacheCheck = "<changed>false</changed>";

    @XmlElement(name = "LegacyJson")
    private String jsonCache;

    public DeviceProtocolCacheAdapter() {
    }

    @Override
    public boolean contentChanged() {
        return !this.jsonCache.contains(legacyDlmsCacheCheck);
    }

    public String getLegacyJsonCache() {
        return jsonCache;
    }

    public void setLegacyJsonCache(String legacyJsonCache) {
        this.jsonCache = legacyJsonCache;
    }
}
