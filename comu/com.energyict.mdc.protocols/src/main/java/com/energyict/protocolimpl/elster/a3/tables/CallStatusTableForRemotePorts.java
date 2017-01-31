/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * CallStatusTableForRemotePorts.java
 *
 * Created on 13/02/2006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class CallStatusTableForRemotePorts extends AbstractTable {

    /*
    Memory storage: Registered memory (RAM saved on power fail)
    Total table size: (bytes) 3
    Read access: 1
    Write access: N/A

    This table provides the call status for the most recent call for each phone number. The Remote
    Call Complete procedure (MP-12) only clears status for the phone number dialed. The Clear
    Data Procedure (MP-3) clears the entire table.
    */

    private int callStatus1; // 1 byte Call status for phone number 1. The meter will post the following status information:
                                              // 0 no phone call made since last Reset Status procedure
                                              // 1 phone call in progress
                                              // 3 waiting for a connection
                                              // 4 communicating
                                              // 5 completed normally
                                              // 6 not completed
                                              // 7 not completed, line busy
                                              // 8 not completed, no dial tone
    private int callStatus2; // 1 byte Call status for phone number 2.
    private int callStatus3; // 1 byte Call status for phone number 3.



    /** Creates a new instance of CallStatusTableForRemotePorts */
    public CallStatusTableForRemotePorts(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(97,true));
    }

    public String toString() {
        return "CallStatusTableForRemotePorts:\n" +
                "   callStatus1=" + getCallStatus1() + "\n" +
                "   callStatus2=" + getCallStatus2() + "\n" +
                "   callStatus3=" + getCallStatus3() + "\n";
    }

    protected void parse(byte[] tableData) throws IOException {
        int offset = 0;
        setCallStatus1(C12ParseUtils.getInt(tableData,offset++));
        setCallStatus2(C12ParseUtils.getInt(tableData,offset++));
        setCallStatus3(C12ParseUtils.getInt(tableData,offset++));
    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

    public int getCallStatus1() {
        return callStatus1;
    }

    public void setCallStatus1(int callStatus1) {
        this.callStatus1 = callStatus1;
    }

    public int getCallStatus2() {
        return callStatus2;
    }

    public void setCallStatus2(int callStatus2) {
        this.callStatus2 = callStatus2;
    }

    public int getCallStatus3() {
        return callStatus3;
    }

    public void setCallStatus3(int callStatus3) {
        this.callStatus3 = callStatus3;
    }


}
