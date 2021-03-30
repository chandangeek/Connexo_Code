package com.energyict.protocolimplv2.dlms.common.framecounter;

import com.energyict.dlms.DLMSCache;
import com.energyict.mdc.upl.cache.DeviceProtocolCacheXmlMarshallAdapter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.Map;


@XmlRootElement
@XmlJavaTypeAdapter(DeviceProtocolCacheXmlMarshallAdapter.class)
public class FrameCounterCache extends DLMSCache implements com.energyict.protocol.FrameCounterCache {

    protected Map<Integer, Long> frameCounters = new HashMap<>();

    @Override
    public void setTXFrameCounter(final int clientId, long frameCounter) {
        frameCounters.put(clientId, frameCounter);
        setChanged(true);
    }

    @Override
    public long getTXFrameCounter(final int clientId) {
       return frameCounters.getOrDefault(clientId, DEFAULT_FC);
    }

}
