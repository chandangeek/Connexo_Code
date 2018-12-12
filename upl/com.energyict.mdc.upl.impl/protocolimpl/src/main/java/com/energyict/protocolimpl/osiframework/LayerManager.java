/*
 * LayerManager.java
 *
 * Created on 18 juli 2006, 13:27
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.osiframework;

/**
 *
 * @author Koen
 */
public class LayerManager {
    
    final int DEBUG=1;
    
    private ApplicationLayer applicationLayer;
    private NetworkLayer networkLayer;
    private DatalinkLayer datalinkLayer;
    private PhysicalLayer physicalLayer;
    private TimerManager timerManager;
    
    /** Creates a new instance of LayerManager */
    public LayerManager() {
    }
    
    public void init() {
        setApplicationLayer(new ApplicationLayer(this));
        setNetworkLayer(new NetworkLayer(this));
        setDatalinkLayer(new DatalinkLayer(this));
        setPhysicalLayer(new PhysicalLayer(this));
        setTimerManager(new TimerManager(this));
    }
    
    public ApplicationLayer getApplicationLayer() {
        return applicationLayer;
    }

    private void setApplicationLayer(ApplicationLayer applicationLayer) {
        this.applicationLayer = applicationLayer;
    }

    public NetworkLayer getNetworkLayer() {
        return networkLayer;
    }

    private void setNetworkLayer(NetworkLayer networkLayer) {
        this.networkLayer = networkLayer;
    }

    public DatalinkLayer getDatalinkLayer() {
        return datalinkLayer;
    }

    private void setDatalinkLayer(DatalinkLayer datalinkLayer) {
        this.datalinkLayer = datalinkLayer;
    }

    public PhysicalLayer getPhysicalLayer() {
        return physicalLayer;
    }

    private void setPhysicalLayer(PhysicalLayer physicalLayer) {
        this.physicalLayer = physicalLayer;
    }

    public TimerManager getTimerManager() {
        return timerManager;
    }

    private void setTimerManager(TimerManager timerManager) {
        this.timerManager = timerManager;
    }
    
    public int getDebug() {
        return DEBUG;
    }
    
}
