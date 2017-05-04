/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3;

import com.energyict.protocolimpl.dlms.common.ProfileCache;

public class G3Cache extends ProfileCache {

    private long frameCounter;

    public G3Cache() {
        this.frameCounter = 1;
    }

    public long getFrameCounter() {
        return frameCounter;
    }

    public void setFrameCounter(long frameCounter) {
        this.frameCounter = frameCounter;
    }
}
