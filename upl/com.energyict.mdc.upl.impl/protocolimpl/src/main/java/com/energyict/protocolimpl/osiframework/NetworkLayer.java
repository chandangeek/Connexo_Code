/*
 * NetworkLayer.java
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
public class NetworkLayer extends AbstractLayer {
    
    /** Creates a new instance of NetworkLayer */
    public NetworkLayer(LayerManager lm) {
        super(lm);
    }
    
    public void sendAPDU(byte[] data) throws ConnectionException {
        if (getLayerManager().getDebug()>=1) System.out.println("NetworkLayer, sendAPDU(()"); 
        
        getLayerManager().getDatalinkLayer().sendNPDU(data);
    }
    
}
