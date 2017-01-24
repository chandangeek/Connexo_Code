/*
 * KamstrupRegister.java
 *
 * Created on 04 mei 2004, 10:00
 */

package com.energyict.protocolimpl.iec1107.emh.lzqj;

import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.vdew.VDEWRegister;
import com.energyict.protocolimpl.iec1107.vdew.VDEWRegisterDataParse;

/**
 *
 * @author  Koen
 * Changes:
 * KV 04052004 Initial version
 */
public class LZQJRegistry extends AbstractVDEWRegistry {

	/** Creates a new instance of KamstrupRegister */
	public LZQJRegistry(MeterExceptionInfo meterExceptionInfo, ProtocolLink protocolLink) {
		// Use ChannelMap to dcetermine which VHI to access... First entry in the ChannelMap is the
		// OBIS B value.
		super(meterExceptionInfo,protocolLink,Integer.parseInt(protocolLink.getProtocolChannelMap().getProtocolChannel(0).getRegister()));
	}
	// KV TO_DO change OBIS B value to control channel id
	protected void initRegisters() {
		String obisB = Integer.toString(getRegisterSet());
		registers.put("Vb", new VDEWRegister("7-"+obisB+":23.2.0*101",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
		registers.put("Vm", new VDEWRegister("7-"+obisB+":23.0.0*101",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
		registers.put("quality/status code", new VDEWRegister("7-"+obisB+":97.97.0*101",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
		registers.put("timestamp", new VDEWRegister("7-"+obisB+":0.1.2*101",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
		registers.put("Actual status code", new VDEWRegister("7-"+obisB+":97.97.0*255",VDEWRegisterDataParse.VDEW_INTEGER,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
		registers.put("Time", new VDEWRegister("0.9.1",VDEWRegisterDataParse.VDEW_TIMESTRING,0, -1,null,VDEWRegister.WRITEABLE,VDEWRegister.NOT_CACHED));
		registers.put("Date", new VDEWRegister("0.9.2",VDEWRegisterDataParse.VDEW_DATESTRING,0, -1,null,VDEWRegister.WRITEABLE,VDEWRegister.NOT_CACHED));
		registers.put("TimeDate", new VDEWRegister("0.9.1 0.9.2",VDEWRegisterDataParse.VDEW_TIMEDATE,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
		registers.put("TimeDate2", new VDEWRegister("C003",VDEWRegisterDataParse.VDEW_DATE_TIME,0, -1,null,VDEWRegister.WRITEABLE,VDEWRegister.NOT_CACHED,FlagIEC1107Connection.READ2,FlagIEC1107Connection.WRITE2));
	}

}
