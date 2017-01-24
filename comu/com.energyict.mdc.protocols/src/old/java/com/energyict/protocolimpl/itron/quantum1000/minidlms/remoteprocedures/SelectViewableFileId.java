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

import com.energyict.protocolimpl.itron.quantum1000.minidlms.RemoteProcedureCallFactory;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.ViewableFileId;

/**
 *
 * @author Koen
 */
public class SelectViewableFileId extends AbstractViewRpc {

    private ViewableFileId viewableFileId;

    /** Creates a new instance of SelectViewableFileId */
    public SelectViewableFileId(RemoteProcedureCallFactory remoteProcedureCallFactory) {
        super(remoteProcedureCallFactory);
    }


    protected int getSubfunction() {
        return getVRPC_SELECT_OBJECT_ID();
    }

    protected byte[] getSubfunctionParameters() {
        return new byte[]{(byte)(getViewableFileId().getId()>>8),(byte)(getViewableFileId().getId())};
    }

    public ViewableFileId getViewableFileId() {
        return viewableFileId;
    }

    public void setViewableFileId(ViewableFileId viewableFileId) {
        this.viewableFileId = viewableFileId;
    }
}
