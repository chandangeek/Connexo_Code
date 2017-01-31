/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AbstractRemoteProcedure.java
 *
 * Created on 8 december 2006, 18:03
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms.remoteprocedures;

import com.energyict.protocolimpl.itron.quantum1000.minidlms.RemoteProcedureCallFactory;

import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class AbstractRemoteProcedure {

    abstract public int getFunctionName();
    abstract public byte[] getParameters();

    private RemoteProcedureCallFactory remoteProcedureCallFactory;

    /** Creates a new instance of AbstractRemoteProcedure */
    public AbstractRemoteProcedure(RemoteProcedureCallFactory remoteProcedureCallFactory) {
        this.setRemoteProcedureCallFactory(remoteProcedureCallFactory);
    }

    public void invoke() throws IOException {
        getRemoteProcedureCallFactory().getProtocolLink().getDataDefinitionFactory().setRemoteProcedureCall(this);
    }

    public RemoteProcedureCallFactory getRemoteProcedureCallFactory() {
        return remoteProcedureCallFactory;
    }

    public void setRemoteProcedureCallFactory(RemoteProcedureCallFactory remoteProcedureCallFactory) {
        this.remoteProcedureCallFactory = remoteProcedureCallFactory;
    }

}
