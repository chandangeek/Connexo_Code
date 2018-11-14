package com.energyict.smartmeterprotocolimpl.nta.esmr50.common.registers;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.UniversalObject;


@Deprecated
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