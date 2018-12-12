/*
 * PhysicalLayer.java
 *
 * Created on 18 juli 2006, 13:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.osiframework;

import com.energyict.dialer.connection.*;
import com.energyict.dialer.core.*;
import java.io.*;

/**
 *
 * @author Koen
 */
public class PhysicalLayer extends AbstractLayer {
    
    PhysicalLayerConnection physicalLayerConnection;
            
    /** Creates a new instance of PhysicalLayer */
    public PhysicalLayer(LayerManager lm) {
        super(lm);
        
    }
    
    public void init(InputStream inputStream,
                     OutputStream outputStream,
                     int timeout,
                     int maxRetries,
                     long forcedDelay,
                     int echoCancelling,
                     HalfDuplexController halfDuplexController,
                     String serialNumber,
                     int securityLevel) throws ConnectionException {
        
        physicalLayerConnection = new PhysicalLayerConnection(inputStream,
                                                              outputStream,
                                                              forcedDelay,
                                                              echoCancelling,
                                                              halfDuplexController,
                                                              this);
        
    } // public void init
    
    public void sendDPDU(byte[] data) throws ConnectionException {
        if (getLayerManager().getDebug()>=1) System.out.println("PhysicalLayer, sendDPDU(()"); 
        if (physicalLayerConnection != null)
            physicalLayerConnection.sendOut(data);
        
    } // public void sendDPDU(byte[] data)
    
} // public class PhysicalLayer extends AbstractLayer
