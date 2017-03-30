/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * KamstrupRegister.java
 *
 * Created on 04 mei 2004, 10:00
 */

package com.energyict.protocolimpl.iec1107.ferranti;

import com.energyict.mdc.protocol.api.MeterExceptionInfo;

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
public class FerrantiRegistry extends AbstractVDEWRegistry {

    /** Creates a new instance of KamstrupRegister */
    public FerrantiRegistry(MeterExceptionInfo meterExceptionInfo,ProtocolLink protocolLink) {
        // Use ChannelMap to dcetermine which VHI tu access... First entry in the ChannelMap is the
        // OBIS B value.
        super(meterExceptionInfo,protocolLink,Integer.parseInt(protocolLink.getChannelMap().getChannel(0).getRegister()));
    }
    // KV TO_DO change OBIS B value to control channel id
    protected void initRegisters() {
        String obisB = Integer.toString(getRegisterSet());
        registers.put("Vb", new VDEWRegister("7-"+obisB+":23.2.0*101",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
        registers.put("Vm", new VDEWRegister("7-"+obisB+":23.0.0*101",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
        registers.put("quality/status code", new VDEWRegister("7-"+obisB+":97.97.0*101",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
        registers.put("timestamp", new VDEWRegister("7-"+obisB+":0.1.2*101",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
        registers.put("Actual status code", new VDEWRegister("7-"+obisB+":97.97.0*255",VDEWRegisterDataParse.VDEW_INTEGER,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
        registers.put("Time in the device", new VDEWRegister("0-"+obisB+":1.0.0*255",VDEWRegisterDataParse.VDEW_TIMEDATE_FERRANTI,0, -1,null,VDEWRegister.WRITEABLE,VDEWRegister.NOT_CACHED,false));
    }

}
