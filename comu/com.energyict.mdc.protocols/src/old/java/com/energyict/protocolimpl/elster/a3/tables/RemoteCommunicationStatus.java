/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RemoteCommunicationStatus.java
 *
 * Created on 13/02/2006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class RemoteCommunicationStatus extends AbstractTable {


    /*
    Memory storage: Combination of RAM and EEPROM
    Total table size: (bytes) 44
    Read access: 1
    Write access: N/A

    This table provides communication status information. When a call is triggered to a phone
    number the meter "ORs" the call purpose into the corresponding Call Purpose field, and resets
    the retry attempts to the larger of the remaining retry counts or the number of retries for the
    new call.
    For calls initiated by the meter, the Call Complete procedure only clears call status for the
    dialed phone number based on the ST/MT-93 phone number index. For incoming calls, the
    Call Complete procedure will only clear call status for calls pending on the remote port of the
    received call. When the Call Complete procedure is received optically, call status for both
    ports will be cleared.
    */

    private PortStatus[] portStatus; //There is a PortStatus entry for remote port 1 and remote port 2.

    /** Creates a new instance of RemoteCommunicationStatus */
    public RemoteCommunicationStatus(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(91,true));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuilder strBuff = new StringBuilder();
        strBuff.append("RemoteCommunicationStatus:\n");
        for (int i=0;i<getPortStatus().length;i++)
            strBuff.append("   portStatus["+i+"]="+getPortStatus()[i]+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int offset = 0;
        setPortStatus(new PortStatus[2]);
        for (int i=0;i<getPortStatus().length;i++) {
            getPortStatus()[i] = new PortStatus(tableData, offset, getTableFactory());
            offset += PortStatus.getSize(getTableFactory());
        }
    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

    public PortStatus[] getPortStatus() {
        return portStatus;
    }

    public void setPortStatus(PortStatus[] portStatus) {
        this.portStatus = portStatus;
    }


}
