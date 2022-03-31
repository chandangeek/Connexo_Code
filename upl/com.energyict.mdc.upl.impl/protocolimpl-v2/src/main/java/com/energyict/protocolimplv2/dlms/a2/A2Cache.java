package com.energyict.protocolimplv2.dlms.a2;

import com.energyict.dlms.DLMSCache;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.cache.DeviceProtocolCacheXmlMarshallAdapter;
import com.energyict.protocol.FrameCounterCache;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
@XmlJavaTypeAdapter(DeviceProtocolCacheXmlMarshallAdapter.class)
public class A2Cache extends DLMSCache implements DeviceProtocolCache, FrameCounterCache, Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@link Map} containing the frame counters for the device.
     */
    private final Map<Integer, Long> clientIdToFrameCounter = new HashMap<>();

    /**
     * Create a new instance.
     */
    public A2Cache() {
    }

    @Override
    public void setTXFrameCounter(int clientId, long frameCounter) {
        clientIdToFrameCounter.put(clientId, frameCounter);
        setChanged(true);
    }

    @Override
    public long getTXFrameCounter(int clientId) {
        if (clientIdToFrameCounter.containsKey(clientId)) {
            return clientIdToFrameCounter.get(clientId);
        } else {
            return -1;
        }
    }

}
