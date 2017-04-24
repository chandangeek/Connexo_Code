/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RecordTemplate.java
 *
 * Created on 28 oktober 2005, 17:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.TableFactory;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class CallScheduleRecord {


    private int retriesRemaining; // 1 byte Number of retries remaining
    private int originateControl; // 1 byte b0-2: phone number identifier (0-2)
                                                             // b3: call pending. 1 = call pending to this number
                                                             // b4-6: unused
                                                             // b7: useWindows. 1 = use call windows Call Purpose 1
                                                             // b0: outage call
                                                             // b1: restoration call
                                                             // b2: billing call
                                                             // b3: alarm call
                                                             // b4: immediate call
                                                             // b5-7: unused
    private int callPurpose; // 1 byte b0: outage call
                                              // b1: restoration call
                                              // b2: billing call
                                              // b3: alarm call
                                              // b4: immediate call
                                              // b5-7: unused


    /** Creates a new instance of SourceDefinitionEntry */
    public CallScheduleRecord(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        setRetriesRemaining(C12ParseUtils.getInt(data,offset++));
        setOriginateControl(C12ParseUtils.getInt(data,offset++));
        setCallPurpose(C12ParseUtils.getInt(data,offset++));

    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CallScheduleRecord:\n");
        strBuff.append("   callPurpose="+getCallPurpose()+"\n");
        strBuff.append("   originateControl="+getOriginateControl()+"\n");
        strBuff.append("   retriesRemaining="+getRetriesRemaining()+"\n");
        return strBuff.toString();
    }



    static public int getSize(TableFactory tableFactory) throws IOException {
        return 3;
    }

    public int getRetriesRemaining() {
        return retriesRemaining;
    }

    public void setRetriesRemaining(int retriesRemaining) {
        this.retriesRemaining = retriesRemaining;
    }

    public int getOriginateControl() {
        return originateControl;
    }

    public void setOriginateControl(int originateControl) {
        this.originateControl = originateControl;
    }

    public int getCallPurpose() {
        return callPurpose;
    }

    public void setCallPurpose(int callPurpose) {
        this.callPurpose = callPurpose;
    }


}
