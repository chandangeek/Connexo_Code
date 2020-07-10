package com.energyict.protocolimplv2.nta.dsmr23.Iskra;

import com.energyict.dlms.DLMSCache;
import com.energyict.mdc.upl.cache.DeviceProtocolCacheXmlMarshallAdapter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlJavaTypeAdapter(DeviceProtocolCacheXmlMarshallAdapter.class)
@XmlRootElement
public class Mx382Cache extends DLMSCache {

    private long frameCounter = 1;

    public Mx382Cache() {
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