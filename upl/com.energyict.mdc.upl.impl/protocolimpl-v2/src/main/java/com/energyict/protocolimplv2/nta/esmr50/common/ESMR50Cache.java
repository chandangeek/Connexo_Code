package com.energyict.protocolimplv2.nta.esmr50.common;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.UniversalObject;
import com.energyict.mdc.upl.cache.DeviceProtocolCacheXmlMarshallAdapter;
import com.energyict.protocol.FrameCounterCache;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.Map;

@XmlJavaTypeAdapter(DeviceProtocolCacheXmlMarshallAdapter.class)
@XmlRootElement
public class ESMR50Cache extends DLMSCache implements FrameCounterCache {

    /**
     * {@link Map} containing the frame counters for the mirror.
     */
    protected Map<Integer, Long> frameCountersMirror = new HashMap<>();

    public ESMR50Cache(UniversalObject[] objectList, int confProgChange) {
        super(objectList, confProgChange);
    }

    public ESMR50Cache() {
        super();
    }

    @Override
    public void setTXFrameCounter(int clientId, long frameCounter) {
        frameCountersMirror.put(clientId, frameCounter);
    }

    @Override
    public long getTXFrameCounter(int clientId) {
        if(frameCountersMirror.containsKey(clientId)){
            return frameCountersMirror.get(clientId);
        }
        return -1;
    }

    public void setFrameCounter(long l) {
    }
}