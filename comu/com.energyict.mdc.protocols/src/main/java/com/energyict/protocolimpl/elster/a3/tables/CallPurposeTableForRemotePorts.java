/*
 * CallPurposeTableForRemotePorts.java
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
public class CallPurposeTableForRemotePorts extends AbstractTable {


    /*
    Memory storage: RAM
    Total table size: (bytes) Length of ST-3 + 2
    Read access: 0
    Write access: N/A

    This table provides status information for the most recent call originated by the meter. The
    meter will keep status for each phone number and will update ST/MT-96 when a called is
    placed on remote port 1 (ST-96) or remote port 2 (MT-96).
    ST/MT-96 is cleared by the Clear Data procedure (SP-3).
    */

    private int callPurposeBitfield; // 2 bytes A set bit indicates the trigger for the call.
                                     // b0: POWER_OUTAGE Not supported by the meter
                                     // b1: POWER_RESTORAL Indicates power restoration call
                                     // b2: SCHEDULED_CALL ST-94/MT-94 triggered call (Billing
                                     // b3: STATUS_CALL ST-3 warning triggered call (Alarm
                                     // b4: IMMEDIATE_CALL Call triggered by procedure (Call back)
                                     // b5-11: unused (spare) = 0
                                     // b12-15: Mfg. specific.
    // Copy of ST-3 table, absorb

    /** Creates a new instance of CallPurposeTableForRemotePorts */
    public CallPurposeTableForRemotePorts(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(96,true));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CallPurposeTableForRemotePorts:\n");
        strBuff.append("   callPurposeBitfield=0x"+Integer.toHexString(getCallPurposeBitfield())+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int offset = 0;
        setCallPurposeBitfield(C12ParseUtils.getInt(tableData,offset,2, dataOrder));
        offset+=2;
        // absorb all the rest of the table
    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

    public int getCallPurposeBitfield() {
        return callPurposeBitfield;
    }

    public void setCallPurposeBitfield(int callPurposeBitfield) {
        this.callPurposeBitfield = callPurposeBitfield;
    }

}
