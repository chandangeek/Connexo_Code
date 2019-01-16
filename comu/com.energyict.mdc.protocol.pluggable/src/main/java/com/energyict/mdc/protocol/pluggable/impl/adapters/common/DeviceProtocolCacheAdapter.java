/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.upl.cache.DeviceProtocolCache;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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

    @Override
    public void setContentChanged(boolean changed) {
        // Ignore, dirty aspect is driven by the wrapped json String
    }

    public String getLegacyJsonCache() {
        return jsonCache;
    }

    public void setLegacyJsonCache(String legacyJsonCache) {
        this.jsonCache = legacyJsonCache;
    }
}