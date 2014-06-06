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

    @XmlElement(name = "LegacyJson")
    private String jsonCache;

    public DeviceProtocolCacheAdapter() {
    }

    @Override
    public boolean contentChanged() {
        return true;    //always return true so the update is always performed
    }

    public String getLegacyJsonCache() {
        // TODO fetch for the actual object ...
        return jsonCache;
    }

    public void setLegacyJsonCache(String legacyJsonCache) {
        this.jsonCache = legacyJsonCache;
    }
}
