/*
 * AbstractLayer.java
 *
 * Created on 18 juli 2006, 13:27
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
abstract public class AbstractLayer implements StateMachineCallBack {
    
    private LayerManager layerManager;
    
    /** Creates a new instance of AbstractLayer */
    public AbstractLayer(LayerManager layerManager) {
        this.setLayerManager(layerManager);
    }

    public LayerManager getLayerManager() {
        return layerManager;
    }

    public void setLayerManager(LayerManager layerManager) {
        this.layerManager = layerManager;
    }
    
    public int receiving() throws IOException {
        
        getLayerManager().getTimerManager().checkTimers();
        return 0;
    }
}
