/*
 * IskraEmecoRegister.java
 *
 * Created on 16 juni 2003, 16:35
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83;

import java.io.*;
import java.util.*;
import com.energyict.cbo.*;
import java.math.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.iec1107.*;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.vdew.*;
import com.energyict.protocol.MeterExceptionInfo;

/**
 *
 * @author  Koen
 * changes:
 * KV 17022004 extended with MeterExceptionInfo
 */
public class MT83Registry extends AbstractVDEWRegistry {
    
	public static final String SERIAL = "MeterSerialNumber";
	public static final String SOFTWARE_REVISION = "SoftwareRevisionNumber";
	public static final String PROFILE_INTERVAL = "ProfileInterval";

	public static final String TIME_AND_DATE_STRING = "TimeDateString";
	public static final String TIME_AND_DATE_READWRITE = "TimeDateReadWrite";
	public static final String TIME_AND_DATE_READONLY = "TimeDateReadOnly";
		
    /** Creates a new instance of IskraEmecoRegister */
    public MT83Registry(MeterExceptionInfo meterExceptionInfo,ProtocolLink protocolLink) {
        super(meterExceptionInfo,protocolLink);
    }
    
	protected void initRegisters() {
		final byte[] READ1 = FlagIEC1107Connection.READ1;
		final byte[] READ5 = FlagIEC1107Connection.READ5;
		final byte[] READ6 = FlagIEC1107Connection.READ6;
		final byte[] WRITE1 = FlagIEC1107Connection.WRITE1;
		final byte[] WRITE5 = FlagIEC1107Connection.WRITE5;

		final int STRING = VDEWRegisterDataParse.VDEW_STRING;
		final int INTEGER = VDEWRegisterDataParse.VDEW_INTEGER;
		final int DATETIME = VDEWRegisterDataParse.VDEW_DATETIME;

		// when READ5 is invoked on 11 and 12, SHHMMSS and SYYMMDD are returned S = seasonal info 0=normal, 1=DST, 2=UTC
        // when READ1 is invoked on 11 and 12, HHMMSS and YYMMDD are returned

        registers.put(SERIAL, newReg("1-0:0.0.0*255", STRING, false, null, READ1, WRITE1));
        registers.put(SOFTWARE_REVISION, newReg("0.2.0", STRING, false, null, READ1, WRITE1));
        registers.put(PROFILE_INTERVAL, newReg("0.8.5", INTEGER, false, null, READ1, WRITE1));

        registers.put(TIME_AND_DATE_STRING, newReg("0.9.4", STRING, false, null, READ1, WRITE1));
        registers.put(TIME_AND_DATE_READONLY, newReg("0.9.4", DATETIME, false, null, READ1, WRITE1));
        registers.put(TIME_AND_DATE_READWRITE, newReg("0.9.4",DATETIME, true, null, READ1, WRITE1));

     
        
//      registers.put("Total Energy A+", new VDEWRegister("20",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,Unit.get("kWh"),VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED,FlagIEC1107Connection.READ1));
//      registers.put("Total Energy R1", new VDEWRegister("22",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,Unit.get("kvarh"),VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED,FlagIEC1107Connection.READ1));
//      registers.put("Total Energy R4", new VDEWRegister("23",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,Unit.get("kvarh"),VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED,FlagIEC1107Connection.READ1));
        //this(objectId,type,offset,length,unit,writeable,cached,FlagIEC1107Connection.READ5,FlagIEC1107Connection.WRITE5,usePassword);

    }
        
    private VDEWRegister newReg(String registerID_in, int dataType, boolean isWritable, Unit unit, byte[] readCommand, byte[] writeCommand) {
    	return new VDEWRegister(registerID_in, dataType,0, -1, unit, isWritable, VDEWRegister.NOT_CACHED, readCommand, writeCommand, true);
    }
    
    
}
