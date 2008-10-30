/*
 * MT83Register.java
 *
 * Created on 16 juni 2003, 16:35
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83;

import com.energyict.protocolimpl.iec1107.*;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.register.*;
import com.energyict.protocol.MeterExceptionInfo;

/**
 *
 * @author  Koen
 * changes:
 * KV 17022004 extended with MeterExceptionInfo
 */
public class MT83Registry extends AbstractMT83Registry {
    
    /** Creates a new instance of MT83Register */
    public MT83Registry(MeterExceptionInfo meterExceptionInfo,ProtocolLink protocolLink) {
        super(meterExceptionInfo,protocolLink);
    }
    
    protected void initRegisters() {
//        registers.put("Total Energy A+", new VDEWRegister("20",MT83RegisterDataParse.VDEW_QUANTITY,0, -1,Unit.get("kWh"),VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED,FlagIEC1107Connection.READ1));
//        registers.put("Total Energy R1", new VDEWRegister("22",MT83RegisterDataParse.VDEW_QUANTITY,0, -1,Unit.get("kvarh"),VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED,FlagIEC1107Connection.READ1));
//        registers.put("Total Energy R4", new VDEWRegister("23",MT83RegisterDataParse.VDEW_QUANTITY,0, -1,Unit.get("kvarh"),VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED,FlagIEC1107Connection.READ1));
        registers.put("Profile Interval", new MT83Register("0.8.5",MT83RegisterDataParse.VDEW_INTEGER,0, -1,null,MT83Register.NOT_WRITEABLE,MT83Register.NOT_CACHED,FlagIEC1107Connection.READ1));
        registers.put("TimeDateWrite", new MT83Register("0.9.4",MT83RegisterDataParse.VDEW_DATE_S_TIME,0, -1,null,MT83Register.WRITEABLE,MT83Register.NOT_CACHED,FlagIEC1107Connection.READ1,FlagIEC1107Connection.WRITE1));
        // when READ5 is invoked on 11 and 12, SHHMMSS and SYYMMDD are returned S = seasonal info 0=normal, 1=DST, 2=UTC
        // when READ1 is invoked on 11 and 12, HHMMSS and YYMMDD are returned
        registers.put("TimeDateReadOnly", new MT83Register("11 12",MT83RegisterDataParse.VDEW_S_TIME_S_DATE,0, -1,null,MT83Register.NOT_WRITEABLE,MT83Register.NOT_CACHED));
        registers.put("TimeDateString", new MT83Register("0.9.4",MT83RegisterDataParse.VDEW_STRING,0, -1,null,MT83Register.WRITEABLE,MT83Register.NOT_CACHED,FlagIEC1107Connection.READ1));
        registers.put("software revision number", new MT83Register("0.2.0",MT83RegisterDataParse.VDEW_STRING,0, -1,null,MT83Register.NOT_WRITEABLE,MT83Register.NOT_CACHED,FlagIEC1107Connection.READ1));
        registers.put("meter serial number", new MT83Register("00",MT83RegisterDataParse.VDEW_STRING,0, -1,null,MT83Register.NOT_WRITEABLE,MT83Register.NOT_CACHED,FlagIEC1107Connection.READ1));
        
    }
    

    
}
