/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RemoteProcedureCallFactory.java
 *
 * Created on 8 december 2006, 18:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocolimpl.itron.quantum1000.minidlms.remoteprocedures.EndSession;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.remoteprocedures.IsFeatureEnabled;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.remoteprocedures.IsFeatureKnown;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.remoteprocedures.MaximizeRecsPerRead;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.remoteprocedures.SelectDefaultViewId;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.remoteprocedures.SelectViewId;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.remoteprocedures.SelectViewableFileId;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.remoteprocedures.SetRecordGreaterThanOrEqualToTime;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.remoteprocedures.SetRestrictions;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.remoteprocedures.SetSourceId;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.remoteprocedures.StartExclusiveViewSession;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class RemoteProcedureCallFactory {

    private ProtocolLink protocolLink;

    /** Creates a new instance of RemoteProcedureCallFactory */
    public RemoteProcedureCallFactory(ProtocolLink protocolLink) {
        this.setProtocolLink(protocolLink);
    }

    public boolean isFeatureEnabled(int featureId) throws IOException {
        IsFeatureEnabled isfe = new IsFeatureEnabled(this);
        isfe.setFeatureId(featureId);
        try {
            isfe.invoke();
            return true;
        }
        catch(ReplyException e) {
            if (e.getAbstractReplyDataError() instanceof WriteReplyDataError) {
                return false;
            }
            throw e;
        }

    }

    public boolean isFeatureKnown(int featureId) throws IOException {
        IsFeatureKnown isfk = new IsFeatureKnown(this);
        isfk.setFeatureId(featureId);
        try {
            isfk.invoke();
            return true;
        }
        catch(ReplyException e) {
            if (e.getAbstractReplyDataError() instanceof WriteReplyDataError) {
                return false;
            }
            throw e;
        }

    }

    public void endSession() throws IOException {
        EndSession rpc = new EndSession(this);
        rpc.invoke();
    }

    public void maximizeRecsPerRead() throws IOException {
        MaximizeRecsPerRead rpc = new MaximizeRecsPerRead(this);
        rpc.invoke();
    }

    public void selectViewableFileId(ViewableFileId viewableFileId) throws IOException {
        SelectViewableFileId rpc = new SelectViewableFileId(this);
        rpc.setViewableFileId(viewableFileId);
        rpc.invoke();
    }

    public void setRestrictions() throws IOException {
        SetRestrictions rpc = new SetRestrictions(this);
        rpc.setRestrictions(new byte[]{(byte)0xff,(byte)0xff,0,0,0,0,0,0,0,0,0,0});
        rpc.invoke();
    }

    public void setRecordGreaterThanOrEqualToTime(Date date) throws IOException {
        SetRecordGreaterThanOrEqualToTime rpc = new SetRecordGreaterThanOrEqualToTime(this);
        rpc.setDate(date);
        rpc.invoke();
    }

    public void setSourceId() throws IOException {
        SetSourceId rpc = new SetSourceId(this);
        rpc.setSourceId(getProtocolLink().getMiniDLMSConnection().getClientAddress());
        rpc.invoke();
    }

    public void selectViewId() throws IOException {
        SelectViewId rpc = new SelectViewId(this);
        rpc.setViewId(0x005B);
        rpc.invoke();
    }


    public void startExclusiveViewSession() throws IOException {
        StartExclusiveViewSession rpc = new StartExclusiveViewSession(this);
        rpc.invoke();
    }

    public void selectDefaultViewId() throws IOException {
        SelectDefaultViewId rpc = new SelectDefaultViewId(this);
        rpc.invoke();
    }

    public ProtocolLink getProtocolLink() {
        return protocolLink;
    }

    public void setProtocolLink(ProtocolLink protocolLink) {
        this.protocolLink = protocolLink;
    }

}
