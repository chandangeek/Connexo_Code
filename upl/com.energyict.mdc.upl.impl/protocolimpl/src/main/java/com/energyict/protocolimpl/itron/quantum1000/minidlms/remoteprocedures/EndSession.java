/*
 * SelectViewableFileId.java
 *
 * Created on 19 december 2006, 14:24
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms.remoteprocedures;

import com.energyict.protocolimpl.itron.quantum1000.minidlms.*;

/**
 *
 * @author Koen
 */
public class EndSession extends AbstractViewRpc {
    

    /** Creates a new instance of SelectViewableFileId */
    public EndSession(RemoteProcedureCallFactory remoteProcedureCallFactory) {
        super(remoteProcedureCallFactory);
    }
    
    
    protected int getSubfunction() {
        return getVRPC_END_SESSION();
    }
    
    protected byte[] getSubfunctionParameters() {
        return null;
    }

}
