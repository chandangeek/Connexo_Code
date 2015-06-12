package com.energyict.protocolimpl.dlms.g3;

import com.energyict.protocolimpl.dlms.common.ProfileCacheImpl;

/**
 * Copyrights EnergyICT
 * Date: 23/10/12
 * Time: 13:53
 * Author: khe
 */
public class G3Cache extends ProfileCacheImpl {

    private long frameCounter = 1;

    public long getFrameCounter() {
        return frameCounter;
    }

    public void setFrameCounter(long frameCounter) {
        this.frameCounter = frameCounter;
    }
}
