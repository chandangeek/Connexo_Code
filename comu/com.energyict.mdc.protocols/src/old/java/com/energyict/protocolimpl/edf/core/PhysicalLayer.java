/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * PhysicalLayer.java
 *
 * Created on 29 juni 2006, 10:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.core;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class PhysicalLayer {

    LayerManager layerManager=null;
    byte[] frame;

    /** Creates a new instance of PhysicalLayer */
    public PhysicalLayer(LayerManager layerManager) {
        this.layerManager=layerManager;
    }


    public void phyAbort() {

    }

    public void phyRequestSendData(byte[] frame) throws IOException {
        // request from datalink layer to send data
        this.frame=frame;
        //stateMachine(EVENT_DL_REQUEST);
    }
}
