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
public class ESMR50Cache extends DLMSCache {

    private long frameCounter = 1;

    public ESMR50Cache(UniversalObject[] objectList, int confProgChange) {
        super(objectList, confProgChange);
    }

    public ESMR50Cache() {
        super();
    }

    public long getFrameCounter() {
        return frameCounter;
    }

    public void setFrameCounter(long frameCounter) {
        if (this.frameCounter != frameCounter) {
            this.frameCounter = frameCounter;
            setChanged(true);
        }
    }
}