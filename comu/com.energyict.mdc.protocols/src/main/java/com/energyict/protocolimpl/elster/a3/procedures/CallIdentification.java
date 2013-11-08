/*
 * SnapShotData.java
 *
 * Created on 9 december 2005, 21:04
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.procedures;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.procedures.AbstractProcedure;
import com.energyict.protocolimpl.ansi.c12.procedures.ProcedureFactory;
import com.energyict.protocolimpl.ansi.c12.procedures.ProcedureIdentification;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class CallIdentification extends AbstractProcedure {

    /*
       Allows a meter reading system to identify the meters remote
       port that the call has been placed on and the reason for the call.
       byte 1 port (1 = dedicated remote port, 2 = shared remote port)
       byte 2-3 copy of XT-96 call purpose for the associated port.
     */
     private int port; // 1 byte
     private int callPurposeBitfield; // 2 bytes A set bit indicates the trigger for the call.
                                      // b0: POWER_OUTAGE Not supported by the meter
                                      // b1: POWER_RESTORAL Indicates power restoration call
                                      // b2: SCHEDULED_CALL ST-94/MT-94 triggered call (Billing
                                      // b3: STATUS_CALL ST-3 warning triggered call (Alarm
                                      // b4: IMMEDIATE_CALL Call triggered by procedure (Call back)
                                      // b5-11: unused (spare) = 0
                                      // b12-15: Mfg. specific.								 
    
    /** Creates a new instance of SnapShotData */
    public CallIdentification(ProcedureFactory procedureFactory) {
        super(procedureFactory,new ProcedureIdentification(6,true));
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CallIdentification:\n");
        strBuff.append("   callPurposeBitfield="+getCallPurposeBitfield()+"\n");
        strBuff.append("   port="+getPort()+"\n");
        return strBuff.toString();
    }
    
    protected void parse(byte[] data) throws IOException {
        int dataOrder = getProcedureFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int offset=0;
        setPort(C12ParseUtils.getInt(data,offset++));
        setCallPurposeBitfield(C12ParseUtils.getInt(data,offset,2, dataOrder)); offset+=2;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getCallPurposeBitfield() {
        return callPurposeBitfield;
    }

    public void setCallPurposeBitfield(int callPurposeBitfield) {
        this.callPurposeBitfield = callPurposeBitfield;
    }
    
}
