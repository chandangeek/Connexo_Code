/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.upl.cache;

import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.cache.DeviceProtocolCacheXmlMarshallAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlJavaTypeAdapter(DeviceProtocolCacheXmlMarshallAdapter.class)
public class DeviceProtocolCacheAdapter implements DeviceProtocolCache {

    private static final String legacyDlmsCacheCheck = "<changed>false</changed>";

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

    @XmlAttribute
    public String getLegacyJsonCache() {
        return jsonCache;
    }

    public void setLegacyJsonCache(String legacyJsonCache) {
        this.jsonCache = legacyJsonCache;
    }
}