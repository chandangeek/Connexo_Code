/*
 * SelectViewId.java
 *
 * Created on 19 december 2006, 14:24
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms.remoteprocedures;

import com.energyict.protocolimpl.itron.quantum1000.minidlms.RemoteProcedureCallFactory;

/**
 *
 * @author Koen
 */
public class SelectViewId extends AbstractViewRpc {

    private long viewId;

    /** Creates a new instance of SelectViewId */
    public SelectViewId(RemoteProcedureCallFactory remoteProcedureCallFactory) {
        super(remoteProcedureCallFactory);
    }


    protected int getSubfunction() {
        return getVRPC_SELECT_VIEW_ID();
    }

    protected byte[] getSubfunctionParameters() {
        byte[] data = new byte[2];

        data[0] = (byte)(viewId>>8);
        data[1] = (byte)(viewId);

        return data;
    }

    public long getViewId() {
        return viewId;
    }

    public void setViewId(long viewId) {
        this.viewId = viewId;
    }


}
