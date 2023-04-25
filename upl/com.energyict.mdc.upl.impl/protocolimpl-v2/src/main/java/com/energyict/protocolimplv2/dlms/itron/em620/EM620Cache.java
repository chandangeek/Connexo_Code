package com.energyict.protocolimplv2.dlms.itron.em620;

import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.cache.DeviceProtocolCacheXmlMarshallAdapter;

import com.energyict.dlms.DLMSCache;
import com.energyict.protocol.FrameCounterCache;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
@XmlJavaTypeAdapter(DeviceProtocolCacheXmlMarshallAdapter.class)
public class EM620Cache extends DLMSCache implements DeviceProtocolCache, FrameCounterCache, Serializable {
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Indicates whether or not the cache has changed.
     */
    private volatile boolean changed;

    /**
     * {@link Map} containing the frame counters for the gateway.
     */
    private Map<Integer, Long> frameCountersGateway = new HashMap<>();

    /**
     * Indicate the cache has changed.
     *
     * @param changed <code>true</code> if the cache has changed, <code>false</code> if not.
     */
    public final void setChanged(final boolean changed) {
        this.changed = changed;
    }

    @Override
    public final boolean contentChanged() {
        return this.changed;
    }

    @Override
    public void setTXFrameCounter(int clientId, long frameCounter) {
        frameCountersGateway.put(clientId, frameCounter);
        setChanged(true);
    }

    @Override
    public long getTXFrameCounter(int clientId) {
        if (frameCountersGateway.containsKey(clientId)) {
            return frameCountersGateway.get(clientId);
        } else {
            return -1;
        }
    }
}
