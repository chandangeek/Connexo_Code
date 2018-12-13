/*
 * ApplicationLayer.java
 *
 * Created on 18 juli 2006, 13:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.osiframework;

import java.io.*;

/**
 *
 * @author Koen
 */
public class ApplicationLayer extends AbstractLayer {
    
    
    
    /** Creates a new instance of ApplicationLayer */
    public ApplicationLayer(LayerManager lm) {
        super(lm);
    }
    
    public void sendCommand(byte[] data) throws IOException {
        if (getLayerManager().getDebug()>=1) System.out.println("ApplicationLayer, sendCommand()"); 
        getLayerManager().getTimerManager().startTimer(0);
        getLayerManager().getNetworkLayer().sendAPDU(data);
    }
    
    
}
