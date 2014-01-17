/*
 * KamstrupRegister.java
 *
 * Created on 04 mei 2004, 10:00
 */

package com.energyict.protocolimpl.iec1107.abba1350;

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
public class ABBA1350Registry extends AbstractVDEWRegistry {

	private static final int DEBUG = 0;

	public static final String ID1 = "ID1";
	public static final String ID2 = "ID2";
	public static final String ID3 = "ID3";
	public static final String ID4 = "ID4";
	public static final String ID5 = "ID5";
	public static final String ID6 = "ID6";

	public static final String SERIAL = "Serial";
	public static final String FIRMWARE = "Firmware";
	public static final String HARDWARE = "Hardware";

	public static final String FIRMWAREID = "FirmwareId";

	public static final String IEC1107_ID = "IEC1107_ID";
	public static final String IEC1107_ADDRESS_OP = "IEC1107_ADDRESS_OP";
	public static final String IEC1107_ADDRESS_EL = "IEC1107_ADDRESS_EL";

    /** Creates a new instance of KamstrupRegister */
    public ABBA1350Registry(MeterExceptionInfo meterExceptionInfo, ProtocolLink protocolLink) {
        // Use ChannelMap to dcetermine which VHI tu access... First entry in the ChannelMap is the
        // OBIS B value.
        super(meterExceptionInfo,protocolLink,Integer.parseInt(protocolLink.getChannelMap().getChannel(0).getRegister()));
    }
    // KV TO_DO change OBIS B value to control channel id
    protected void initRegisters() {
        String obisB = Integer.toString(getRegisterSet());
        registers.put(SERIAL, new VDEWRegister("0.0.0",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));
        registers.put(FIRMWARE, new VDEWRegister("C0800000000",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED, FlagIEC1107Connection.READ3));
        registers.put(HARDWARE, new VDEWRegister("C06001E0000",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED, FlagIEC1107Connection.READ3));

        registers.put(FIRMWAREID, new VDEWRegister("0.2.0",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));

        registers.put(ID1, new VDEWRegister("C0100080004",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED, FlagIEC1107Connection.READ3));
        registers.put(ID2, new VDEWRegister("C010008000C",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED, FlagIEC1107Connection.READ3));
        registers.put(ID3, new VDEWRegister("C0100080014",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED, FlagIEC1107Connection.READ3));
        registers.put(ID4, new VDEWRegister("C0100080050",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED, FlagIEC1107Connection.READ3));
        registers.put(ID5, new VDEWRegister("C0100080058",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED, FlagIEC1107Connection.READ3));
        registers.put(ID6, new VDEWRegister("C0100080060",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED, FlagIEC1107Connection.READ3));

        registers.put(IEC1107_ID, new VDEWRegister("C010010001C",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED, FlagIEC1107Connection.READ3));
        registers.put(IEC1107_ADDRESS_OP, new VDEWRegister("C010010002C",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED, FlagIEC1107Connection.READ3));
        registers.put(IEC1107_ADDRESS_EL, new VDEWRegister("C010010003C",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED, FlagIEC1107Connection.READ3));

        registers.put("Vb", new VDEWRegister("7-"+obisB+":23.2.0*101",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
        registers.put("Vm", new VDEWRegister("7-"+obisB+":23.0.0*101",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
        registers.put("quality/status code", new VDEWRegister("7-"+obisB+":97.97.0*101",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
        registers.put("timestamp", new VDEWRegister("7-"+obisB+":0.1.2*101",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
        registers.put("Actual status code", new VDEWRegister("7-"+obisB+":97.97.0*255",VDEWRegisterDataParse.VDEW_INTEGER,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
        registers.put("Time", new VDEWRegister("0.9.1",VDEWRegisterDataParse.VDEW_TIMESTRING,0, -1,null,VDEWRegister.WRITEABLE,VDEWRegister.NOT_CACHED));
        registers.put("Date", new VDEWRegister("0.9.2",VDEWRegisterDataParse.VDEW_DATESTRING,0, -1,null,VDEWRegister.WRITEABLE,VDEWRegister.NOT_CACHED));
        registers.put("TimeDate", new VDEWRegister("0.9.1 0.9.2",VDEWRegisterDataParse.VDEW_TIMEDATE,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
        registers.put("TimeDate2", new VDEWRegister("C003",VDEWRegisterDataParse.VDEW_DATE_TIME,0, -1,null,VDEWRegister.WRITEABLE,VDEWRegister.NOT_CACHED,FlagIEC1107Connection.READ5,FlagIEC1107Connection.WRITE2));
    }

}
