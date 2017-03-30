/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * IskraEmecoRegister.java
 *
 * Created on 16 juni 2003, 16:35
 */

package com.energyict.protocolimpl.iec1107.iskraemeco;

import com.energyict.mdc.protocol.api.MeterExceptionInfo;

import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.vdew.VDEWRegister;
import com.energyict.protocolimpl.iec1107.vdew.VDEWRegisterDataParse;

/**
 *
 * @author  Koen
 * changes:
 * KV 17022004 extended with MeterExceptionInfo
 */
public class IskraEmecoRegistry extends AbstractVDEWRegistry {

    /** Creates a new instance of IskraEmecoRegister */
    public IskraEmecoRegistry(MeterExceptionInfo meterExceptionInfo,ProtocolLink protocolLink) {
        super(meterExceptionInfo,protocolLink);
    }

    protected void initRegisters() {
//        registers.put("Total Energy A+", new VDEWRegister("20",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,Unit.get("kWh"),VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED,FlagIEC1107Connection.READ1));
//        registers.put("Total Energy R1", new VDEWRegister("22",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,Unit.get("kvarh"),VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED,FlagIEC1107Connection.READ1));
//        registers.put("Total Energy R4", new VDEWRegister("23",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,Unit.get("kvarh"),VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED,FlagIEC1107Connection.READ1));
        registers.put("Profile Interval", new VDEWRegister("0.8.5",VDEWRegisterDataParse.VDEW_INTEGER,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED,FlagIEC1107Connection.READ1));
        registers.put("TimeDateWrite", new VDEWRegister("0.9.4",VDEWRegisterDataParse.VDEW_DATE_S_TIME,0, -1,null,VDEWRegister.WRITEABLE,VDEWRegister.NOT_CACHED,FlagIEC1107Connection.READ1,FlagIEC1107Connection.WRITE1));
        // when READ5 is invoked on 11 and 12, SHHMMSS and SYYMMDD are returned S = seasonal info 0=normal, 1=DST, 2=UTC
        // when READ1 is invoked on 11 and 12, HHMMSS and YYMMDD are returned
        registers.put("TimeDateReadOnly", new VDEWRegister("11 12",VDEWRegisterDataParse.VDEW_S_TIME_S_DATE,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
        registers.put("TimeDateString", new VDEWRegister("0.9.4",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.WRITEABLE,VDEWRegister.NOT_CACHED,FlagIEC1107Connection.READ1));
        registers.put("software revision number", new VDEWRegister("0.2.0",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED,FlagIEC1107Connection.READ1));
        registers.put("meter serial number", new VDEWRegister("00",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED,FlagIEC1107Connection.READ1));

    }



}
