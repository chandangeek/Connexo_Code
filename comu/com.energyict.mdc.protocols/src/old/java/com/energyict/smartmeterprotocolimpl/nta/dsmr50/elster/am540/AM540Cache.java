package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540;

import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.DLMSCache;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Extension of the normal DLMSCache, it also remembers our last frame counter used in the previous session
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 12/06/2014 - 15:42
 */
@XmlRootElement
public class AM540Cache extends DLMSCache {

    private long frameCounter = 1;

    public AM540Cache(UniversalObject[] objectList, int confProgChange) {
        super(objectList, confProgChange);
    }

    public AM540Cache() {
        super();
    }

    public long getFrameCounter() {
        return frameCounter;
    }

    public void setFrameCounter(long frameCounter) {
        this.frameCounter = frameCounter;
    }
}