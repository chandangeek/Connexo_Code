/*
 * KamstrupRegister.java
 *
 * Created on 04 mei 2004, 10:00
 */

package com.energyict.protocolimpl.iec1107.eictrtuvdew;

import java.io.*;
import java.util.*;
import com.energyict.cbo.*;
import java.math.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.iec1107.*;
import com.energyict.protocolimpl.iec1107.vdew.*;

/**
 *
 * @author  Koen
 * Changes:
 * KV 04052004 Initial version
 */
public class EictRtuVdewRegistry extends AbstractVDEWRegistry {
    
    /** Creates a new instance of KamstrupRegister */
    public EictRtuVdewRegistry(MeterExceptionInfo meterExceptionInfo, ProtocolLink protocolLink) {
        // Use ChannelMap to determine which VHI tu access... First entry in the ChannelMap is the
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
        registers.put("Time", new VDEWRegister("0.9.1",VDEWRegisterDataParse.VDEW_GMTTIMESTRING,0, -1,null,VDEWRegister.WRITEABLE,VDEWRegister.NOT_CACHED,false));
        registers.put("Date", new VDEWRegister("0.9.2",VDEWRegisterDataParse.VDEW_GMTDATESTRING,0, -1,null,VDEWRegister.WRITEABLE,VDEWRegister.NOT_CACHED,false));
        registers.put("TimeDate", new VDEWRegister("0.9.1 0.9.2",VDEWRegisterDataParse.VDEW_TIMEDATE,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
    }
    
}
