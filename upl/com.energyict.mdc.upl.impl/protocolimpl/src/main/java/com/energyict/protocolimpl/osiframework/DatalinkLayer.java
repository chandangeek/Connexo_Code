/*
 * DatalinkLayer.java
 *
 * Created on 18 juli 2006, 13:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.osiframework;

import com.energyict.dialer.connection.*;

/**
 *
 * @author Koen
 */
public class DatalinkLayer extends AbstractLayer {
    
    /** Creates a new instance of DatalinkLayer */
    public DatalinkLayer(LayerManager lm) {
        super(lm);
    }
    
    public void sendNPDU(byte[] data)throws ConnectionException {
        if (getLayerManager().getDebug()>=1) System.out.println("DatalinkLayer, sendNPDU()"); 
        getLayerManager().getPhysicalLayer().sendDPDU(data);
    }    
}
