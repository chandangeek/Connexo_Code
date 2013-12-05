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
import com.energyict.protocolimpl.itron.quantum1000.minidlms.Utils;

import java.util.Date;

/**
 *
 * @author Koen
 */
public class SetRecordGreaterThanOrEqualToTime extends AbstractViewRpc {

    private Date date;

    /** Creates a new instance of SelectViewableFileId */
    public SetRecordGreaterThanOrEqualToTime(RemoteProcedureCallFactory remoteProcedureCallFactory) {
        super(remoteProcedureCallFactory);
    }


    protected int getSubfunction() {
        return getVRPC_SELECT_RECORD_GREATERTHANOREQUALTO_TIME();
    }

    protected byte[] getSubfunctionParameters() {
        return Utils.getDateTimeExtendedFromDate(getDate(),getRemoteProcedureCallFactory().getProtocolLink().getProtocol().getTimeZone());
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


}
