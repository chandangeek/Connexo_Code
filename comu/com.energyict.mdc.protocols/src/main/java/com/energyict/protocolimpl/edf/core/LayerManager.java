/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * LayerManager.java
 *
 * Created on 29 juni 2006, 10:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.core;

/**
 *
 * @author Koen
 */
public class LayerManager {


    
    private PhysicalLayer physicalLayer=null;
    private DatalinkLayer datalinkLayer=null;
    private SessionLayer sessionLayer=null;
    private TrimeranConnectionLayering connection;
    
    /** Creates a new instance of LayerManager */
    public LayerManager(TrimeranConnectionLayering connection) {
        this.setConnection(connection);
    }
    
    public void init() {

        physicalLayer = new PhysicalLayer(this);
        datalinkLayer = new DatalinkLayer(this);
        sessionLayer = new SessionLayer(this);
    }

    public PhysicalLayer getPhysicalLayer() {
        return physicalLayer;
    }

    private void setPhysicalLayer(PhysicalLayer physicalLayer) {
        this.physicalLayer = physicalLayer;
    }

    public DatalinkLayer getDatalinkLayer() {
        return datalinkLayer;
    }

    private void setDatalinkLayer(DatalinkLayer datalinkLayer) {
        this.datalinkLayer = datalinkLayer;
    }

    public SessionLayer getSessionLayer() {
        return sessionLayer;
    }

    private void setSessionLayer(SessionLayer sessionLayer) {
        this.sessionLayer = sessionLayer;
    }

    public TrimeranConnectionLayering getConnection() {
        return connection;
    }

    private void setConnection(TrimeranConnectionLayering connection) {
        this.connection = connection;
    }

}
