/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.UniversalObject;

import javax.xml.bind.annotation.XmlRootElement;

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