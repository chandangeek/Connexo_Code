package com.energyict.protocolimplv2.dlms.as3000.dlms;

import com.energyict.dlms.DLMSCache;
import com.energyict.mdc.upl.cache.DeviceProtocolCacheXmlMarshallAdapter;
import com.energyict.protocol.FrameCounterCache;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.Map;


@XmlRootElement
@XmlJavaTypeAdapter(DeviceProtocolCacheXmlMarshallAdapter.class)
public class AS3000Cache extends DLMSCache implements FrameCounterCache {

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
